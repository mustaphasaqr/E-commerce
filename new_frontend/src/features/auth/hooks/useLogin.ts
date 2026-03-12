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
  const { setToken, setUser, setRefreshToken, setSessionId } = useAuthStore()

  const login = async (request: LoginRequest): Promise<LoginResponse | null> => {
    setLoading(true)
    setError(null)

    try {
      const response = await loginService(request)

      // Update store with response
      setToken(response.accessToken)
      setRefreshToken(response.refreshToken)
      setUser(response.user)
      setSessionId(response.sessionId)

      console.log('🔓 Login: User authenticated', response.user.email)
      return response
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Login failed'
      setError(message)
      console.error('❌ Login error:', message)
      return null
    } finally {
      setLoading(false)
    }
  }

  return { login, loading, error }
}
