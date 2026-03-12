import { useState } from 'react'
import { logout as logoutService } from '../api/authService'
import { useAuthStore } from './useAuthStore'

/**
 * useLogout Hook
 *
 * Pattern: Calls service like test does
 * const { logout, loading, error } = useLogout()
 *
 * Manages: loading state, error state
 * Updates: authStore to clear all auth data
 */
export function useLogout() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const { logout: clearAuth } = useAuthStore()

  const logout = async (): Promise<void> => {
    setLoading(true)
    setError(null)

    try {
      await logoutService()
      clearAuth() // Clear local store
      console.log('🔓 Logout: Session invalidated')
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Logout failed'
      setError(message)
      console.error('❌ Logout error:', message)
    } finally {
      setLoading(false)
    }
  }

  return { logout, loading, error }
}
