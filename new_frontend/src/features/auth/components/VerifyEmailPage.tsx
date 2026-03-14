import { useSearchParams } from 'react-router-dom'
import { CheckCircle2, AlertCircle, Mail, Loader2 } from 'lucide-react'
import { useVerifyEmail } from '../hooks/useVerifyEmail'
import { Navigation } from '@/shared/components'
import { Button } from '@/shared/components/ui'
import { useState } from 'react'

/**
 * VerifyEmailPage Component
 *
 * Shows during email verification process:
 * 1. Loading spinner while verifying token
 * 2. Success message with auto-redirect
 * 3. Error message with resend option
 *
 * Flow:
 * - User clicks email link: /verify-email?token=abc123
 * - This page extracts token from URL
 * - Shows "Verifying..." spinner
 * - Calls backend to verify and auto-login
 * - Redirects to home on success
 */
export function VerifyEmailPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token')
  const [resendEmail, setResendEmail] = useState('')
  const [showResendForm, setShowResendForm] = useState(false)

  const { loading, error, verificationComplete, resendLoading, resendSuccess, resendEmail: handleResend } = useVerifyEmail(
    token
  )

  const onResendClick = async () => {
    if (!resendEmail.trim()) {
      return
    }
    await handleResend(resendEmail)
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 via-blue-50 to-indigo-50">
      {/* Navigation */}
      <Navigation />

      {/* Main Content */}
      <div className="flex flex-col justify-center items-center min-h-[calc(100vh-80px)] px-6 py-12">
        <div className="max-w-md w-full">
          {/* Loading State */}
          {loading && (
            <div className="text-center space-y-6 py-12">
              <div className="flex justify-center">
                <Loader2 size={64} className="text-blue-600 animate-spin" />
              </div>
              <div>
                <h1 className="text-2xl font-bold text-gray-900 mb-2">Verifying your email...</h1>
                <p className="text-gray-600">Please wait while we verify your account</p>
              </div>
            </div>
          )}

          {/* Success State */}
          {verificationComplete && !error && (
            <div className="space-y-6 py-12">
              <div className="text-center space-y-4">
                <div className="flex justify-center animate-bounce">
                  <CheckCircle2 size={64} className="text-green-600" />
                </div>
                <div>
                  <h1 className="text-3xl font-bold text-green-700 mb-2">✅ Email Verified!</h1>
                  <p className="text-gray-700 text-lg">Your account is now active</p>
                </div>
              </div>

              <div className="bg-green-50 border border-green-200 rounded-lg p-6 space-y-3 text-center">
                <p className="text-green-900 font-semibold">🎉 Welcome to ShopHub!</p>
                <p className="text-green-800 text-sm">You are logged in and will be redirected to the home page...</p>
              </div>
            </div>
          )}

          {/* Error State */}
          {error && !loading && (
            <div className="space-y-6 py-12">
              <div className="text-center space-y-4">
                <div className="flex justify-center">
                  <AlertCircle size={64} className="text-red-600" />
                </div>
                <div>
                  <h1 className="text-2xl font-bold text-red-700 mb-2">Verification Failed</h1>
                  <p className="text-gray-600 text-sm">{error}</p>
                </div>
              </div>

              {/* Resend Email Section */}
              <div className="bg-red-50 border border-red-200 rounded-lg p-6 space-y-4">
                <div className="flex gap-2 text-red-900">
                  <Mail size={20} />
                  <div>
                    <p className="font-semibold">Didn't get an email?</p>
                    <p className="text-sm">We can send you a new verification link</p>
                  </div>
                </div>

                {!showResendForm ? (
                  <Button
                    onClick={() => setShowResendForm(true)}
                    variant="default"
                    className="w-full h-10 bg-blue-600 hover:bg-blue-700"
                    disabled={resendLoading}
                  >
                    {resendLoading ? 'Sending...' : 'Resend Verification Email'}
                  </Button>
                ) : (
                  <div className="space-y-3">
                    <input
                      type="email"
                      placeholder="Enter your email address"
                      value={resendEmail}
                      onChange={(e) => setResendEmail(e.target.value)}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                    <div className="flex gap-2">
                      <Button
                        onClick={onResendClick}
                        variant="default"
                        className="flex-1 h-10 bg-blue-600 hover:bg-blue-700"
                        disabled={resendLoading || !resendEmail.trim()}
                      >
                        {resendLoading ? 'Sending...' : 'Send'}
                      </Button>
                      <Button
                        onClick={() => {
                          setShowResendForm(false)
                          setResendEmail('')
                        }}
                        variant="outline"
                        className="flex-1 h-10"
                      >
                        Cancel
                      </Button>
                    </div>
                  </div>
                )}

                {resendSuccess && (
                  <div className="bg-green-50 border border-green-200 rounded p-3 text-green-800 text-sm">
                    ✅ Verification email sent! Check your inbox.
                  </div>
                )}
              </div>

              {/* Go Back Link */}
              <div className="text-center">
                <p className="text-gray-600 text-sm">
                  Wants to register again?{' '}
                  <a href="/register" className="text-blue-600 hover:text-blue-700 font-semibold">
                    Sign up
                  </a>
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
