import { create } from 'zustand'
import type { User } from '../types'

// Helper functions to safely access localStorage
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

const removeFromStorage = (key: string): void => {
  if (typeof window !== 'undefined' && localStorage) {
    localStorage.removeItem(key)
  }
}

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
  token: getFromStorage('authToken'),
  refreshToken: getFromStorage('authRefreshToken'),
  user: null,
  sessionId: getFromStorage('authSessionId'),
  expiresIn: null,

  setToken: (token: string) => {
    setInStorage('authToken', token)
    set({ token })
    console.log('✅ Access token stored')
  },

  setRefreshToken: (refreshToken: string) => {
    setInStorage('authRefreshToken', refreshToken)
    set({ refreshToken })
    console.log('✅ Refresh token stored')
  },

  setUser: (user: User) => {
    set({ user })
    console.log(`✅ User set: ${user.email} (${user.role})`)
  },

  setSessionId: (sessionId: string) => {
    setInStorage('authSessionId', sessionId)
    set({ sessionId })
    console.log(`✅ Session stored: ${sessionId}`)
  },

  setExpiresIn: (expiresIn: number) => {
    set({ expiresIn })
    console.log(`⏱️ Token expiry: ${expiresIn}ms from now`)
  },

  logout: () => {
    removeFromStorage('authToken')
    removeFromStorage('authRefreshToken')
    removeFromStorage('authSessionId')
    set({ token: null, refreshToken: null, user: null, sessionId: null, expiresIn: null })
    console.log('🚪 User logged out - all tokens cleared')
  },

  isAuthenticated: () => {
    const { token } = get()
    return !!token
  },
}))
