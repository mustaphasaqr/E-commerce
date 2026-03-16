import { useNavigate } from 'react-router-dom'
import { Navigation } from '@/shared/components'
import {
  HeroSection,
  FeaturedProductsSection,
  CategoriesSection,
  TestimonialSection,
  FooterSection,
} from '@/shared/components/sections'

/**
 * HomePage - Landing Page
 *
 * This is the main landing page displayed when users open the app.
 * NOT a login or registration page - that comes later when user chooses to authenticate.
 *
 * Entry Points to Authentication:
 * 1. Click Profile Icon (top right) → Show login/signup prompt
 * 2. Try to "Add to Cart" → Redirect to login
 * 3. Try to "Wishlist" → Redirect to login
 * 4. Try to "My Orders" → Redirect to login
 * 5. Try to "Write Review" → Redirect to login
 * 6. Try to "Settings" → Redirect to login
 *
 * Design Stack:
 * - Preline UI sections (Hero, Features, CTA blocks)
 * - shadcn/ui components (Button, Card, Dialog)
 * - Tailwind CSS responsive design
 * - Lucide Icons for visual hierarchy
 *
 * Layout:
 * - Navigation Bar (with profile icon)
 * - Hero Section (brand story + CTA)
 * - Featured Products Section (showcase popular items)
 * - Categories Section (browse by type)
 * - Testimonials Section (social proof)
 * - Footer Section (links + info)
 */
export function HomePage() {
  const navigate = useNavigate()

  const goToProductsSection = () => {
    const section = document.getElementById('products-section')
    if (section) {
      section.scrollIntoView({ behavior: 'smooth', block: 'start' })
      return
    }
    navigate('/')
  }

  return (
    <div className="min-h-screen bg-white">
      {/* Navigation Bar */}
      <Navigation />

      {/* Hero Section - Preline UI */}
      <HeroSection onGetStartedClick={goToProductsSection} />

      {/* Featured Products Section */}
      <FeaturedProductsSection />

      {/* Categories Section */}
      <CategoriesSection />

      {/* Testimonials / Social Proof Section */}
      <TestimonialSection />

      {/* Footer */}
      <FooterSection />
    </div>
  )
}

export default HomePage
