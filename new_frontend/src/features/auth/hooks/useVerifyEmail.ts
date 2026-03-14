import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { verifyEmail as verifyEmailService, resendVerificationEmail } from '../api/authService'
import { useAuthStore } from '@/shared/hooks/useAuthStore'
import type { LoginResponse } from '../types'

/**
 * useVerifyEmail Hook
 *
 * Handles email verification flow:
 * 1. Extract token from URL query params
 * 2. Call backend to verify email
 * 3. Auto-login user on success
 * 4. Redirect to home
 * 5. Handle resend verification email request
 */
export function useVerifyEmail(token: string | null) {
  const navigate = useNavigate()
  const { setToken, setUser } = useAuthStore()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [resendLoading, setResendLoading] = useState(false)
  const [resendSuccess, setResendSuccess] = useState(false)
  const [verificationComplete, setVerificationComplete] = useState(false)

  // Verify email on mount if token exists
  useEffect(() => {
    if (!token) {
      setError('No verification token provided')
      setLoading(false)
      return
    }

    verifyEmail()
  }, [token])

  const verifyEmail = async () => {
    if (!token) return

    setLoading(true)
    setError(null)

    try {
      const response: LoginResponse = await verifyEmailService(token)

      // Store auth token and user
      setToken(response.accessToken)
      setUser(response.user as any)

      // Mark verification as complete
      setVerificationComplete(true)

      // Redirect to home after 1.5 seconds
      setTimeout(() => {
        navigate('/')
      }, 1500)

      console.log('✅ Email verified successfully, auto-logged in')
    } catch (err) {
      let message = 'Email verification failed'

      if (err instanceof Error) {
        const axiosError = err as any
        if (axiosError.response?.data?.message) {
          message = axiosError.response.data.message
        } else if (axiosError.response?.data?.error) {
          message = axiosError.response.data.error
        } else {
          message = err.message
        }
      }

      setError(message)
      console.error('❌ Email verification error:', message)
    } finally {
      setLoading(false)
    }
  }

  const resendEmail = async (userEmail: string) => {
    setResendLoading(true)
    setResendSuccess(false)

    try {
      await resendVerificationEmail(userEmail)
      setResendSuccess(true)

      // Reset success message after 5 seconds
      setTimeout(() => {
        setResendSuccess(false)
      }, 5000)

      console.log('✅ Verification email resent to:', userEmail)
    } catch (err) {
      let message = 'Failed to resend email'

      if (err instanceof Error) {
        const axiosError = err as any
        if (axiosError.response?.data?.message) {
          message = axiosError.response.data.message
        } else {
          message = err.message
        }
      }

      setError(message)
      console.error('❌ Resend email error:', message)
    } finally {
      setResendLoading(false)
    }
  }

  return {
    loading,
    error,
    verificationComplete,
    resendLoading,
    resendSuccess,
    resendEmail,
  }
}
