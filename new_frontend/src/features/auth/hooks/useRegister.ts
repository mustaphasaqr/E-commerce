import { useState } from 'react'
import { register as registerService } from '../api/authService'
import type { RegisterRequest, LoginResponse } from '../types'
import { useAuthStore } from './useAuthStore'

/**
 * useRegister Hook
 *
 * Pattern: Calls service like test does
 * const { register, loading, error, data } = useRegister()
 *
 * Manages: loading state, error state, registration response
 * Does NOT auto-login - user must verify email and login after registration
 */
export function useRegister() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [data, setData] = useState<LoginResponse | null>(null)
  const { setUser, setToken, setRefreshToken, setSessionId } = useAuthStore()

  const register = async (request: RegisterRequest): Promise<LoginResponse | null> => {
    setLoading(true)
    setError(null)

    try {
      const response = await registerService(request)
      setData(response)
      setToken(response.accessToken)
      setRefreshToken(response.refreshToken)
      setUser(response.user)
      setSessionId(response.sessionId)
      console.log('🆕 Registration: User registered and auto-logged in')
      return response
    } catch (err) {
      // Extract detailed error message from axios error response
      let message = 'Registration failed'
      
      if (err instanceof Error) {
        // Check if it's an axios error with response data
        const axiosError = err as any
        if (axiosError.response?.data?.message) {
          message = axiosError.response.data.message
        } else if (axiosError.response?.data?.error) {
          message = axiosError.response.data.error
        } else if (axiosError.response?.statusText) {
          message = `${axiosError.response.statusText}: ${axiosError.message}`
        } else {
          message = err.message
        }
      }
      
      setError(message)
      console.error('❌ Registration error:', message)
      console.error('Full error details:', err)
      return null
    } finally {
      setLoading(false)
    }
  }

  return { register, loading, error, data }
}
