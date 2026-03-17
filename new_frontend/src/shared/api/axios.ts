import axios, { AxiosInstance, AxiosError } from 'axios'

// In development: use dev server proxy (relative path)
// In production: use absolute URL from env variable
const API_URL = import.meta.env.DEV 
  ? '/api' 
  : (import.meta.env.VITE_API_URL || 'http://localhost:8080') + '/api'

const API_TIMEOUT = import.meta.env.VITE_API_TIMEOUT || 30000

const axiosInstance: AxiosInstance = axios.create({
  baseURL: `${API_URL}/v1`,
  timeout: parseInt(API_TIMEOUT as string),
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - logs all API calls
axiosInstance.interceptors.request.use(
  (config) => {
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
  (error: AxiosError) => {
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
  (error: AxiosError) => {
    // Console monitoring - error response with detailed info
    const errorData = error.response?.data as any
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
    
    if (error.response?.status === 401) {
      // Only redirect/clear if NOT already on login page and NOT a login attempt
      const isLoginRequest = error.config?.url?.includes('/auth/login')
      const isOnLoginPage = typeof window !== 'undefined' && window.location.pathname.startsWith('/login')
      if (!isLoginRequest && !isOnLoginPage) {
        console.warn('⚠️ Unauthorized - clearing auth and redirecting to login')
        if (typeof window !== 'undefined' && localStorage) {
          localStorage.removeItem('authToken')
          window.location.href = '/login'
        }
      }
    }
    return Promise.reject(error)
  }
)

export default axiosInstance
