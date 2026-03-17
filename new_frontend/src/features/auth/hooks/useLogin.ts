import { useState } from 'react'
import { login as loginService } from '../api/authService'
import { useAuthStore } from './useAuthStore'
import type { LoginRequest, LoginResponse } from '../types'

/**
 * useLogin Hook
 *
 * Pattern: Calls service like test does
 * const { login, loading, error, data } = useLogin()
 *
 * Manages: loading state, error state, API call
 * Updates: authStore with token, user, sessionId
 */
export function useLogin() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({})
  const [retryAfter, setRetryAfter] = useState<number | null>(null)
  const [rateLimitedEmail, setRateLimitedEmail] = useState<string | null>(null)
  const { setToken, setUser, setRefreshToken, setSessionId } = useAuthStore()

  const login = async (request: LoginRequest): Promise<LoginResponse | null> => {
    setLoading(true)
    setError(null)
    setFieldErrors({})
    setRetryAfter(null)
    setRateLimitedEmail(null)

    try {
      const response = await loginService(request)
      setToken(response.accessToken)
      setRefreshToken(response.refreshToken)
      setUser(response.user)
      setSessionId(response.sessionId)
      console.log('🔓 Login: User authenticated', response.user.email)
      return response
    } catch (err: any) {
      let message = 'Login failed. Please try again.'
      let emailError: string | undefined
      let passwordError: string | undefined
      let retryAfterSeconds: number | null = null
      let limitedEmail: string | null = null

      if (err?.response) {
        const status = err.response.status
        const apiMsg = err.response.data?.message || ''
        // Handle rate limit (429)
        if (status === 429) {
          // Check Retry-After header (in seconds)
          const retryAfterHeader = err.response.headers?.['retry-after']
          if (retryAfterHeader) {
            retryAfterSeconds = parseInt(retryAfterHeader, 10)
          } else if (err.response.data?.retryAfterSeconds) {
            retryAfterSeconds = parseInt(err.response.data.retryAfterSeconds, 10)
          }
          // Try to get the affected email from backend response
          if (err.response.data?.email) {
            limitedEmail = err.response.data.email
          }
          message = apiMsg || 'Too many failed login attempts. Please try again later.'
        } else if (status === 401) {
          // For any 401 error, always use the generic message
          message = 'Login failed. Please try again.'
        } else if (apiMsg) {
          message = apiMsg
        }
      } else if (err instanceof Error) {
        message = err.message
      }
      setError(message)
      setFieldErrors({ email: emailError, password: passwordError })
      setRetryAfter(retryAfterSeconds)
      setRateLimitedEmail(limitedEmail)
      console.error('❌ Login error:', message)
      return null
    } finally {
      setLoading(false)
    }
  }

  const clearRateLimitState = () => {
    setError(null)
    setFieldErrors({})
    setRetryAfter(null)
    setRateLimitedEmail(null)
  }
  return { login, loading, error, fieldErrors, retryAfter, rateLimitedEmail, clearRateLimitState }
}
