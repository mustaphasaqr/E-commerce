import { useAuthStore as useAuthStoreZustand } from '../store/authSlice'

/**
 * useAuthStore Wrapper
 * Re-exports Zustand store for cleaner imports in hooks
 *
 * Usage: const { token, user, logout } = useAuthStore()
 */
export const useAuthStore = useAuthStoreZustand
