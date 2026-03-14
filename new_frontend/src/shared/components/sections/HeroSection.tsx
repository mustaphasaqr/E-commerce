import { ArrowRight, Zap, Shield, Truck } from 'lucide-react'
import { Button } from '@/shared/components/ui'

interface HeroSectionProps {
  onGetStartedClick?: () => void
}

/**
 * Hero Section (Preline UI Style)
 *
 * Displays:
 * - Large headline
 * - Subheading
 * - CTA buttons (Browse Products, Learn More)
 * - Features badges (Fast delivery, Secure payment, Quality guarantee)
 * - Background gradient
 */
export function HeroSection({ onGetStartedClick }: HeroSectionProps) {
  return (
    <div className="relative bg-gradient-to-r from-blue-50 via-indigo-50 to-purple-50 overflow-hidden">
      {/* Decorative elements */}
      <div className="absolute top-0 left-0 -z-10 w-96 h-96 bg-blue-200 rounded-full opacity-30 blur-3xl -translate-x-1/2 -translate-y-1/2"></div>
      <div className="absolute bottom-0 right-0 -z-10 w-96 h-96 bg-purple-200 rounded-full opacity-30 blur-3xl translate-x-1/2 translate-y-1/2"></div>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {/* Left Content */}
          <div className="space-y-8">
            <div className="space-y-4">
              <h1 className="text-5xl lg:text-6xl font-bold text-gray-900 leading-tight">
                Discover Your <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">Perfect</span> Shopping Experience
              </h1>
              <p className="text-xl text-gray-600">
                Browse thousands of products from trusted sellers. Fast shipping, secure payments, and quality guaranteed.
              </p>
            </div>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4">
              <Button
                onClick={onGetStartedClick}
                className="bg-gradient-to-r from-blue-600 to-indigo-600 text-white px-8 py-3 rounded-lg font-semibold hover:shadow-lg transition flex items-center justify-center gap-2"
              >
                Browse Products
                <ArrowRight size={20} />
              </Button>
              <Button
                variant="outline"
                className="border-2 border-gray-300 text-gray-700 px-8 py-3 rounded-lg font-semibold hover:bg-gray-100 transition"
              >
                Learn More
              </Button>
            </div>

            {/* Trust Badges */}
            <div className="grid grid-cols-3 gap-4 pt-8">
              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                  <Truck className="text-blue-600" size={24} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Fast Shipping</p>
                  <p className="text-sm text-gray-600">Free delivery over $50</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-green-100 rounded-lg flex items-center justify-center">
                  <Shield className="text-green-600" size={24} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Secure Payment</p>
                  <p className="text-sm text-gray-600">100% protected</p>
                </div>
              </div>

              <div className="flex items-center gap-3">
                <div className="w-12 h-12 bg-purple-100 rounded-lg flex items-center justify-center">
                  <Zap className="text-purple-600" size={24} />
                </div>
                <div>
                  <p className="font-semibold text-gray-900">Quality Guarantee</p>
                  <p className="text-sm text-gray-600">30-day returns</p>
                </div>
              </div>
            </div>
          </div>

          {/* Right Image/Illustration */}
          <div className="hidden lg:block">
            <div className="relative w-full aspect-square bg-gradient-to-br from-blue-400 to-indigo-600 rounded-2xl shadow-2xl overflow-hidden">
              <div className="w-full h-full flex items-center justify-center text-white text-center p-8">
                <div className="space-y-4">
                  <div className="text-6xl">🛍️</div>
                  <h3 className="text-3xl font-bold">Coming Soon</h3>
                  <p className="text-lg opacity-90">Product imagery and featured promotions</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
