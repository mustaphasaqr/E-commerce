import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, Lock, Loader2 } from 'lucide-react'
import { useLogin } from '../hooks/useLogin'
import { loginSchema, type LoginFormData } from '@/shared/validation/auth.schema'
import { Button, Input, Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/shared/components/ui'

interface LoginFormProps {
  onLoginSuccess?: () => void
}

/**
 * LoginForm Component (shadcn/ui + Lucide + Preline)
 *
 * Design Stack:
 * - shadcn/ui components (Button, Input, Card)
 * - Lucide Icons (Mail, Lock, Loader2)
 * - Tailwind CSS responsive design
 * - Radix UI primitives (accessible form inputs)
 * - Preline UI layout
 *
 * Formula: Same as test uses
 * - Form validates with Zod schema
 * - Calls useLogin hook (which calls service)
 * - Service returns LoginResponse (same formula as test expects)
 */
export function LoginForm({ onLoginSuccess }: LoginFormProps) {
  const { login, loading, error: loginError } = useLogin()
  const [generalError, setGeneralError] = useState<string | null>(null)

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onBlur',
  })

  const onSubmit = async (data: LoginFormData) => {
    setGeneralError(null)
    const result = await login(data)

    if (result) {
      onLoginSuccess?.()
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <Card>
          <CardHeader className="space-y-2">
            <CardTitle className="text-center text-3xl">Welcome Back</CardTitle>
            <p className="text-center text-sm text-gray-600">Sign in to your account to continue</p>
          </CardHeader>

          <CardContent>
            {/* General Error Message */}
            {(loginError || generalError) && (
              <div className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                <span className="text-red-600 text-lg">❌</span>
                <div className="flex-1">
                  <p className="text-sm text-red-800 font-medium">Login Failed</p>
                  <p className="text-sm text-red-700 mt-1">{loginError || generalError}</p>
                </div>
                <button
                  type="button"
                  onClick={() => setGeneralError(null)}
                  className="text-red-600 hover:text-red-800 text-xl font-bold"
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

              {/* Password Input */}
              <div className="space-y-2">
                <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                  Password
                </label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  icon={<Lock size={18} />}
                  error={!!errors.password}
                  {...register('password')}
                  aria-label="Password"
                  aria-describedby={errors.password ? 'password-error' : undefined}
                />
                {errors.password && (
                  <p id="password-error" className="text-sm text-red-600 font-medium">
                    {errors.password.message}
                  </p>
                )}
              </div>

              {/* Submit Button */}
              <Button
                type="submit"
                disabled={loading}
                variant="default"
                className="w-full h-11 flex items-center justify-center gap-2"
              >
                {loading ? (
                  <>
                    <Loader2 size={18} className="animate-spin" />
                    <span>Logging in...</span>
                  </>
                ) : (
                  <span>Sign In</span>
                )}
              </Button>
            </form>
          </CardContent>

          <CardFooter className="flex-col space-y-4">
            {/* Register Link */}
            <p className="text-center text-sm text-gray-600 w-full">
              Don't have an account?{' '}
              <a href="/register" className="text-blue-600 hover:text-blue-700 font-semibold transition">
                Register here
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
