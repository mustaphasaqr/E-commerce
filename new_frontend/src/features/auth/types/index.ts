/**
 * Auth Types - Match backend API contracts exactly
 * Source: AuthController.java, UserController.java
 */

// ========== User ==========
export interface User {
  id: string
  username: string
  email: string
  role: 'CUSTOMER' | 'ADMIN' | 'OWNER'
  status: 'PENDING' | 'ACTIVE' | 'SUSPENDED' | 'DELETED'
  emailVerified: boolean
  marketingConsent?: boolean
  createdAt: string
  updatedAt: string
}

// ========== Login Formula ==========
export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number // milliseconds
  sessionId: string
  user: User
}

// ========== Logout Formula ==========
// No request body needed - uses Bearer token header
// Returns: 204 No Content

// ========== Refresh Token Formula ==========
export interface RefreshTokenRequest {
  refreshToken: string
}

export interface TokenResponse {
  accessToken: string
  expiresIn: number // milliseconds
}

// ========== Logout All Devices Formula ==========
// No request body needed - uses Bearer token header
// Returns: 204 No Content

// ========== Password Reset Request Formula ==========
export interface PasswordResetRequestRequest {
  email: string
}
// Returns: 204 No Content (silent - doesn't confirm if email exists)

// ========== Password Reset Complete Formula ==========
export interface PasswordResetCompleteRequest {
  token: string
  newPassword: string
}
// Returns: 204 No Content

// ========== Register Formula ==========
export interface RegisterRequest {
  email: string
  password: string
  username: string
  termsAccepted: boolean
}

// Registration now returns LoginResponse (see below)

// ========== Email Verification Formulas ==========
export interface RequestEmailVerificationRequest {
  email: string
}
// Returns: 204 No Content

export interface VerifyEmailWithTokenRequest {
  token: string
}
// Returns: 204 No Content

// ========== Auth Store State ==========
export interface AuthState {
  token: string | null
  refreshToken: string | null
  user: User | null
  sessionId: string | null
  expiresIn: number | null
  isAuthenticated: boolean
  isLoading: boolean
  error: string | null
}

// ========== API Error Response ==========
export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
}
