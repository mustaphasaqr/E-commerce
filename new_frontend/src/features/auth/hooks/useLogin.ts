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
  const { setToken, setUser, setRefreshToken, setSessionId } = useAuthStore()

  const login = async (request: LoginRequest): Promise<LoginResponse | null> => {
    setLoading(true)
    setError(null)
    setFieldErrors({})
    setRetryAfter(null)

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
          message = apiMsg || 'Too many failed login attempts. Please try again later.'
        }
        // Professional message for unregistered/deleted account
        else if (
          status === 401 &&
          (apiMsg.toLowerCase().includes('not found') || apiMsg.toLowerCase().includes('no user') || apiMsg.toLowerCase().includes('deleted') || apiMsg.toLowerCase().includes('invalid credentials'))
        ) {
          message =
            'No account found for this email address. ' +
            'If you don\'t have an account, you can ' +
            '<a href="/register" class="text-blue-600 hover:text-blue-800 underline font-semibold">create one here</a>.'
          emailError = 'Account not found'
        } else if (status === 401 && apiMsg.toLowerCase().includes('password')) {
          message = 'Incorrect password. Please try again.'
          passwordError = 'Incorrect password'
        } else if (apiMsg) {
          message = apiMsg
        }
      } else if (err instanceof Error) {
        message = err.message
      }
      setError(message)
      setFieldErrors({ email: emailError, password: passwordError })
      setRetryAfter(retryAfterSeconds)
      console.error('❌ Login error:', message)
      return null
    } finally {
      setLoading(false)
    }
  }

  return { login, loading, error, fieldErrors, retryAfter }
}
