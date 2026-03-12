// Global types
export interface User {
  id: string
  email: string
  name: string
  role: 'user' | 'admin'
}

export interface AuthResponse {
  token: string
  user: User
}

export interface ApiError {
  message: string
  code?: string
  details?: Record<string, unknown>
}

export interface ApiResponse<T> {
  data: T
  message?: string
  timestamp?: string
}
