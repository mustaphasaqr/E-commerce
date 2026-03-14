import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import { Mail, User, Lock, Eye, EyeOff, CheckCircle2, AlertCircle, Zap, Shield, Headphones } from 'lucide-react'
import { useRegister } from '../hooks/useRegister'
import { Navigation } from '@/shared/components'
import { registerSchema, type RegisterFormData } from '@/shared/validation/auth.schema'
import { Button, Input, Card, CardHeader, CardTitle, CardContent, CardFooter } from '@/shared/components/ui'

interface RegisterFormProps {
  onRegisterSuccess?: () => void
}

/**
 * RegisterForm Component (shadcn/ui + Lucide + Preline UI Sections)
 *
 * Design Stack:
 * - shadcn/ui components (Button, Input, Card)
 * - Lucide Icons (Mail, User, Lock, Eye, CheckCircle2, etc.)
 * - Tailwind CSS responsive design with gradients
 * - Radix UI primitives (accessible form inputs)
 * - Preline UI layout sections (header, hero, footer)
 *
 * Registration Flow:
 * - User signs up with email, username, password
 * - Backend creates user and sends verification email
 * - User must verify email via link before login
 * - After verification, user can login with credentials
 */
export function RegisterForm({ onRegisterSuccess }: RegisterFormProps) {
  const { register: registerUser, loading, error: registerError, data: registerData } = useRegister()
  const [generalError, setGeneralError] = useState<string | null>(null)
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const navigate = useNavigate();

  // Fix: Always call hooks in the same order, useEffect outside conditional
  useEffect(() => {
    if (registerData) {
      const timer = setTimeout(() => {
        navigate("/");
      }, 3000);
      return () => clearTimeout(timer);
    }
  }, [registerData, navigate]);

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
      <div className="min-h-screen bg-gradient-to-b from-slate-50 via-green-50 to-emerald-50">
        {/* Preline Header Section */}
        <header className="py-8 px-6 border-b border-gray-200 bg-white/80 backdrop-blur">
          <div className="max-w-6xl mx-auto flex items-center justify-between">
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-green-600 to-emerald-600 bg-clip-text text-transparent">
                ShopHub
              </h1>
              <p className="text-xs text-gray-500 mt-1">Premium E-commerce Platform</p>
            </div>
            <nav className="hidden md:flex gap-8 text-sm text-gray-600">
              <a href="#" className="hover:text-gray-900 transition">Features</a>
              <a href="#" className="hover:text-gray-900 transition">Pricing</a>
              <a href="#" className="hover:text-gray-900 transition">Support</a>
            </nav>
          </div>
        </header>

        {/* Success Content */}
        <div className="flex flex-col justify-center items-center min-h-[calc(100vh-200px)] px-6 py-12">
          <div className="max-w-md w-full">
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
                      <p className="text-sm font-semibold text-green-900">{registerData.user.email}</p>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <span className="text-xl">🔗</span>
                    <div>
                      <p className="font-medium text-sm text-green-900">Click the link</p>
                      <p className="text-sm text-green-700">to activate your account</p>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <span className="text-xl">✅</span>
                    <div>
                      <p className="font-medium text-sm text-green-900">You will be automatically logged in</p>
                      <p className="text-sm text-green-700">after verification</p>
                    </div>
                  </div>

                  <div className="flex gap-3">
                    <span className="text-xl">⏱️</span>
                    <div>
                      <p className="font-medium text-sm text-green-900">Verification link expires</p>
                      <p className="text-sm text-green-700">in 24 hours</p>
                    </div>
                  </div>
                </div>
              </CardContent>

              <CardFooter className="flex flex-col gap-3">
                <p className="text-center text-sm text-gray-600 w-full">
                  Please check your email and click the verification link to activate your account.
                </p>
              </CardFooter>
            </Card>
          </div>
        </div>

        {/* Preline Footer Section */}
        <footer className="bg-gray-900 text-gray-400 py-12 px-6">
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
                  Join thousands of sellers
                </h2>
                <p className="text-lg text-blue-100">
                  Start your e-commerce journey today. No credit card required.
                </p>
              </div>

              {/* Feature List */}
              <div className="space-y-4">
                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Zap size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Quick Setup</h3>
                    <p className="text-sm text-blue-100">Get your store running in minutes</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Shield size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Enterprise Security</h3>
                    <p className="text-sm text-blue-100">Your data is encrypted and protected</p>
                  </div>
                </div>

                <div className="flex gap-3 items-start">
                  <div className="flex-shrink-0 w-6 h-6 rounded-full bg-white/20 flex items-center justify-center mt-1">
                    <Headphones size={16} className="text-white" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Dedicated Support</h3>
                    <p className="text-sm text-blue-100">Expert help throughout your journey</p>
                  </div>
                </div>
              </div>

              {/* CTA */}
              <div className="pt-8 border-t border-white/20">
                <p className="text-sm text-blue-100">💳 No credit card required to start</p>
              </div>
            </div>
          </div>

          {/* Right: Register Form */}
          <div className="flex flex-col justify-center px-6 lg:px-12 py-12 max-w-md mx-auto w-full">
            <Card className="border-gray-200 shadow-lg">
              <CardHeader className="space-y-2 pb-6">
                <CardTitle className="text-center text-3xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                  Create Account
                </CardTitle>
                <p className="text-center text-sm text-gray-600">
                  Join us to start selling and managing your store
                </p>
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
                      className="text-red-600 hover:text-red-800 text-lg font-bold flex-shrink-0"
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
                    className="w-full h-11 bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 text-white font-semibold"
                  >
                    {loading ? (
                      <>
                        <span className="inline-block animate-spin mr-2">⏳</span>
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
