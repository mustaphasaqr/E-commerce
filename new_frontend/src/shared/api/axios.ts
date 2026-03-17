import axios, { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'

// In development: use dev server proxy (relative path)
// In production: use absolute URL from env variable
const API_URL = import.meta.env.DEV 
  ? '/api' 
  : (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api'

const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

let refreshPromise: Promise<string | null> | null = null
let hasForcedReauth = false

const ANALYTICS_PATH_SEGMENT = '/owner/analytics'
const ANALYTICS_WINDOW_MS = 60_000
const ANALYTICS_MAX_REQUESTS_PER_WINDOW = 40
const analyticsRequestTimestamps: number[] = []
let analyticsCooldownUntil = 0

const ERROR_CODE_MESSAGE_MAP: Record<string, string> = {
  PROD_CONFLICT_003: 'This product is discontinued and cannot be modified.',
}

const extractBackendErrorCode = (payload: unknown): string | null => {
  if (!payload || typeof payload !== 'object') {
    return null
  }

  const typedPayload = payload as {
    code?: string
    errorCode?: string
    message?: string
    error?: { code?: string; errorCode?: string; message?: string } | string
  }

  const directCode = typedPayload.code || typedPayload.errorCode
  if (typeof directCode === 'string' && directCode.trim().length > 0) {
    return directCode
  }

  if (typedPayload.error && typeof typedPayload.error === 'object') {
    const nestedCode = typedPayload.error.code || typedPayload.error.errorCode
    if (typeof nestedCode === 'string' && nestedCode.trim().length > 0) {
      return nestedCode
    }
  }

  const message =
    typedPayload.message ||
    (typeof typedPayload.error === 'string' ? typedPayload.error : typedPayload.error?.message) ||
    ''

  const matchedCode = message.match(/\b[A-Z]+_[A-Z]+_\d{3}\b/)
  return matchedCode ? matchedCode[0] : null
}

const isProductStateConflict = (status?: number, url?: string, message?: string, errorCode?: string | null): boolean => {
  if (status !== 409 || typeof url !== 'string') {
    return false
  }

  const isProductStateEndpoint = /\/products\/[^/]+\/(activate|deactivate|price|details|images)$/.test(url)
  if (!isProductStateEndpoint) {
    return false
  }

  if (errorCode === 'PROD_CONFLICT_003') {
    return true
  }

  return typeof message === 'string' && /invalid product state/i.test(message)
}

const sleep = (ms: number): Promise<void> =>
  new Promise((resolve) => setTimeout(resolve, ms))

const isAnalyticsRequest = (url?: string): boolean =>
  typeof url === 'string' && url.includes(ANALYTICS_PATH_SEGMENT)

const parseRetryAfterSeconds = (error: AxiosError): number | null => {
  const retryAfterHeader = error.response?.headers?.['retry-after']
  const retryAfterRaw = Array.isArray(retryAfterHeader) ? retryAfterHeader[0] : retryAfterHeader
  const retryAfter = Number(retryAfterRaw)
  if (Number.isFinite(retryAfter) && retryAfter > 0) {
    return retryAfter
  }

  const payload = error.response?.data as
    | { message?: string; error?: string | { message?: string } }
    | undefined
  const message =
    payload?.message ||
    (typeof payload?.error === 'string' ? payload.error : payload?.error?.message) ||
    ''

  const match = message.match(/(\d+)\s*seconds?/i)
  if (!match) {
    return null
  }

  const parsed = Number(match[1])
  return Number.isFinite(parsed) ? parsed : null
}

const throttleAnalyticsRequest = async (): Promise<void> => {
  while (true) {
    const now = Date.now()

    while (analyticsRequestTimestamps.length > 0 && now - analyticsRequestTimestamps[0] >= ANALYTICS_WINDOW_MS) {
      analyticsRequestTimestamps.shift()
    }

    if (analyticsCooldownUntil > now) {
      const waitMs = Math.max(100, analyticsCooldownUntil - now)
      await sleep(waitMs)
      continue
    }

    if (analyticsRequestTimestamps.length < ANALYTICS_MAX_REQUESTS_PER_WINDOW) {
      analyticsRequestTimestamps.push(now)
      return
    }

    const oldest = analyticsRequestTimestamps[0]
    const waitMs = Math.max(100, ANALYTICS_WINDOW_MS - (now - oldest) + 25)
    await sleep(waitMs)
  }
}

const getFromStorage = (key: string): string | null => {
  if (typeof window !== 'undefined' && localStorage) {
    return localStorage.getItem(key)
  }
  return null
}

const setInStorage = (key: string, value: string): void => {
  if (typeof window !== 'undefined' && localStorage) {
    localStorage.setItem(key, value)
  }
}

const clearAuthState = (): void => {
  if (typeof window !== 'undefined' && localStorage) {
    localStorage.removeItem('authToken')
    localStorage.removeItem('authRefreshToken')
    localStorage.removeItem('authSessionId')
    localStorage.removeItem('authUser')
  }
}

const forceReauthenticate = (): void => {
  if (typeof window === 'undefined' || hasForcedReauth) {
    return
  }

  hasForcedReauth = true
  clearAuthState()

  if (!window.location.pathname.startsWith('/login')) {
    const redirect = encodeURIComponent(window.location.pathname + window.location.search)
    window.location.replace(`/login?reason=expired&redirect=${redirect}`)
  }
}

const refreshAccessToken = async (): Promise<string | null> => {
  const refreshToken = getFromStorage('authRefreshToken')
  if (!refreshToken) {
    return null
  }

  try {
    const response = await axios.post(
      `${API_URL}/v1/auth/refresh`,
      { refreshToken },
      {
        timeout: parseInt(API_TIMEOUT as string),
        headers: {
          'Content-Type': 'application/json',
        },
      }
    )

    const newAccessToken = response.data?.accessToken as string | undefined
    const newRefreshToken = response.data?.refreshToken as string | undefined
    if (!newAccessToken) {
      return null
    }

    setInStorage('authToken', newAccessToken)
    if (newRefreshToken) {
      setInStorage('authRefreshToken', newRefreshToken)
    }
    return newAccessToken
  } catch {
    return null
  }
}

const axiosInstance: AxiosInstance = axios.create({
  baseURL: `${API_URL}/v1`,
  timeout: parseInt(API_TIMEOUT as string),
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - logs all API calls
axiosInstance.interceptors.request.use(
  async (config) => {
    if (isAnalyticsRequest(config.url)) {
      await throttleAnalyticsRequest()
    }

    // Only access localStorage if in browser environment
    if (typeof window !== 'undefined' && localStorage) {
      const token = localStorage.getItem('authToken')
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }
    
    // Console monitoring - request
    console.log(`📤 API Request: ${config.method?.toUpperCase()} ${config.url}`, {
      data: config.data,
      params: config.params,
    })
    
    return config
  },
  async (error: AxiosError) => {
    console.error('❌ Request Error:', error.message)
    return Promise.reject(error)
  }
)

// Response interceptor - logs all responses
axiosInstance.interceptors.response.use(
  (response) => {
    // Console monitoring - success response
    console.log(`✅ API Success: ${response.status} ${response.config.url}`, response.data)
    return response
  },
  async (error: AxiosError) => {
    // Console monitoring - error response with detailed info
    const errorData = error.response?.data as any
    const backendErrorCode = extractBackendErrorCode(errorData)
    const rawMessage =
      errorData?.message ||
      errorData?.error?.message ||
      errorData?.error ||
      error.message
    const fallbackMappedMessage = isProductStateConflict(error.response?.status, error.config?.url, rawMessage, backendErrorCode)
      ? ERROR_CODE_MESSAGE_MAP.PROD_CONFLICT_003
      : undefined
    const mappedMessage = fallbackMappedMessage

    if (mappedMessage && errorData && typeof errorData === 'object') {
      errorData.message = mappedMessage
      if (errorData.error && typeof errorData.error === 'object') {
        errorData.error.message = mappedMessage
      }
      error.message = mappedMessage
    } else if (mappedMessage) {
      error.message = mappedMessage
    }
    const errorMessage = 
      errorData?.message || 
      errorData?.error?.message ||
      errorData?.error ||
      error.message
    
    console.error(
      `❌ API Error: ${error.response?.status || 'Network Error'} ${error.config?.url}`,
      {
        status: error.response?.status,
        message: errorMessage,
        fullResponseData: error.response?.data,
      }
    )
    console.log('📋 Full Response:', error.response)

    if (error.response?.status === 429 && isAnalyticsRequest(error.config?.url)) {
      const retryAfterSeconds = parseRetryAfterSeconds(error) ?? 60
      analyticsCooldownUntil = Math.max(analyticsCooldownUntil, Date.now() + retryAfterSeconds * 1000)
      console.warn(`⏳ Analytics cooldown active for ${retryAfterSeconds} seconds to respect backend rate limits`)
    }
    
    if (error.response?.status === 401) {
      const isLoginRequest = error.config?.url?.includes('/auth/login')
      const isRefreshRequest = error.config?.url?.includes('/auth/refresh')
      const originalRequest = error.config as RetryableRequestConfig | undefined

      if (!isLoginRequest && !isRefreshRequest && originalRequest && !originalRequest._retry) {
        originalRequest._retry = true

        if (!refreshPromise) {
          refreshPromise = refreshAccessToken().finally(() => {
            refreshPromise = null
          })
        }

        const newToken = await refreshPromise
        if (newToken) {
          originalRequest.headers = originalRequest.headers ?? {}
          originalRequest.headers.Authorization = `Bearer ${newToken}`
          return axiosInstance(originalRequest)
        }

        console.warn('⚠️ Refresh token failed - forcing re-authentication')
        forceReauthenticate()
      }

      if (isLoginRequest || isRefreshRequest) {
        console.warn('⚠️ Authentication endpoint returned 401')
      } else {
        console.warn('⚠️ Unauthorized response received - forcing re-authentication')
        forceReauthenticate()
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
