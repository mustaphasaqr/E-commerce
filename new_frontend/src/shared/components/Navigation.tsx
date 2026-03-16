import { useState, useEffect, useRef } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Loader2 } from 'lucide-react'
import { ShoppingCart, User, Menu, X, LogOut, ChevronLeft, ArrowRight, Trash2 } from 'lucide-react'
import cartService from '@/features/cart/api/cartService'
import type { CartDTO } from '@/features/cart/types'

interface NavigationProps {}

interface LocalCartItem {
  productId: string
  productName: string
  quantity: number
  price: number
  subtotal: number
}

const LOCAL_DRAWER_CART_KEY = 'localDrawerCartItems'

const loadLocalDrawerCart = (): LocalCartItem[] => {
  if (typeof window === 'undefined' || !localStorage) {
    return []
  }

  try {
    const raw = localStorage.getItem(LOCAL_DRAWER_CART_KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

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
  const { isAuthenticated, logout, user } = useAuth()
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  const [profileMenuOpen, setProfileMenuOpen] = useState(false)
  const [showLogoutLoader, setShowLogoutLoader] = useState(false)
  const [isCartOpen, setIsCartOpen] = useState(false)
  const [isCartLoading, setIsCartLoading] = useState(false)
  const [isCartMutating, setIsCartMutating] = useState(false)
  const [cartError, setCartError] = useState<string | null>(null)
  const [localCartItems, setLocalCartItems] = useState<LocalCartItem[]>(loadLocalDrawerCart)
  const [cart, setCart] = useState<CartDTO>({
    id: null,
    userId: null,
    sessionId: '',
    items: [],
    totalAmount: 0,
    status: 'ACTIVE',
    totalItems: 0,
  })
  const profileMenuRef = useRef<HTMLDivElement>(null)

  const formatMoney = (value: number): string =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0)

  const localItemsCount = localCartItems.reduce((sum, item) => sum + item.quantity, 0)
  const localSubtotal = localCartItems.reduce((sum, item) => sum + item.subtotal, 0)
  const hasBackendItems = cart.items.length > 0
  const displayItemsCount = hasBackendItems ? cart.totalItems : localItemsCount
  const displaySubtotal = hasBackendItems ? cart.totalAmount : localSubtotal
  const canUseBackendCart = isAuthenticated

  const loadCart = async (openAfterLoad = false) => {
    if (!canUseBackendCart) {
      if (openAfterLoad) {
        setIsCartOpen(true)
      }
      setCartError(null)
      return
    }
    setIsCartLoading(true)
    setCartError(null)
    try {
      const current = await cartService.getCart()
      setCart(current)
      if (openAfterLoad) {
        setIsCartOpen(true)
      }
    } catch (err: any) {
      setCartError('Failed to load cart.')
      if (openAfterLoad) {
        setIsCartOpen(true)
      }
    } finally {
      setIsCartLoading(false)
    }
  }

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

  // Always close the profile dropdown when route changes.
  useEffect(() => {
    setProfileMenuOpen(false)
  }, [location.pathname])

  useEffect(() => {
    if (!isAuthenticated) {
      setCart({
        id: null,
        userId: null,
        sessionId: '',
        items: [],
        totalAmount: 0,
        status: 'ACTIVE',
        totalItems: 0,
      })
      setIsCartOpen(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    if (typeof window === 'undefined' || !localStorage) {
      return
    }

    localStorage.setItem(LOCAL_DRAWER_CART_KEY, JSON.stringify(localCartItems))
  }, [localCartItems])

  useEffect(() => {
    const onCartUpdated = (event: Event) => {
      const customEvent = event as CustomEvent<{
        open?: boolean
        localItem?: LocalCartItem
      }>

      if (customEvent.detail?.localItem) {
        const item = customEvent.detail.localItem
        setLocalCartItems((prev) => {
          const existing = prev.find((entry) => entry.productId === item.productId)
          if (!existing) {
            return [...prev, { ...item }]
          }

          return prev.map((entry) =>
            entry.productId === item.productId
              ? {
                  ...entry,
                  quantity: entry.quantity + item.quantity,
                  subtotal: (entry.quantity + item.quantity) * entry.price,
                }
              : entry
          )
        })
      }

      if (!customEvent.detail?.localItem && canUseBackendCart) {
        void loadCart(Boolean(customEvent.detail?.open))
      }

      if (customEvent.detail?.open) {
        setIsCartOpen(true)
      }
    }

    window.addEventListener('cart:updated', onCartUpdated)
    return () => {
      window.removeEventListener('cart:updated', onCartUpdated)
    }
  }, [canUseBackendCart, isAuthenticated])

  useEffect(() => {
    if (!isCartOpen) return
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = ''
    }
  }, [isCartOpen])

  // Check if we can go back
  const canGoBack = location.pathname !== '/'

  const handleProfileClick = () => {
    setProfileMenuOpen(!profileMenuOpen)
  }

  const goToProductsSection = () => {
    if (location.pathname !== '/') {
      navigate('/')
      setTimeout(() => {
        document.getElementById('products-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
      }, 50)
      return
    }

    document.getElementById('products-section')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  const openCart = () => {
    if (!isAuthenticated) {
      navigate('/login?redirect=cart')
      return
    }
    void loadCart(true)
  }

  const removeCartItem = async (productId: string) => {
    setCartError(null)

    if (hasBackendItems) {
      setIsCartMutating(true)
      try {
        const updated = await cartService.removeFromCart(productId)
        setCart(updated)
      } catch {
        setCartError('Failed to remove item. Please retry.')
      } finally {
        setIsCartMutating(false)
      }
      return
    }

    setLocalCartItems((prev) => prev.filter((item) => item.productId !== productId))
  }

  const handleLogout = async () => {
    await logout()
    setProfileMenuOpen(false)
    setShowLogoutLoader(true)
    setTimeout(() => {
      window.location.href = '/';
    }, 2000); // 2 seconds
  }

  return (
    <>
    <nav className="sticky top-0 z-50 bg-white border-b border-gray-200">
      {showLogoutLoader && (
        <div className="fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-white bg-opacity-90">
          <Loader2 size={64} className="animate-spin text-blue-600 mb-4" />
          <div className="text-lg font-semibold text-blue-700">Logging out...</div>
        </div>
      )}
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
            <button onClick={goToProductsSection} className="text-gray-600 hover:text-gray-900">
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
              onClick={openCart}
              className="p-2 hover:bg-gray-100 rounded-lg relative"
              aria-label="Shopping cart"
            >
              <ShoppingCart size={24} className="text-gray-600" />
              {displayItemsCount > 0 && (
                <span className="absolute top-1 right-1 flex h-5 w-5 items-center justify-center rounded-full bg-red-500 text-xs text-white">
                  {displayItemsCount > 99 ? '99+' : displayItemsCount}
                </span>
              )}
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
                <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-lg shadow-lg py-2 z-10">
                  {/* Admin/Owner menu */}
                  {user?.role === 'OWNER' && (
                    <>
                      <button
                        onClick={() => {
                          navigate('/admin')
                          setProfileMenuOpen(false)
                        }}
                        className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                      >
                        Commerce Insights
                      </button>
                      <button
                        onClick={() => {
                          navigate('/admin/orders')
                          setProfileMenuOpen(false)
                        }}
                        className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                      >
                        Orders
                      </button>
                      <button
                        onClick={() => {
                          navigate('/admin/users')
                          setProfileMenuOpen(false)
                        }}
                        className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                      >
                        Users
                      </button>
                      <hr className="my-2" />
                    </>
                  )}
                  {/* User menu */}
                  <button
                    onClick={() => {
                      navigate('/account?tab=account')
                      setProfileMenuOpen(false)
                    }}
                    className="w-full text-left px-4 py-2 hover:bg-gray-100 text-gray-700"
                  >
                    My Account
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
                goToProductsSection()
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
    {isCartOpen && (
      <>
        <button
          className="fixed inset-0 z-[70] bg-black/30"
          aria-label="Close cart drawer"
          onClick={() => setIsCartOpen(false)}
        />
        <aside className="fixed right-0 top-0 z-[80] h-full w-full max-w-md border-l border-gray-200 bg-white shadow-2xl">
          <div className="flex h-full flex-col">
            <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4">
              <h2 className="text-lg font-semibold text-gray-900">Your Cart</h2>
              <button
                onClick={() => setIsCartOpen(false)}
                className="rounded-md p-2 text-gray-500 hover:bg-gray-100 hover:text-gray-800"
                aria-label="Close"
              >
                <X size={20} />
              </button>
            </div>

            <div className="flex-1 overflow-y-auto px-5 py-4">
              {isCartLoading ? (
                <p className="text-sm text-gray-600">Loading cart...</p>
              ) : cartError ? (
                <p className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{cartError}</p>
              ) : !hasBackendItems && localCartItems.length === 0 ? (
                <div className="rounded-lg border border-dashed border-gray-300 p-6 text-center">
                  <p className="text-sm text-gray-700">Your cart is empty.</p>
                  <button
                    onClick={() => {
                      setIsCartOpen(false)
                      goToProductsSection()
                    }}
                    className="mt-3 text-sm font-semibold text-blue-700 hover:text-blue-800"
                  >
                    Browse products
                  </button>
                </div>
              ) : (
                <div className="space-y-3">
                  {(hasBackendItems
                    ? cart.items.map((item) => ({
                        productId: String(item.productId),
                        productName: item.productName,
                        quantity: item.quantity,
                        subtotal: item.subtotal,
                        price: item.price,
                        source: 'backend' as const,
                      }))
                    : localCartItems.map((item) => ({ ...item, source: 'local' as const }))
                  ).map((item) => (
                    <div key={`${item.productId}`} className="rounded-lg border border-gray-200 p-3">
                      <div className="flex items-start justify-between gap-2">
                        <p className="text-sm font-semibold text-gray-900">{item.productName}</p>
                        <button
                          onClick={() => void removeCartItem(item.productId)}
                          disabled={isCartMutating || isCartLoading}
                          className="rounded-md p-1 text-gray-400 transition hover:bg-rose-50 hover:text-rose-600 disabled:cursor-not-allowed"
                          aria-label="Remove item"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                      <div className="mt-1 flex items-center justify-between text-xs text-gray-600">
                        <span>Qty: {item.quantity}</span>
                        <span>{formatMoney(item.subtotal)}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

            <div className="border-t border-gray-200 p-5">
              <div className="mb-3 flex items-center justify-between text-sm text-gray-700">
                <span>Items</span>
                <span className="font-semibold text-gray-900">{displayItemsCount}</span>
              </div>
              <div className="mb-4 flex items-center justify-between text-base text-gray-900">
                <span>Subtotal</span>
                <span className="text-xl font-bold">{formatMoney(displaySubtotal)}</span>
              </div>
              <button
                onClick={() => {
                  setIsCartOpen(false)
                  navigate('/checkout')
                }}
                disabled={displayItemsCount === 0 || isCartLoading || isCartMutating || !hasBackendItems}
                className="flex w-full items-center justify-center gap-2 rounded-lg bg-blue-600 px-4 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-blue-300"
              >
                Checkout <ArrowRight size={16} />
              </button>
            </div>
          </div>
        </aside>
      </>
    )}
    </>
  )
}
