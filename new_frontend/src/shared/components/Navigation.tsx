import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { ShoppingCart, User, Menu, X, LogOut, ChevronLeft } from 'lucide-react'

interface NavigationProps {}

/**
 * Navigation Component (Preline UI + shadcn/ui)
 *
 * Displays:
 * - Brand logo
 * - Navigation links (Home, Products, About)
 * - Cart icon (links to /cart if authenticated)
 * - Profile icon dropdown (login/signup if not authenticated, profile menu if authenticated)
 * - Mobile menu
 *
 * Auth Flow:
 * - Profile icon click → If not authenticated, show login/signup prompt
 * - If authenticated → Show profile menu with My Orders, Settings, Logout
 */
export function Navigation({}: NavigationProps) {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated, logout } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)
  const profileMenuRef = useRef<HTMLDivElement>(null)

  // Close dropdown when clicking outside
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (profileMenuRef.current && !profileMenuRef.current.contains(event.target as Node)) {
        setProfileMenuOpen(false)
      }
    }

    if (profileMenuOpen) {
      document.addEventListener('click', handleClickOutside)
    }

    return () => {
      document.removeEventListener('click', handleClickOutside)
    }
  }, [profileMenuOpen])

  // Check if we can go back
  const canGoBack = location.pathname !== '/'

  const handleProfileClick = () => {
    if (!isAuthenticated) {
      setProfileMenuOpen(!profileMenuOpen)
    } else {
      navigate('/profile')
    }
  }

  const handleLogout = async () => {
    await logout()
    setProfileMenuOpen(false)
    navigate('/')
  }

  return (
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-200">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Left Side: Back Arrow + Logo */}
          <div className="flex items-center gap-3">
            {canGoBack && (
              <button
                onClick={() => window.history.back()}
                className="p-2 hover:bg-gray-100 rounded-lg transition"
                aria-label="Go back"
              >
                <ChevronLeft size={24} className="text-gray-600" />
              </button>
            )}
            <div className="flex-shrink-0 cursor-pointer" onClick={() => navigate('/')}>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
                ShopHub
              </h1>
            </div>
          </div>

          {/* Desktop Menu */}
          <div className="hidden md:flex items-center gap-8">
            <button onClick={() => navigate('/')} className="text-gray-600 hover:text-gray-900">
              Home
            </button>
            <button onClick={() => navigate('/products')} className="text-gray-600 hover:text-gray-900">
              Products
            </button>
            <a href="#" className="text-gray-600 hover:text-gray-900">
              About
            </a>
            <a href="#" className="text-gray-600 hover:text-gray-900">
              Contact
            </a>
          </div>

          {/* Right Side Icons */}
          <div className="flex items-center gap-4">
            {/* Cart Icon */}
            <button
              onClick={() => (isAuthenticated ? navigate('/cart') : navigate('/login?redirect=cart'))}
              className="p-2 hover:bg-gray-100 rounded-lg relative"
              aria-label="Shopping cart"
            >
              <ShoppingCart size={24} className="text-gray-600" />
              <span className="absolute top-1 right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                0
              </span>
            </button>

            {/* Profile Icon with Dropdown */}
            <div className="relative" ref={profileMenuRef}>
              <button
                onClick={handleProfileClick}
                className="p-2 hover:bg-gray-100 rounded-lg"
                aria-label="User profile"
              >
                <User size={24} className="text-gray-600" />
              </button>

              {/* Profile Dropdown Menu - Show when not authenticated */}
              {!isAuthenticated && profileMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-10">
                  <button
                    onClick={() => {
                      navigate('/login')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    Sign In
                  </button>
                  <button
                    onClick={() => {
                      navigate('/register')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    Create Account
                  </button>
                </div>
              )}

              {/* Profile Dropdown Menu - Show when authenticated */}
              {isAuthenticated && profileMenuOpen && (
                <div className="absolute right-0 mt-2 w-48 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-10">
                  <button
                    onClick={() => {
                      navigate('/profile')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    My Profile
                  </button>
                  <button
                    onClick={() => {
                      navigate('/orders')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    My Orders
                  </button>
                  <button
                    onClick={() => {
                      navigate('/profile')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    Settings
                  </button>
                  <hr className="my-2" />
                  <button
                    onClick={handleLogout}
                    className="w-full text-left px-4 py-2 hover:bg-red-50 text-red-600 flex items-center gap-2"
                  >
                    <LogOut size={16} />
                    Logout
                  </button>
                </div>
              )}
            </div>

            {/* Mobile Menu Button */}
            <button
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 hover:bg-gray-100 rounded-lg"
            >
              {mobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {mobileMenuOpen && (
          <div className="md:hidden border-t border-gray-200 py-4 space-y-2">
            <button
              onClick={() => {
                navigate('/')
                setMobileMenuOpen(false)
              }}
              className="block w-full text-left px-4 py-2 hover:bg-gray-100 rounded"
            >
              Home
            </button>
            <button
              onClick={() => {
                navigate('/products')
                setMobileMenuOpen(false)
              }}
              className="block w-full text-left px-4 py-2 hover:bg-gray-100 rounded"
            >
              Products
            </button>
            <button
              onClick={() => {
                navigate('/profile')
                setMobileMenuOpen(false)
              }}
              className="block w-full text-left px-4 py-2 hover:bg-gray-100 rounded"
            >
              Account
            </button>
          </div>
        )}
      </div>
    </nav>
  )
}
