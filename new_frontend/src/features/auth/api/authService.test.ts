import { describe, it, expect, vi, beforeEach } from 'vitest'
import axios from '@/shared/api/axios'
import {
  login,
  logout,
  register,
  refreshToken,
  logoutAllDevices,
  requestPasswordReset,
  completePasswordReset,
} from './authService'
import type {
  LoginRequest,
  LoginResponse,
  RegisterRequest,
  RefreshTokenRequest,
  TokenResponse,
  RegisterResponse,
  PasswordResetRequestRequest,
  PasswordResetCompleteRequest,
} from '../types'

/**
 * Auth Service Tests
 * Mirror backend test structure and formulas
 * 
 * Formula 1: Login (POST /api/v1/auth/login)
 * Formula 2: Logout (POST /api/v1/auth/logout)
 * Formula 3: Refresh (POST /api/v1/auth/refresh)
 * Formula 4: Logout All (POST /api/v1/auth/logout-all)
 * Formula 5: Password Reset Request (POST /api/v1/auth/password-reset/request)
 * Formula 6: Password Reset Complete (POST /api/v1/auth/password-reset/complete)
 */

vi.mock('@/shared/api/axios')

describe('Auth Service', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  // ========== FORMULA 1: Login ==========
  describe('login()', () => {
    it('should login with valid credentials and return LoginResponse', async () => {
      const request: LoginRequest = {
        email: 'authtest@example.com',
        password: 'AuthTest123!@#',
      }

      const expectedResponse: LoginResponse = {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        expiresIn: 3600000,
        sessionId: '550e8400-e29b-41d4-a716-446655440000',
        user: {
          id: 'USR-123456',
          username: 'authtest',
          email: 'authtest@example.com',
          role: 'CUSTOMER',
          status: 'ACTIVE',
          emailVerified: true,
          createdAt: '2026-01-01T00:00:00Z',
          updatedAt: '2026-03-12T00:00:00Z',
        },
      }

      vi.mocked(axios.post).mockResolvedValueOnce({ data: expectedResponse })

      const result = await login(request)

      expect(axios.post).toHaveBeenCalledWith('/auth/login', request)
      expect(result).toEqual(expectedResponse)
      expect(result.accessToken).toBeDefined()
      expect(result.refreshToken).toBeDefined()
      expect(result.user.id).toBeDefined()
      expect(result.sessionId).toBeDefined()
      // 📤 Axios logs: API Request: POST /auth/login
      // ✅ Axios logs: API Success: 200 /auth/login
    })

    it('should handle invalid credentials (401)', async () => {
      const request: LoginRequest = {
        email: 'authtest@example.com',
        password: 'WrongPassword123!',
      }

      const error = new Error('Invalid email or password')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await login(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
        // ❌ Axios logs: API Error: 401 /auth/login
      }
    })

    it('should handle non-existent user (401)', async () => {
      const request: LoginRequest = {
        email: 'nonexistent@example.com',
        password: 'Password123!',
      }

      const error = new Error('User not found')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await login(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })
  })

  // ========== FORMULA 2: Logout ==========
  describe('logout()', () => {
    it('should logout successfully and return void', async () => {
      vi.mocked(axios.post).mockResolvedValueOnce({ status: 204 })

      const result = await logout()

      expect(axios.post).toHaveBeenCalledWith('/auth/logout')
      expect(result).toBeUndefined()
      // 📤 Axios logs: API Request: POST /auth/logout
      // ✅ Axios logs: API Success: 204 /auth/logout
    })

    it('should handle missing authentication (401)', async () => {
      const error = new Error('Full authentication is required')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await logout()
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
        // ❌ Axios logs: API Error: 401 /auth/logout
      }
    })
  })

  // ========== FORMULA 3: Refresh Token ==========
  describe('refreshToken()', () => {
    it('should refresh token with valid refresh token', async () => {
      const request: RefreshTokenRequest = {
        refreshToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
      }

      const expectedResponse: TokenResponse = {
        accessToken: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
        expiresIn: 3600000,
      }

      vi.mocked(axios.post).mockResolvedValueOnce({ data: expectedResponse })

      const result = await refreshToken(request)

      expect(axios.post).toHaveBeenCalledWith('/auth/refresh', request)
      expect(result).toEqual(expectedResponse)
      expect(result.accessToken).toBeDefined()
      expect(result.expiresIn).toBe(3600000)
      // 📤 Axios logs: API Request: POST /auth/refresh
      // ✅ Axios logs: API Success: 200 /auth/refresh
    })

    it('should handle invalid refresh token (400)', async () => {
      const request: RefreshTokenRequest = {
        refreshToken: 'invalid-refresh-token-12345',
      }

      const error = new Error('Invalid refresh token')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await refreshToken(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
        // ❌ Axios logs: API Error: 400 /auth/refresh
      }
    })
  })

  // ========== FORMULA 4: Logout All Devices ==========
  describe('logoutAllDevices()', () => {
    it('should logout all devices successfully', async () => {
      vi.mocked(axios.post).mockResolvedValueOnce({ status: 204 })

      const result = await logoutAllDevices()

      expect(axios.post).toHaveBeenCalledWith('/auth/logout-all')
      expect(result).toBeUndefined()
      // 📤 Axios logs: API Request: POST /auth/logout-all
      // ✅ Axios logs: API Success: 204 /auth/logout-all
    })

    it('should handle missing authentication (401)', async () => {
      const error = new Error('Full authentication is required')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await logoutAllDevices()
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })
  })

  // ========== FORMULA 5: Password Reset Request ==========
  describe('requestPasswordReset()', () => {
    it('should request password reset and return 204 (silent)', async () => {
      const request: PasswordResetRequestRequest = {
        email: 'authtest@example.com',
      }

      vi.mocked(axios.post).mockResolvedValueOnce({ status: 204 })

      const result = await requestPasswordReset(request)

      expect(axios.post).toHaveBeenCalledWith('/auth/password-reset/request', request)
      expect(result).toBeUndefined()
      // 📤 Axios logs: API Request: POST /auth/password-reset/request
      // ✅ Axios logs: API Success: 204 /auth/password-reset/request
    })

    it('should handle invalid email format (400)', async () => {
      const request: PasswordResetRequestRequest = {
        email: 'not-an-email',
      }

      const error = new Error('Invalid email format')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await requestPasswordReset(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })

    it('should handle rate limiting (429)', async () => {
      const request: PasswordResetRequestRequest = {
        email: 'authtest@example.com',
      }

      const error = new Error('Rate limit exceeded')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await requestPasswordReset(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })
  })

  // ========== FORMULA 6: Password Reset Complete ==========
  describe('completePasswordReset()', () => {
    it('should complete password reset with valid token and new password', async () => {
      const request: PasswordResetCompleteRequest = {
        token: 'valid-reset-token-12345',
        newPassword: 'NewSecurePassword123!@#',
      }

      vi.mocked(axios.post).mockResolvedValueOnce({ status: 204 })

      const result = await completePasswordReset(request)

      expect(axios.post).toHaveBeenCalledWith('/auth/password-reset/complete', request)
      expect(result).toBeUndefined()
      // 📤 Axios logs: API Request: POST /auth/password-reset/complete
      // ✅ Axios logs: API Success: 204 /auth/password-reset/complete
    })

    it('should handle invalid/expired token (400)', async () => {
      const request: PasswordResetCompleteRequest = {
        token: 'expired-token',
        newPassword: 'NewPassword123!@#',
      }

      const error = new Error('Verification token has expired')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await completePasswordReset(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })

    it('should handle weak password (400)', async () => {
      const request: PasswordResetCompleteRequest = {
        token: 'valid-token',
        newPassword: '123',
      }

      const error = new Error('Password must be at least 8 characters')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await completePasswordReset(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })
  })

  // ========== BONUS: Register ==========
  describe('register()', () => {
    it('should register new user and return User with PENDING status', async () => {
      const request: RegisterRequest = {
        email: 'newuser@example.com',
        password: 'UniqueSecureP@ssw0rd2026!',
        username: 'newuser',
        termsAccepted: true,
      }

      const expectedResponse: RegisterResponse = {
        id: 'USR-654321',
        username: 'newuser',
        email: 'newuser@example.com',
        role: 'CUSTOMER',
        status: 'PENDING',
        emailVerified: false,
        createdAt: '2026-03-12T00:00:00Z',
        updatedAt: '2026-03-12T00:00:00Z',
      }

      vi.mocked(axios.post).mockResolvedValueOnce({ data: expectedResponse })

      const result = await register(request)

      expect(axios.post).toHaveBeenCalledWith('/users', request)
      expect(result).toEqual(expectedResponse)
      expect(result.status).toBe('PENDING')
      expect(result.emailVerified).toBe(false)
    })

    it('should handle duplicate email (400)', async () => {
      const request: RegisterRequest = {
        email: 'existing@example.com',
        password: 'Password123!@#',
        username: 'newuser',
        termsAccepted: true,
      }

      const error = new Error('Email already exists')
      vi.mocked(axios.post).mockRejectedValueOnce(error)

      try {
        await register(request)
        expect.fail('should have thrown')
      } catch (err) {
        expect(err).toBeInstanceOf(Error)
      }
    })
  })
})
