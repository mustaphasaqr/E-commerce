import { useState } from 'react'
import { register as registerService } from '../api/authService'
import type { RegisterRequest, RegisterResponse } from '../types'

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
  const [data, setData] = useState<RegisterResponse | null>(null)

  const register = async (request: RegisterRequest): Promise<RegisterResponse | null> => {
    setLoading(true)
    setError(null)

    try {
      const response = await registerService(request)
      setData(response)
      console.log('🆕 Registration: Check email to verify account')
      return response
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Registration failed'
      setError(message)
      console.error('❌ Registration error:', message)
      return null
    } finally {
      setLoading(false)
    }
  }

  return { register, loading, error, data }
}
