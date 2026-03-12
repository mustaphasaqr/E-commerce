import { create } from 'zustand'

interface User {
  id: string
  email: string
  name: string
  role: 'user' | 'admin'
}

interface AuthStore {
  token: string | null
  user: User | null
  setToken: (token: string) => void
  setUser: (user: User) => void
  logout: () => void
  isAuthenticated: () => boolean
}

/**
 * Auth Store - Manages authentication state globally
 *
 * Usage:
 * const { token, user, logout, isAuthenticated } = useAuthStore()
 *
 * setToken - Store JWT token from login
 * setUser - Store user info from login
 * logout - Clear token and user on logout
 * isAuthenticated - Check if user is logged in
 */
export const useAuthStore = create<AuthStore>((set, get) => ({
  token: localStorage.getItem('authToken'),
  user: null,

  setToken: (token: string) => {
    localStorage.setItem('authToken', token)
    set({ token })
    console.log('✅ Auth token stored')
  },

  setUser: (user: User) => {
    set({ user })
    console.log(`✅ User set: ${user.email} (${user.role})`)
  },

  logout: () => {
    localStorage.removeItem('authToken')
    set({ token: null, user: null })
    console.log('🚪 User logged out')
  },

  isAuthenticated: () => !!get().token,
}))
