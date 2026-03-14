import { useAuthStore } from './useAuthStore'
import { useLogout } from './useLogout'

/**
 * useAuth Hook - Main Authentication Hook
 *
 * Provides:
 * - isAuthenticated: boolean
 * - user: User object or null
 * - token: JWT token or null
 * - logout: Function to logout
 * - loading: boolean (logout loading state)
 *
 * Usage: const { isAuthenticated, user, logout } = useAuth()
 */
export function useAuth() {
  const { token, user } = useAuthStore()
  const { logout, loading } = useLogout()

  return {
    isAuthenticated: !!token && !!user,
    token,
    user,
    logout,
    loading,
  }
}

