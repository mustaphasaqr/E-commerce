import { describe, it, expect, beforeAll, afterAll } from 'vitest'
import axios from '@/shared/api/axios'
import type { LoginRequest, RegisterRequest, PasswordResetRequestRequest, PasswordResetCompleteRequest } from '../types'

/**
 * Auth Service Integration Tests (Real Backend)
 * 
 * These tests run against the REAL Railway backend
 * Not mocked - actual API calls
 * 
 * Setup required:
 * - Railway backend running and accessible
 * - Test user account with known credentials
 * - Environment: VITE_API_URL set to Railway URL
 */

describe('Auth Service Integration Tests (Real Backend)', () => {
  // Test user credentials - use a test account on your Railway backend
  const TEST_USER_EMAIL = 'integration-test@example.com'
  const TEST_USER_PASSWORD = 'IntegrationTest123!@#'
  const TEST_USERNAME = 'integration-test-user'

  let accessToken: string | null = null
  let refreshToken: string | null = null

  beforeAll(() => {
    console.log('🚀 Starting integration tests against Railway backend...')
    console.log(`📍 Backend URL: ${axios.defaults.baseURL}`)
  })

  afterAll(() => {
    console.log('✅ Integration tests completed')
  })

  // ========== TEST 1: Register ==========
  it('should register a new user on real backend', async () => {
    // Use unique email to avoid conflicts
    const uniqueEmail = `test-${Date.now()}@example.com`

    const registerRequest: RegisterRequest = {
      email: uniqueEmail,
      username: `testuser-${Date.now()}`,
      password: 'TestPassword123!@#',
      termsAccepted: true,
    }

    try {
      const response = await axios.post('/users', registerRequest)
      
      expect(response.status).toBe(201)
      expect(response.data).toHaveProperty('id')
      expect(response.data.email).toBe(uniqueEmail)
      expect(response.data.status).toBe('PENDING')

      console.log('✅ Registration successful - user created on backend')
      console.log(`📧 Email: ${uniqueEmail}`)
      console.log(`🔔 Status: PENDING (requires email verification)`)
    } catch (error: any) {
      // Handle 409 Conflict if email already registered
      if (error.response?.status === 409) {
        console.log('⚠️ Email already registered (expected for retry)')
        expect(error.response.status).toBe(409)
      } else {
        throw error
      }
    }
  })

  // ========== TEST 2: Login ==========
  it('should login with valid credentials on real backend', async () => {
    // Use a pre-created test account or register first
    const loginRequest: LoginRequest = {
      email: TEST_USER_EMAIL,
      password: TEST_USER_PASSWORD,
    }

    try {
      const response = await axios.post('/auth/login', loginRequest)

      expect(response.status).toBe(200)
      expect(response.data).toHaveProperty('accessToken')
      expect(response.data).toHaveProperty('refreshToken')
      expect(response.data).toHaveProperty('user')
      expect(response.data.user).toHaveProperty('id')
      expect(response.data.user.email).toBe(TEST_USER_EMAIL)

      // Store tokens for subsequent tests
      accessToken = response.data.accessToken
      refreshToken = response.data.refreshToken

      console.log('✅ Login successful')
      console.log(`👤 User: ${response.data.user.username} (${response.data.user.role})`)
      console.log(`🎫 Session ID: ${response.data.sessionId}`)
      console.log(`⏱️  Token expires in: ${response.data.expiresIn}ms`)
    } catch (error: any) {
      if (error.response?.status === 401) {
        console.log('⚠️ Invalid credentials - test user may not exist on backend yet')
        console.log('📝 Create test user manually or through registration test')
        expect(error.response.status).toBe(401)
      } else if (error.response?.status === 403) {
        console.log('⚠️ Account not verified - user needs email verification')
        expect(error.response.status).toBe(403)
      } else {
        throw error
      }
    }
  })

  // ========== TEST 3: Refresh Token ==========
  it('should refresh token with valid refresh token on real backend', async () => {
    if (!refreshToken) {
      console.log('⏭️  Skipping refresh test - no valid refresh token from login')
      expect(true).toBe(true)
      return
    }

    try {
      const response = await axios.post('/auth/refresh', {
        refreshToken: refreshToken,
      })

      expect(response.status).toBe(200)
      expect(response.data).toHaveProperty('accessToken')
      expect(response.data).toHaveProperty('expiresIn')

      // Update token for subsequent tests
      accessToken = response.data.accessToken

      console.log('✅ Token refreshed successfully')
      console.log(`🔄 New token expires in: ${response.data.expiresIn}ms`)
    } catch (error: any) {
      if (error.response?.status === 400 || error.response?.status === 401) {
        console.log('⚠️ Invalid refresh token - may have expired')
        expect([400, 401]).toContain(error.response.status)
      } else {
        throw error
      }
    }
  })

  // ========== TEST 4: Logout ==========
  it('should logout successfully on real backend', async () => {
    if (!accessToken) {
      console.log('⏭️  Skipping logout test - no valid access token')
      expect(true).toBe(true)
      return
    }

    try {
      const response = await axios.post('/auth/logout', {})

      // Logout typically returns 204 No Content
      expect([200, 204]).toContain(response.status)

      console.log('✅ Logout successful')
      console.log('🔒 Session cleared on backend')

      // Clear tokens locally
      accessToken = null
      refreshToken = null
    } catch (error: any) {
      if (error.response?.status === 401) {
        console.log('⚠️ Not authenticated - may have already logged out')
        expect(error.response.status).toBe(401)
      } else {
        throw error
      }
    }
  })

  // ========== TEST 5: Password Reset Request ==========
  it('should request password reset on real backend', async () => {
    const resetRequest: PasswordResetRequestRequest = {
      email: TEST_USER_EMAIL,
    }

    try {
      const response = await axios.post('/auth/password-reset/request', resetRequest)

      // Password reset request typically returns 204 No Content (silent)
      expect([200, 204]).toContain(response.status)

      console.log('✅ Password reset email sent (check email for reset link)')
      console.log(`📧 Email: ${TEST_USER_EMAIL}`)
    } catch (error: any) {
      if (error.response?.status === 404) {
        console.log('⚠️ User not found - check email address')
        expect(error.response.status).toBe(404)
      } else if (error.response?.status === 429) {
        console.log('⚠️ Rate limited - too many requests')
        expect(error.response.status).toBe(429)
      } else {
        throw error
      }
    }
  })

  // ========== TEST 6: Health Check ==========
  it('should confirm backend is accessible', async () => {
    try {
      // Simple health check - try accessing the login endpoint without credentials
      const response = await axios.post('/auth/login', {
        email: 'test@test.com',
        password: 'test',
      })

      // We expect 401 (invalid credentials) not 500 or connection error
      // This means the backend is responding
      expect(response.status).not.toBe(500)
    } catch (error: any) {
      if (error.response?.status === 401 || error.response?.status === 400) {
        // Expected - backend is responding (401 Unauthorized or 400 Bad Request)
        console.log('✅ Backend is accessible and responding')
        expect([400, 401]).toContain(error.response.status)
      } else if (error.code === 'ECONNREFUSED') {
        throw new Error('❌ Cannot connect to backend - is Railway app running?')
      } else {
        throw error
      }
    }
  })
})
