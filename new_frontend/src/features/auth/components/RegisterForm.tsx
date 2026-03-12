import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useRegister } from '../hooks/useRegister'
import { registerSchema, type RegisterFormData } from '@/shared/validation/auth.schema'

interface RegisterFormProps {
  onRegisterSuccess?: () => void
}

/**
 * RegisterForm Component
 *
 * Features:
 * - Email/username/password/confirm password inputs
 * - Terms and conditions checkbox
 * - Password strength requirements displayed
 * - Field-level and general error messages
 * - Loading spinner while submitting
 * - Success message with next steps
 *
 * Formula: Same as test uses
 * - Form validates with Zod schema
 * - Calls useRegister hook (which calls service)
 * - Service returns RegisterResponse with status PENDING
 * - User must verify email before logging in
 */
export function RegisterForm({ onRegisterSuccess }: RegisterFormProps) {
  const { register: registerUser, loading, error: registerError, data: registerData } = useRegister()
  const [generalError, setGeneralError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
    watch,
  } = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: 'onBlur',
  })

  const password = watch('password')

  const onSubmit = async (data: RegisterFormData) => {
    setGeneralError(null)
    const result = await registerUser({
      email: data.email,
      username: data.username,
      password: data.password,
      termsAccepted: data.termsAccepted,
    })

    if (result) {
      onRegisterSuccess?.()
    }
  }

  // Show success state after registration
  if (registerData) {
    return (
      <div className="w-full max-w-md mx-auto p-6 bg-white rounded-lg shadow-md border border-green-200">
        <div className="text-center">
          <h2 className="text-2xl font-bold mb-2 text-green-700">✅ Registration Successful</h2>
          <p className="text-gray-600 mb-6">
            We've sent a verification email to <span className="font-semibold">{registerData.email}</span>
          </p>

          <div className="bg-green-50 border border-green-200 rounded p-4 mb-6 text-sm text-green-800 space-y-2">
            <p>📧 <strong>Check your email:</strong> Click the verification link</p>
            <p>⏱️ <strong>Link expires in 24 hours</strong></p>
            <p>🔐 <strong>After verifying:</strong> You can login with your credentials</p>
          </div>

          <button
            onClick={() => onRegisterSuccess?.()}
            className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-semibold transition"
          >
            Go to Login
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="w-full max-w-md mx-auto p-6 bg-white rounded-lg shadow-md border border-gray-200">
      <h2 className="text-2xl font-bold mb-6 text-gray-900">Create Account</h2>

      {/* General Error Message */}
      {(registerError || generalError) && (
        <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded text-red-800 text-sm">
          <span className="font-semibold">❌ Error:</span> {registerError || generalError}
          <button
            type="button"
            onClick={() => setGeneralError(null)}
            className="ml-2 text-red-600 hover:text-red-800 underline text-xs"
          >
            Dismiss
          </button>
        </div>
      )}

      <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
        {/* Email Input */}
        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-1">
            Email Address
          </label>
          <input
            id="email"
            type="email"
            placeholder="you@example.com"
            {...register('email')}
            className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
              errors.email ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
            }`}
            aria-describedby={errors.email ? 'email-error' : undefined}
          />
          {errors.email && (
            <p id="email-error" className="mt-1 text-sm text-red-600">
              {errors.email.message}
            </p>
          )}
        </div>

        {/* Username Input */}
        <div>
          <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-1">
            Username
          </label>
          <input
            id="username"
            type="text"
            placeholder="johndoe"
            {...register('username')}
            className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
              errors.username ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
            }`}
            aria-describedby={errors.username ? 'username-error' : undefined}
          />
          {errors.username && (
            <p id="username-error" className="mt-1 text-sm text-red-600">
              {errors.username.message}
            </p>
          )}
        </div>

        {/* Password Input */}
        <div>
          <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-1">
            Password
          </label>
          <input
            id="password"
            type="password"
            placeholder="••••••••"
            {...register('password')}
            className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
              errors.password ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
            }`}
            aria-describedby={errors.password ? 'password-error' : 'password-requirements'}
          />
          {errors.password && (
            <p id="password-error" className="mt-1 text-sm text-red-600">
              {errors.password.message}
            </p>
          )}

          {/* Password Requirements */}
          {password && (
            <div id="password-requirements" className="mt-2 text-xs space-y-1">
              <p className={password.length >= 8 ? 'text-green-600' : 'text-gray-500'}>
                {password.length >= 8 ? '✓' : '○'} At least 8 characters
              </p>
              <p className={/[A-Z]/.test(password) ? 'text-green-600' : 'text-gray-500'}>
                {/[A-Z]/.test(password) ? '✓' : '○'} One uppercase letter
              </p>
              <p className={/[a-z]/.test(password) ? 'text-green-600' : 'text-gray-500'}>
                {/[a-z]/.test(password) ? '✓' : '○'} One lowercase letter
              </p>
              <p className={/[0-9]/.test(password) ? 'text-green-600' : 'text-gray-500'}>
                {/[0-9]/.test(password) ? '✓' : '○'} One digit
              </p>
              <p className={/[!@#$%^&*(),.?":{}|<>]/.test(password) ? 'text-green-600' : 'text-gray-500'}>
                {/[!@#$%^&*(),.?":{}|<>]/.test(password) ? '✓' : '○'} One special character
              </p>
            </div>
          )}
        </div>

        {/* Confirm Password Input */}
        <div>
          <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-1">
            Confirm Password
          </label>
          <input
            id="confirmPassword"
            type="password"
            placeholder="••••••••"
            {...register('confirmPassword')}
            className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
              errors.confirmPassword ? 'border-red-500 focus:ring-red-500' : 'border-gray-300 focus:ring-blue-500'
            }`}
            aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
          />
          {errors.confirmPassword && (
            <p id="confirmPassword-error" className="mt-1 text-sm text-red-600">
              {errors.confirmPassword.message}
            </p>
          )}
        </div>

        {/* Terms Checkbox */}
        <div className="flex items-start gap-2">
          <input
            id="termsAccepted"
            type="checkbox"
            {...register('termsAccepted')}
            className="mt-1 w-4 h-4 border-gray-300 rounded text-blue-600 focus:ring-blue-500"
            aria-describedby={errors.termsAccepted ? 'terms-error' : undefined}
          />
          <label htmlFor="termsAccepted" className="text-sm text-gray-700">
            I agree to the <a href="#" className="text-blue-600 hover:underline">Terms of Service</a> and{' '}
            <a href="#" className="text-blue-600 hover:underline">Privacy Policy</a>
          </label>
        </div>
        {errors.termsAccepted && (
          <p id="terms-error" className="text-sm text-red-600">
            {errors.termsAccepted.message}
          </p>
        )}

        {/* Submit Button */}
        <button
          type="submit"
          disabled={loading}
          className={`w-full px-4 py-2 rounded-lg font-semibold text-white transition flex items-center justify-center gap-2 ${
            loading
              ? 'bg-gray-400 cursor-not-allowed'
              : 'bg-green-600 hover:bg-green-700 active:bg-green-800'
          }`}
        >
          {loading ? (
            <>
              <span className="inline-block w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></span>
              Creating account...
            </>
          ) : (
            '🆕 Create Account'
          )}
        </button>
      </form>

      {/* Login Link */}
      <p className="mt-6 text-center text-sm text-gray-600">
        Already have an account?{' '}
        <a href="/login" className="text-blue-600 hover:text-blue-700 font-semibold">
          Login here
        </a>
      </p>

      {/* Axios Monitoring Info */}
      <p className="mt-4 text-xs text-gray-500 text-center">
        💡 Tip: Open DevTools Console to see API logs (📤✅❌)
      </p>
    </div>
  )
}
