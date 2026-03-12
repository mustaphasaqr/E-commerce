import { create } from 'zustand'

interface AuthStore {
  token: string | null
  user: { id: string; email: string; name: string } | null
  setToken: (token: string) => void
  setUser: (user: { id: string; email: string; name: string }) => void
  logout: () => void
  isAuthenticated: () => boolean
}

export const useAuthStore = create<AuthStore>((set, get) => ({
  token: localStorage.getItem('authToken'),
  user: null,
  setToken: (token: string) => {
    localStorage.setItem('authToken', token)
    set({ token })
  },
  setUser: (user) => set({ user }),
  logout: () => {
    localStorage.removeItem('authToken')
    set({ token: null, user: null })
  },
  isAuthenticated: () => !!get().token,
}))
