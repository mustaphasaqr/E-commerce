import React, { useState } from 'react'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { useNavigate } from 'react-router-dom'
import { Mail, Lock, Loader2, Zap, Shield, Headphones } from 'lucide-react'
import { useLogin } from '../hooks/useLogin'
import { Navigation } from '@/shared/components'
import { loginSchema, type LoginFormData } from '@/shared/validation/auth.schema'
import { Button, Input, Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/shared/components/ui'

interface LoginFormProps {
  onLoginSuccess?: () => void
}

/**
 * LoginForm Component (shadcn/ui + Lucide + Preline UI Sections)
 *
 * Design Stack:
 * - shadcn/ui components (Button, Input, Card)
 * - Lucide Icons (Mail, Lock, Loader2, Zap, Shield, Headphones)
 * - Tailwind CSS responsive design with gradients
 * - Radix UI primitives (accessible form inputs)
 * - Preline UI layout sections (header, hero, footer)
 *
 * Auto-Login Flow:
 * - User enters email and password
 * - useLogin hook calls login API
 * - API returns token and user data
 * - Hook automatically stores in auth store
 * - Redirect to previous page or home
 */
export function LoginForm({ onLoginSuccess }: LoginFormProps) {
  const navigate = useNavigate()
  const { login, loading, error: loginError, fieldErrors, retryAfter, rateLimitedEmail, clearRateLimitState } = useLogin?.() ?? {}
  const [generalError, setGeneralError] = useState<string | null>(null)
  const [localFieldErrors, setLocalFieldErrors] = useState<{ email?: string; password?: string }>({})

  const {
    register,
    handleSubmit,
    setError,
    formState: { errors },
    resetField,
  } = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    mode: 'onBlur',
  })

  const registrationSuggestion =
    "<span style=\"color:#2563eb;\">If you don't have an account, you can <a href=\"/register\" class=\"text-blue-600 hover:text-blue-800 underline font-semibold\">create one here</a>.</span>";

  // Sync local field errors with hook fieldErrors
  React.useEffect(() => {
    setLocalFieldErrors(fieldErrors)
  }, [fieldErrors])

  const onSubmit = async (data: LoginFormData) => {
    // Do not clear generalError here; only clear on input change
    const result = await login(data)

    if (!result) {
      // Always show the registration suggestion for any login failure except rate limit
      let errorMsg = loginError || 'Login failed. Please try again.'
      if (errorMsg.toLowerCase().includes('too many failed')) {
        // Get the current email input value
        const emailInput = (document.getElementById('email') as HTMLInputElement)?.value?.trim()?.toLowerCase() || ''
        if (!rateLimitedEmail || (rateLimitedEmail && rateLimitedEmail.toLowerCase() === emailInput)) {
          setGeneralError(errorMsg)
        } else {
          // If another user is rate limited, show generic error with registration suggestion
          setGeneralError('Login failed. Please try again.<br/>' + registrationSuggestion)
        }
      } else {
        // Always append the registration suggestion
        setGeneralError(
          'Login failed. Please try again.<br/>' + registrationSuggestion
        )
      }
      return
    }

    if (result) {
      setGeneralError(null)
      onLoginSuccess?.()
      // Redirect to previous page or home
      const redirectPath = sessionStorage.getItem('redirectAfterAuth') || '/'
      sessionStorage.removeItem('redirectAfterAuth')
      navigate(redirectPath)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-50 via-blue-50 to-indigo-50">
      {/* Navigation with Back Arrow */}
      <Navigation />

      {/* Main Content - Two Column Layout */}
      <div className="min-h-[calc(100vh-80px)]">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-0">
          {/* Left: Hero Section with Features (Preline Style) */}
          <div className="hidden lg:flex flex-col justify-center px-12 bg-gradient-to-br from-blue-600 via-indigo-600 to-purple-700 text-white py-12">
            <div className="max-w-md space-y-8">
              <div>
                <h2 className="text-4xl font-bold mb-4 leading-tight">
                  Welcome to your store
                </h2>
                <p className="text-lg text-blue-100">
                  Manage inventory, process orders, and grow your business with our powerful platform.
                </p>
              </div>

              {/* Feature List */}
              <div className="space-y-4">
                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Zap size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Real-time Analytics</h3>
                    <p className="text-sm text-blue-100">Track sales and customer behavior instantly</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Shield size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Secure Payments</h3>
                    <p className="text-sm text-blue-100">Process orders safely with industry-standard security</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Headphones size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">24/7 Support</h3>
                    <p className="text-sm text-blue-100">Get help whenever you need it from our support team</p>
                  </div>
                </div>
              </div>

              {/* Trust Badge */}
              <div className="pt-8 border-t border-white/20">
                <p className="text-sm text-blue-100 mb-3">Trusted by 10,000+ merchants worldwide</p>
                <div className="flex gap-2">
                  <div className="w-10 h-10 rounded-full bg-white/10 border border-white/20"></div>
                  <div className="w-10 h-10 rounded-full bg-white/10 border border-white/20"></div>
                  <div className="w-10 h-10 rounded-full bg-white/10 border border-white/20"></div>
                  <div className="w-10 h-10 rounded-full bg-white/10 border border-white/20 flex items-center justify-center text-xs font-bold">+</div>
                </div>
              </div>
            </div>
          </div>

          {/* Right: Login Form */}
          <div className="flex flex-col justify-center px-6 lg:px-12 py-12 max-w-md mx-auto w-full">
            <Card className="border-gray-200 shadow-lg">
              <CardHeader className="space-y-2 pb-6">
                <CardTitle className="text-center text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                  Welcome Back
                </CardTitle>
                <p className="text-center text-sm text-gray-600">
                  Sign in to your account to continue shopping
                </p>
              </CardHeader>

              <CardContent className="space-y-4">
                {/* General Error Message */}
                {generalError && (
                  <div className="p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3">
                    <span className="text-red-600 text-lg flex-shrink-0">❌</span>
                    <div className="flex-1">
                      <p className="text-sm text-red-800 font-medium">Login Failed</p>
                      <p className="text-sm mt-1" style={{ color: generalError.toLowerCase().includes('too many failed') ? '#b91c1c' : undefined }} dangerouslySetInnerHTML={{ __html: generalError }} />
                      {generalError.toLowerCase().includes('too many failed') && (
                        <p className="text-sm text-orange-700 mt-2 font-semibold">
                          You have been rate limited.{' '}
                          {retryAfter && retryAfter > 0
                            ? `Please wait ${Math.ceil(retryAfter / 60) > 1 ? Math.ceil(retryAfter / 60) + ' minutes' : retryAfter + ' seconds'} before trying again.`
                            : 'Please wait a few minutes before trying again.'}
                        </p>
                      )}
                    </div>
                    <button
                      type="button"
                      onClick={() => setGeneralError(null)}
                      className="text-red-600 hover:text-red-800 text-xl font-bold flex-shrink-0"
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
                        error={!!errors.email || !!localFieldErrors.email}
                        {...register('email', {
                          onChange: () => {
                            setGeneralError(null)
                            setLocalFieldErrors({})
                            if (typeof clearRateLimitState === 'function') clearRateLimitState();
                          },
                        })}
                        onFocus={() => {
                          setGeneralError(null)
                          setLocalFieldErrors({})
                          if (typeof clearRateLimitState === 'function') clearRateLimitState();
                        }}
                        autoComplete="username"
                        aria-label="Email address"
                        aria-describedby={errors.email ? 'email-error' : undefined}
                      />
                    {(errors.email || localFieldErrors.email) && (
                      <p
                        id="email-error"
                        className="text-sm text-red-600 font-medium"
                        dangerouslySetInnerHTML={{ __html: ((errors.email?.message || localFieldErrors.email) || '').replace(/\n/g, '<br/>') }}
                      />
                    )}
                  </div>

                  {/* Password Input */}
                  <div className="space-y-2">
                    <div className="flex items-center justify-between">
                      <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                        Password
                      </label>
                      <a href="#" className="text-xs text-blue-600 hover:text-blue-700 font-medium">
                        Forgot?
                      </a>
                    </div>
                    <Input
                      id="password"
                      type="password"
                      placeholder="••••••••"
                      icon={<Lock size={18} />}
                      error={!!errors.password || !!localFieldErrors.password}
                      {...register('password', {
                        onChange: () => {
                          setGeneralError(null)
                          setLocalFieldErrors({})
                        },
                      })}
                      onFocus={() => {
                        setGeneralError(null)
                        setLocalFieldErrors({})
                      }}
                      autoComplete="current-password"
                      aria-label="Password"
                      aria-describedby={errors.password ? 'password-error' : undefined}
                    />
                    {(errors.password || localFieldErrors.password) && (
                      <p
                        id="password-error"
                        className="text-sm text-red-600 font-medium"
                        dangerouslySetInnerHTML={{ __html: ((errors.password?.message || localFieldErrors.password) || '').replace(/\n/g, '<br/>') }}
                      />
                    )}
                  </div>

                  {/* Submit Button */}
                  <Button
                    type="submit"
                    disabled={Boolean(loading || (generalError && generalError.toLowerCase().includes('too many failed')))}
                    className="w-full h-11 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold"
                  >
                    {loading ? (
                      <div className="flex items-center justify-center gap-2">
                        <Loader2 size={18} className="animate-spin" />
                        <span>Signing in...</span>
                      </div>
                    ) : (
                      'Sign In'
                    )}
                  </Button>
                </form>
              </CardContent>

              <CardFooter className="flex-col space-y-4 pt-4 border-t">
                {/* Register Link */}
                <p className="text-center text-sm text-gray-600 w-full">
                  Don't have an account?{' '}
                  <a href="/register" className="text-blue-600 hover:text-blue-700 font-semibold">
                    Create one
                  </a>
                </p>

                {/* Divider */}
                <div className="relative">
                  <div className="absolute inset-0 flex items-center">
                    <div className="w-full border-t border-gray-200"></div>
                  </div>
                  <div className="relative flex justify-center text-xs uppercase">
                    <span className="px-2 bg-white text-gray-500">Or continue with</span>
                  </div>
                </div>

                {/* Social Login Buttons */}
                <div className="grid grid-cols-2 gap-3">
                  <button className="py-2 px-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium text-sm">
                    Google
                  </button>
                  <button className="py-2 px-3 border border-gray-300 rounded-lg hover:bg-gray-50 transition font-medium text-sm">
                    GitHub
                  </button>
                </div>

                {/* Help Text */}
                <p className="text-xs text-gray-500 text-center w-full">
                  💡 Open DevTools Console to see API logs (📤 Request, ✅ Success, ❌ Error)
                </p>
              </CardFooter>
            </Card>
          </div>
        </div>
      </div>

      {/* Preline Footer Section */}
      <footer className="bg-gray-900 text-gray-400 py-12 px-6 mt-12">
        <div className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-4 gap-8">
          <div>
            <h3 className="text-white font-bold mb-4">ShopHub</h3>
            <p className="text-sm">Premium e-commerce platform for modern merchants.</p>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3">Product</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white transition">Features</a></li>
              <li><a href="#" className="hover:text-white transition">Pricing</a></li>
              <li><a href="#" className="hover:text-white transition">Status</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3">Company</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white transition">About</a></li>
              <li><a href="#" className="hover:text-white transition">Blog</a></li>
              <li><a href="#" className="hover:text-white transition">Careers</a></li>
            </ul>
          </div>
          <div>
            <h4 className="text-white font-semibold mb-3">Legal</h4>
            <ul className="space-y-2 text-sm">
              <li><a href="#" className="hover:text-white transition">Privacy</a></li>
              <li><a href="#" className="hover:text-white transition">Terms</a></li>
              <li><a href="#" className="hover:text-white transition">Contact</a></li>
            </ul>
          </div>
        </div>
        <div className="max-w-6xl mx-auto border-t border-gray-800 mt-8 pt-8 flex items-center justify-between text-sm">
          <p>© 2026 ShopHub. All rights reserved.</p>
          <div className="flex gap-4">
            <a href="#" className="hover:text-white transition">Twitter</a>
            <a href="#" className="hover:text-white transition">LinkedIn</a>
            <a href="#" className="hover:text-white transition">GitHub</a>
          </div>
        </div>
      </footer>
    </div>
  )
}
