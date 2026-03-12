import { create } from 'zustand'
import type { User } from '../types'

interface AuthStore {
  // State
  token: string | null
  refreshToken: string | null
  user: User | null
  sessionId: string | null
  expiresIn: number | null

  // Actions
  setToken: (token: string) => void
  setRefreshToken: (token: string) => void
  setUser: (user: User) => void
  setSessionId: (sessionId: string) => void
  setExpiresIn: (expiresIn: number) => void
  logout: () => void
  isAuthenticated: () => boolean
}

/**
 * Auth Store - Manages authentication state globally
 *
 * Usage:
 * const { token, user, logout, isAuthenticated } = useAuthStore()
 *
 * Actions:
 * - setToken - Store JWT access token from login
 * - setRefreshToken - Store refresh token from login
 * - setUser - Store user info from login
 * - setSessionId - Store session ID from login
 * - setExpiresIn - Store token expiration time
 * - logout - Clear all auth data on logout
 * - isAuthenticated - Check if user is currently logged in
 *
 * Storage: All tokens persist in localStorage to survive page reload
 * Auth Flow: Login → setToken/User/SessionId → isAuthenticated returns true
 *            Logout → clear localStorage → isAuthenticated returns false
 */
export const useAuthStore = create<AuthStore>((set, get) => ({
  token: localStorage.getItem('authToken'),
  refreshToken: localStorage.getItem('authRefreshToken'),
  user: null,
  sessionId: localStorage.getItem('authSessionId'),
  expiresIn: null,

  setToken: (token: string) => {
    localStorage.setItem('authToken', token)
    set({ token })
    console.log('✅ Access token stored')
  },

  setRefreshToken: (refreshToken: string) => {
    localStorage.setItem('authRefreshToken', refreshToken)
    set({ refreshToken })
    console.log('✅ Refresh token stored')
  },

  setUser: (user: User) => {
    set({ user })
    console.log(`✅ User set: ${user.email} (${user.role})`)
  },

  setSessionId: (sessionId: string) => {
    localStorage.setItem('authSessionId', sessionId)
    set({ sessionId })
    console.log(`✅ Session stored: ${sessionId}`)
  },

  setExpiresIn: (expiresIn: number) => {
    set({ expiresIn })
    console.log(`⏱️ Token expiry: ${expiresIn}ms from now`)
  },

  logout: () => {
    localStorage.removeItem('authToken')
    localStorage.removeItem('authRefreshToken')
    localStorage.removeItem('authSessionId')
    set({ token: null, refreshToken: null, user: null, sessionId: null, expiresIn: null })
    console.log('🚪 User logged out - all tokens cleared')
  },

  isAuthenticated: () => {
    const { token } = get()
    return !!token
  },
}))
