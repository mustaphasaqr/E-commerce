import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, User, Lock, Eye, EyeOff, CheckCircle2, AlertCircle } from 'lucide-react'
import { useRegister } from '../hooks/useRegister'
import { registerSchema, type RegisterFormData } from '@/shared/validation/auth.schema'
import { Button, Input, Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/shared/components/ui'

interface RegisterFormProps {
  onRegisterSuccess?: () => void
}

/**
 * RegisterForm Component (shadcn/ui + Lucide + Preline)
 *
 * Design Stack:
 * - shadcn/ui components (Button, Input, Card)
 * - Lucide Icons (Mail, User, Lock, Eye, CheckCircle2)
 * - Tailwind CSS responsive design
 * - Radix UI primitives (accessible form inputs)
 * - Preline UI layout with gradient background
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
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

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
      <div className="min-h-screen bg-gradient-to-br from-green-50 to-emerald-100 flex items-center justify-center p-4">
        <div className="w-full max-w-md">
          <Card className="border-green-200 bg-white">
            <CardHeader className="text-center space-y-2">
              <div className="flex justify-center mb-4">
                <CheckCircle2 size={64} className="text-green-600" />
              </div>
              <CardTitle className="text-3xl text-green-700">Registration Successful</CardTitle>
              <p className="text-sm text-gray-600">Verify your email to get started</p>
            </CardHeader>

            <CardContent className="space-y-4">
              <div className="bg-green-50 border border-green-200 rounded-lg p-4 space-y-3">
                <div className="flex gap-3">
                  <span className="text-xl">📧</span>
                  <div>
                    <p className="font-medium text-sm text-green-900">Check your email</p>
                    <p className="text-sm text-green-700 mt-1">We sent a verification link to</p>
                    <p className="text-sm font-semibold text-green-900">{registerData.email}</p>
                  </div>
                </div>

                <div className="flex gap-3">
                  <span className="text-xl">⏱️</span>
                  <div>
                    <p className="font-medium text-sm text-green-900">Verification link expires</p>
                    <p className="text-sm text-green-700">in 24 hours</p>
                  </div>
                </div>

                <div className="flex gap-3">
                  <span className="text-xl">🔐</span>
                  <div>
                    <p className="font-medium text-sm text-green-900">After verifying</p>
                    <p className="text-sm text-green-700">You can login with your credentials</p>
                  </div>
                </div>
              </div>
            </CardContent>

            <CardFooter>
              <Button
                onClick={() => onRegisterSuccess?.()}
                variant="default"
                className="w-full h-11"
              >
                Go to Login
              </Button>
            </CardFooter>
          </Card>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <Card>
          <CardHeader className="space-y-2">
            <CardTitle className="text-center text-3xl">Create Account</CardTitle>
            <p className="text-center text-sm text-gray-600">Join us to start shopping</p>
          </CardHeader>

          <CardContent>
            {/* General Error Message */}
            {(registerError || generalError) && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                <AlertCircle size={20} className="text-red-600 flex-shrink-0 mt-0.5" />
                <div className="flex-1">
                  <p className="text-sm text-red-800 font-medium">Registration Failed</p>
                  <p className="text-sm text-red-700 mt-1">{registerError || generalError}</p>
                </div>
                <button
                  type="button"
                  onClick={() => setGeneralError(null)}
                  className="text-red-600 hover:text-red-800 text-lg font-bold"
                  aria-label="Dismiss error"
                >
                  ×
                </button>
              </div>
            )}

            <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
              {/* Email Input */}
              <div className="space-y-2">
                <label htmlFor="email" className="block text-sm font-medium text-gray-700">
                  Email Address
                </label>
                <Input
                  id="email"
                  type="email"
                  placeholder="you@example.com"
                  icon={<Mail size={18} />}
                  error={!!errors.email}
                  {...register('email')}
                  aria-label="Email address"
                  aria-describedby={errors.email ? 'email-error' : undefined}
                />
                {errors.email && (
                  <p id="email-error" className="text-sm text-red-600 font-medium">
                    {errors.email.message}
                  </p>
                )}
              </div>

              {/* Username Input */}
              <div className="space-y-2">
                <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                  Username
                </label>
                <Input
                  id="username"
                  type="text"
                  placeholder="johndoe"
                  icon={<User size={18} />}
                  error={!!errors.username}
                  {...register('username')}
                  aria-label="Username"
                  aria-describedby={errors.username ? 'username-error' : undefined}
                />
                {errors.username && (
                  <p id="username-error" className="text-sm text-red-600 font-medium">
                    {errors.username.message}
                  </p>
                )}
              </div>

              {/* Password Input */}
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                  Password
                </label>
                <div className="relative w-full">
                  <Input
                    id="password"
                    type={showPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    icon={<Lock size={18} />}
                    error={!!errors.password}
                    {...register('password')}
                    aria-label="Password"
                    aria-describedby={errors.password ? 'password-error' : 'password-requirements'}
                    className="pr-12"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    aria-label={showPassword ? 'Hide password' : 'Show password'}
                  >
                    {showPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {errors.password && (
                  <p id="password-error" className="text-sm text-red-600 font-medium">
                    {errors.password.message}
                  </p>
                )}

                {/* Password Requirements */}
                {password && (
                  <div id="password-requirements" className="space-y-1 mt-2 p-3 bg-gray-50 rounded-lg">
                    <div className={`flex items-center gap-2 text-xs ${password.length >= 8 ? 'text-green-600' : 'text-gray-500'}`}>
                      <CheckCircle2 size={14} />
                      <span>At least 8 characters</span>
                    </div>
                    <div className={`flex items-center gap-2 text-xs ${/[A-Z]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                      <CheckCircle2 size={14} />
                      <span>One uppercase letter</span>
                    </div>
                    <div className={`flex items-center gap-2 text-xs ${/[a-z]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                      <CheckCircle2 size={14} />
                      <span>One lowercase letter</span>
                    </div>
                    <div className={`flex items-center gap-2 text-xs ${/[0-9]/.test(password) ? 'text-green-600' : 'text-gray-500'}`}>
                      <CheckCircle2 size={14} />
                      <span>One digit</span>
                    </div>
                  </div>
                )}
              </div>

              {/* Confirm Password Input */}
              <div className="space-y-2">
                <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700">
                  Confirm Password
                </label>
                <div className="relative w-full">
                  <Input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    placeholder="••••••••"
                    icon={<Lock size={18} />}
                    error={!!errors.confirmPassword}
                    {...register('confirmPassword')}
                    aria-label="Confirm password"
                    aria-describedby={errors.confirmPassword ? 'confirmPassword-error' : undefined}
                    className="pr-12"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    aria-label={showConfirmPassword ? 'Hide confirm password' : 'Show confirm password'}
                  >
                    {showConfirmPassword ? <EyeOff size={18} /> : <Eye size={18} />}
                  </button>
                </div>
                {errors.confirmPassword && (
                  <p id="confirmPassword-error" className="text-sm text-red-600 font-medium">
                    {errors.confirmPassword.message}
                  </p>
                )}
              </div>

              {/* Terms Checkbox */}
              <div className="flex items-start gap-3 p-3 bg-gray-50 rounded-lg">
                <input
                  id="termsAccepted"
                  type="checkbox"
                  {...register('termsAccepted')}
                  className="mt-1 w-4 h-4 border-gray-300 rounded text-blue-600 focus:ring-2 focus:ring-blue-500 cursor-pointer"
                  aria-describedby={errors.termsAccepted ? 'terms-error' : undefined}
                />
                <label htmlFor="termsAccepted" className="text-sm text-gray-700 cursor-pointer">
                  I agree to the{' '}
                  <a href="#" className="text-blue-600 hover:text-blue-700 font-semibold transition">
                    Terms of Service
                  </a>
                  {' '}and{' '}
                  <a href="#" className="text-blue-600 hover:text-blue-700 font-semibold transition">
                    Privacy Policy
                  </a>
                </label>
              </div>
              {errors.termsAccepted && (
                <p id="terms-error" className="text-sm text-red-600 font-medium">
                  {errors.termsAccepted.message}
                </p>
              )}

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={loading}
                variant="default"
                className="w-full h-11 flex items-center justify-center gap-2 bg-green-600 hover:bg-green-700"
              >
                {loading ? (
                  <>
                    <span className="inline-block animate-spin">⏳</span>
                    <span>Creating account...</span>
                  </>
                ) : (
                  <span>Create Account</span>
                )}
              </Button>
            </form>
          </CardContent>

          <CardFooter className="flex-col space-y-4">
            {/* Login Link */}
            <p className="text-center text-sm text-gray-600 w-full">
              Already have an account?{' '}
              <a href="/login" className="text-blue-600 hover:text-blue-700 font-semibold transition">
                Login here
              </a>
            </p>

            {/* Axios Monitoring Info */}
            <p className="text-xs text-gray-500 text-center w-full border-t pt-4">
              💡 Open DevTools Console to see API logs (📤 Request, ✅ Success, ❌ Error)
            </p>
          </CardFooter>
        </Card>
      </div>
    </div>
  )
}
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
