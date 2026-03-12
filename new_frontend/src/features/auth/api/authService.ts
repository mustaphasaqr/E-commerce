import axios from '@/shared/api/axios'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RegisterResponse,
  RefreshTokenRequest,
  TokenResponse,
  PasswordResetRequestRequest,
  PasswordResetCompleteRequest,
  User,
} from '../types'

/**
 * Auth Service - API calls matching 6 backend formulas
 *
 * Uses same async/await pattern as tests use
 * Every call is logged by Axios interceptors (📤✅❌)
 *
 * Formulas:
 * 1. login(credentials) → Promise<LoginResponse>
 * 2. logout() → Promise<void>
 * 3. refreshToken(token) → Promise<TokenResponse>
 * 4. logoutAllDevices() → Promise<void>
 * 5. requestPasswordReset(email) → Promise<void>
 * 6. completePasswordReset(token, password) → Promise<void>
 */

// ========== FORMULA 1: Login ==========
/**
 * Login with email/password
 * POST /api/v1/auth/login
 * Returns: accessToken, refreshToken, user, sessionId
 */
export async function login(request: LoginRequest): Promise<LoginResponse> {
  const response = await axios.post<LoginResponse>('/auth/login', request)
  console.log('✅ Login successful')
  return response.data
}

// ========== FORMULA 2: Logout ==========
/**
 * Logout current session
 * POST /api/v1/auth/logout (requires Authorization header)
 * Returns: 204 No Content
 */
export async function logout(): Promise<void> {
  await axios.post('/auth/logout')
  console.log('✅ Logout successful')
}

// ========== FORMULA 3: Refresh Token ==========
/**
 * Get new access token using refresh token
 * POST /api/v1/auth/refresh
 * Returns: new accessToken, expiresIn
 */
export async function refreshToken(request: RefreshTokenRequest): Promise<TokenResponse> {
  const response = await axios.post<TokenResponse>('/auth/refresh', request)
  console.log('✅ Token refreshed')
  return response.data
}

// ========== FORMULA 4: Logout All Devices ==========
/**
 * Logout all sessions except current
 * POST /api/v1/auth/logout-all (requires Authorization header)
 * Returns: 204 No Content
 */
export async function logoutAllDevices(): Promise<void> {
  await axios.post('/auth/logout-all')
  console.log('✅ All devices logged out')
}

// ========== FORMULA 5: Request Password Reset ==========
/**
 * Request password reset email
 * POST /api/v1/auth/password-reset/request
 * Returns: 204 No Content (silent - doesn't confirm if email exists)
 */
export async function requestPasswordReset(
  request: PasswordResetRequestRequest
): Promise<void> {
  await axios.post('/auth/password-reset/request', request)
  console.log('✅ Password reset email sent')
}

// ========== FORMULA 6: Complete Password Reset ==========
/**
 * Complete password reset with token and new password
 * POST /api/v1/auth/password-reset/complete
 * Returns: 204 No Content
 */
export async function completePasswordReset(
  request: PasswordResetCompleteRequest
): Promise<void> {
  await axios.post('/auth/password-reset/complete', request)
  console.log('✅ Password reset complete')
}

// ========== BONUS: Register ==========
/**
 * Register new user
 * POST /api/v1/users
 * Returns: User with status PENDING
 */
export async function register(request: RegisterRequest): Promise<RegisterResponse> {
  const response = await axios.post<RegisterResponse>('/users', request)
  console.log('✅ Registration successful - check email to verify')
  return response.data
}

/**
 * Get current user
 * GET /api/v1/users/me (requires Authorization header)
 * Returns: Current user details
 */
export async function getCurrentUser(): Promise<User> {
  const response = await axios.get<User>('/users/me')
  return response.data
}
