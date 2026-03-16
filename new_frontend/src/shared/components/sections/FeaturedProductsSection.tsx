import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/features/auth/hooks/useAuth'
import { Flame, Heart, MessageCircle, Search, ShoppingCart, Sparkles, Star, X } from 'lucide-react'
import { Button } from '@/shared/components/ui'
import productService from '@/features/products/api/productService'
import cartService from '@/features/cart/api/cartService'
import type { ProductDetail, ProductListItem, ProductRecommendation, ProductReviewStats, ProductReviewsPage } from '@/features/products/types'

interface FeaturedProductsSectionProps {}

interface DisplayProduct {
  id: string
  name: string
  price: number
  originalPrice?: number
  isActive: boolean
  availableStock: number
  image: string
  badge?: string | null
  rating: number
  reviews: number
}

const isBackendProductId = (id: string): boolean => id.trim().length > 0 && !id.startsWith('demo-')

/**
 * Featured Products Section (Preline UI Style)
 *
 * Displays:
 * - Grid of backend products
 * - Product card with image, rating, price
 * - "Add to Cart" button → requires auth
 * - "Wishlist" button → requires auth
 * - Real recommendation/review API integrations when available
 */
export function FeaturedProductsSection({}: FeaturedProductsSectionProps) {
  const navigate = useNavigate()
  const { isAuthenticated, user } = useAuth()
  const [wishlistItems, setWishlistItems] = useState<string[]>([])
  const [products, setProducts] = useState<DisplayProduct[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [busyProductId, setBusyProductId] = useState<string | null>(null)
  const [searchTerm, setSearchTerm] = useState('')
  const [stockFilter, setStockFilter] = useState<'all' | 'in-stock' | 'low-stock'>('all')
  const [trending, setTrending] = useState<ProductRecommendation[]>([])
  const [forYou, setForYou] = useState<ProductRecommendation[]>([])
  const [selectedProduct, setSelectedProduct] = useState<DisplayProduct | null>(null)
  const [selectedProductDetail, setSelectedProductDetail] = useState<ProductDetail | null>(null)
  const [productReviews, setProductReviews] = useState<ProductReviewsPage | null>(null)
  const [reviewStats, setReviewStats] = useState<ProductReviewStats | null>(null)
  const [frequentlyBought, setFrequentlyBought] = useState<ProductRecommendation[]>([])
  const [isDetailsLoading, setIsDetailsLoading] = useState(false)
  const [ownerName, setOwnerName] = useState('')
  const [ownerDescription, setOwnerDescription] = useState('')
  const [ownerPrice, setOwnerPrice] = useState('')
  const [ownerCurrency, setOwnerCurrency] = useState('USD')
  const [ownerActionStatus, setOwnerActionStatus] = useState<string | null>(null)
  const [ownerActionError, setOwnerActionError] = useState<string | null>(null)
  const [isOwnerBusy, setIsOwnerBusy] = useState(false)

  useEffect(() => {
    const loadFeatured = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const list = await productService.listProducts()
        const isOwner = user?.role === 'OWNER'
        const visibleList = isOwner ? list : list.filter((item) => item.active)

        if (visibleList.length === 0) {
          setProducts([])
          setError(isOwner
            ? 'No backend products found.'
            : 'No active backend products found. Activate products first to enable cart and quick-view actions.')
          return
        }

        const mapped: DisplayProduct[] = visibleList.map((item: ProductListItem) => ({
          id: item.id,
          name: item.name,
          price: item.price,
          isActive: item.active,
          availableStock: item.availableStock,
          image: getEmojiForProduct(item.name),
          badge: !item.active ? 'Inactive' : item.availableStock <= 3 ? 'Low Stock' : null,
          rating: 4.0,
          reviews: Math.max(item.availableStock, 1),
        }))
        setProducts(mapped)
      } catch {
        setError('Products API unavailable. Unable to load backend products.')
        setProducts([])
      } finally {
        setIsLoading(false)
      }
    }

    void loadFeatured()
  }, [user?.role])

  useEffect(() => {
    const loadRecommendations = async () => {
      try {
        const [trendingRecommendations, forYouRecommendations] = await Promise.all([
          productService.getTrendingProducts(6),
          isAuthenticated ? productService.getPersonalizedRecommendations(6) : Promise.resolve([]),
        ])

        setTrending(trendingRecommendations)
        setForYou(forYouRecommendations)
      } catch {
        setTrending([])
        setForYou([])
      }
    }

    void loadRecommendations()
  }, [isAuthenticated])

  const syncDisplayProduct = (detail: ProductDetail) => {
    setProducts((prev) =>
      prev.map((item) =>
        item.id === detail.id
          ? {
              ...item,
              name: detail.name,
              price: detail.price,
              availableStock: detail.availableStock,
              badge: detail.availableStock <= 3 ? 'Low Stock' : null,
            }
          : item
      )
    )
  }

  const getEmojiForProduct = (name: string): string => {
    const source = name.toLowerCase()
    if (source.includes('headphone')) return '🎧'
    if (source.includes('watch')) return '⌚'
    if (source.includes('power') || source.includes('battery')) return '🔋'
    if (source.includes('cable')) return '🔌'
    if (source.includes('charger')) return '⚡'
    if (source.includes('phone')) return '📱'
    if (source.includes('laptop')) return '💻'
    if (source.includes('keyboard')) return '⌨️'
    return '🛍️'
  }

  const openQuickView = async (product: DisplayProduct) => {
    setSelectedProduct(product)
    setOwnerActionStatus(null)
    setOwnerActionError(null)
    setOwnerName(product.name)
    setOwnerDescription('')
    setOwnerPrice(String(product.price))
    setOwnerCurrency('USD')
    // Clear stale detail immediately so owner controls do not render from a previously selected product.
    setSelectedProductDetail(null)
    setProductReviews(null)
    setReviewStats(null)
    setFrequentlyBought([])

    const canUseProductDetailsApi = isBackendProductId(product.id)
    if (!canUseProductDetailsApi) {
      setIsDetailsLoading(false)
      return
    }

    setIsDetailsLoading(true)
    try {
      const details = await productService.getProductById(product.id)
      setSelectedProductDetail(details)
      setOwnerName(details.name)
      setOwnerDescription(details.description ?? '')
      setOwnerPrice(String(details.price))
      setOwnerCurrency(details.currency || 'USD')

      try {
        const [stats, reviews, frequentlyBoughtItems] = await Promise.all([
          productService.getProductReviewStats(product.id),
          productService.getProductReviews(product.id, 0, 5, 'MOST_HELPFUL'),
          productService.getFrequentlyBoughtTogether(product.id, 5),
        ])
        setReviewStats(stats)
        setProductReviews(reviews)
        setFrequentlyBought(frequentlyBoughtItems)
      } catch {
        setReviewStats(null)
        setProductReviews(null)
        setFrequentlyBought([])
      }
    } catch {
      setSelectedProductDetail(null)
      setReviewStats(null)
      setProductReviews(null)
      setFrequentlyBought([])
    } finally {
      setIsDetailsLoading(false)
    }
  }

  const refreshQuickViewDetail = async (id: string) => {
    try {
      const details = await productService.getProductById(id)
      setSelectedProductDetail(details)
      setOwnerName(details.name)
      setOwnerDescription(details.description ?? '')
      setOwnerPrice(String(details.price))
      setOwnerCurrency(details.currency || 'USD')
      syncDisplayProduct(details)
      setSelectedProduct((prev) => (prev ? { ...prev, name: details.name, price: details.price, availableStock: details.availableStock } : prev))
    } catch {
      // Keep current quick view state if refresh fails.
    }
  }

  const runOwnerAction = async (action: () => Promise<void>, successMessage: string) => {
    setOwnerActionError(null)
    setOwnerActionStatus(null)
    setIsOwnerBusy(true)
    try {
      await action()
      setOwnerActionStatus(successMessage)
      if (selectedProduct) {
        await refreshQuickViewDetail(selectedProduct.id)
      }
    } catch (error) {
      const statusCode = (error as { response?: { status?: number } })?.response?.status
      const backendMessage = (error as { response?: { data?: { message?: string } } })?.response?.data?.message
      const normalizedMessage = (backendMessage || '').toLowerCase()
      const looksLikeDiscontinuedConflict =
        statusCode === 409 &&
        (normalizedMessage.includes('invalid product state') ||
          normalizedMessage.includes('discontinued') ||
          normalizedMessage.includes('prod_conflict_003'))

      if (statusCode === 409) {
        if (selectedProductDetail?.discontinued || looksLikeDiscontinuedConflict) {
          setOwnerActionError('This product is discontinued. Activate and image upload are blocked by backend rules.')
        } else {
          setOwnerActionError(backendMessage || 'Action conflicts with current product state.')
        }
      } else {
        setOwnerActionError('Owner action failed. Please retry.')
      }
    } finally {
      setIsOwnerBusy(false)
    }
  }

  const handleAddToCart = async (product: DisplayProduct) => {
    if (!isAuthenticated) {
      navigate('/login?redirect=add-to-cart')
      return
    }

    setBusyProductId(product.id)
    try {
      await cartService.addToCart({ productId: product.id, quantity: 1 })
      setSelectedProduct(null)
      window.dispatchEvent(new CustomEvent('cart:updated', { detail: { open: true } }))
    } catch {
      setError('Failed to add item to cart. Please retry.')
    } finally {
      setBusyProductId(null)
    }
  }

  const toggleWishlist = (productId: string) => {
    if (!isAuthenticated) {
      navigate('/login?redirect=wishlist')
      return
    }
    // Toggle wishlist
    setWishlistItems((prev) =>
      prev.includes(productId) ? prev.filter((id) => id !== productId) : [...prev, productId]
    )
  }

  const filteredProducts = products.filter((product) => {
    const matchesSearch = product.name.toLowerCase().includes(searchTerm.trim().toLowerCase())
    const matchesStock =
      stockFilter === 'all' ||
      (stockFilter === 'in-stock' && product.availableStock > 3) ||
      (stockFilter === 'low-stock' && product.availableStock > 0 && product.availableStock <= 3)

    return matchesSearch && matchesStock
  })

  const selectedProductSupportsOwnerActions = Boolean(
    selectedProduct && isBackendProductId(selectedProduct.id)
  )
  const isOwnerUser = user?.role === 'OWNER'

  const getRecommendationEmoji = (name: string): string => getEmojiForProduct(name)

  return (
    <section id="products-section" className="bg-white py-20">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Section Header */}
        <div className="text-center space-y-4 mb-16">
          <h2 className="text-4xl font-bold text-gray-900">Products</h2>
          <p className="text-xl text-gray-600">
            Explore all available products and add directly to cart
          </p>
        </div>

        <div className="mb-8 rounded-2xl border border-sky-100 bg-gradient-to-r from-sky-50 via-white to-amber-50 p-4 shadow-sm sm:p-5">
          <div className="grid gap-3 md:grid-cols-[1fr_220px]">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
              <input
                value={searchTerm}
                onChange={(event) => setSearchTerm(event.target.value)}
                placeholder="Search products on Home..."
                className="h-11 w-full rounded-xl border border-slate-200 bg-white pl-10 pr-3 text-sm text-slate-700 outline-none ring-sky-200 transition focus:ring"
              />
            </div>
            <select
              value={stockFilter}
              onChange={(event) => setStockFilter(event.target.value as 'all' | 'in-stock' | 'low-stock')}
              className="h-11 rounded-xl border border-slate-200 bg-white px-3 text-sm text-slate-700 outline-none ring-sky-200 transition focus:ring"
            >
              <option value="all">All stock</option>
              <option value="in-stock">In stock</option>
              <option value="low-stock">Low stock</option>
            </select>
          </div>
        </div>

        {trending.length > 0 && (
          <div className="mb-8 rounded-2xl border border-orange-100 bg-orange-50/70 p-4 sm:p-5">
            <div className="mb-3 flex items-center gap-2 text-orange-700">
              <Flame className="h-4 w-4" />
              <h3 className="text-sm font-bold uppercase tracking-wide">Trending now</h3>
            </div>
            <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {trending.map((item) => (
                <button
                  key={`trend-${item.productId}`}
                  onClick={() => setSearchTerm(item.productName)}
                  className="flex items-center justify-between rounded-xl border border-orange-200 bg-white px-3 py-2 text-left transition hover:-translate-y-0.5 hover:shadow"
                >
                  <span className="mr-3 text-xl">{getRecommendationEmoji(item.productName)}</span>
                  <span className="line-clamp-1 flex-1 text-sm font-medium text-slate-700">{item.productName}</span>
                  <span className="ml-2 text-xs font-semibold text-orange-700">${item.price}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {isAuthenticated && forYou.length > 0 && (
          <div className="mb-10 rounded-2xl border border-emerald-100 bg-emerald-50/70 p-4 sm:p-5">
            <div className="mb-3 flex items-center gap-2 text-emerald-700">
              <Sparkles className="h-4 w-4" />
              <h3 className="text-sm font-bold uppercase tracking-wide">Recommended for you</h3>
            </div>
            <div className="grid gap-2 sm:grid-cols-2 lg:grid-cols-3">
              {forYou.map((item) => (
                <button
                  key={`for-you-${item.productId}`}
                  onClick={() => setSearchTerm(item.productName)}
                  className="flex items-center justify-between rounded-xl border border-emerald-200 bg-white px-3 py-2 text-left transition hover:-translate-y-0.5 hover:shadow"
                >
                  <span className="mr-3 text-xl">{getRecommendationEmoji(item.productName)}</span>
                  <span className="line-clamp-1 flex-1 text-sm font-medium text-slate-700">{item.productName}</span>
                  <span className="ml-2 text-xs font-semibold text-emerald-700">${item.price}</span>
                </button>
              ))}
            </div>
          </div>
        )}

        {error && (
          <div className="mb-6 rounded-md border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
            {error}
          </div>
        )}

        {/* Products Grid */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          {isLoading ? (
            <div className="col-span-full text-center text-sm text-gray-600">Loading featured products...</div>
          ) : filteredProducts.length === 0 ? (
            <div className="col-span-full rounded-lg border border-dashed border-gray-300 p-8 text-center text-sm text-gray-600">
              No matching products. Try another search keyword.
            </div>
          ) : filteredProducts.map((product) => (
            <div
              key={product.id}
              className="group bg-white border border-gray-200 rounded-xl shadow-sm hover:shadow-xl transition overflow-hidden"
            >
              {/* Product Image */}
              <div className="relative bg-gradient-to-br from-gray-100 to-gray-200 aspect-square flex items-center justify-center overflow-hidden">
                <span className="text-7xl group-hover:scale-110 transition duration-300">
                  {product.image}
                </span>

                {/* Badge */}
                {product.badge && (
                  <div className="absolute top-4 left-4 bg-red-500 text-white px-3 py-1 rounded-full text-sm font-semibold">
                    {product.badge}
                  </div>
                )}

                {/* Wishlist Button */}
                <button
                  onClick={() => toggleWishlist(product.id)}
                  className="absolute top-4 right-4 bg-white/80 hover:bg-white p-2 rounded-full shadow-md transition"
                  aria-label="Add to wishlist"
                >
                  <Heart
                    size={20}
                    className={
                      wishlistItems.includes(product.id) ? 'fill-red-500 text-red-500' : 'text-gray-600'
                    }
                  />
                </button>

                {/* Discount Badge */}
                  {product.isActive && product.availableStock > 0 && (
                  <div className="absolute bottom-4 right-4 bg-green-500 text-white px-3 py-1 rounded-full text-sm font-semibold">
                    In stock
                  </div>
                )}
              </div>

              {/* Product Info */}
              <div className="p-6 space-y-4">
                <button onClick={() => void openQuickView(product)} className="cursor-pointer text-left">
                  <h3 className="text-lg font-semibold text-gray-900 group-hover:text-blue-600 transition">
                    {product.name}
                  </h3>
                </button>

                {/* Rating */}
                <div className="flex items-center gap-2">
                  <div className="flex items-center gap-1">
                    {[...Array(5)].map((_, i) => (
                      <Star
                        key={i}
                        size={16}
                        className={i < Math.floor(product.rating) ? 'fill-yellow-400 text-yellow-400' : 'text-gray-300'}
                      />
                    ))}
                  </div>
                  <span className="text-sm text-gray-600">{product.rating.toFixed(1)}</span>
                  <span className="text-sm text-gray-500">({product.reviews} reviews)</span>
                </div>

                {/* Price */}
                <div className="flex items-center gap-2 pt-2">
                  <span className="text-2xl font-bold text-gray-900">${product.price}</span>
                  {typeof product.originalPrice === 'number' && product.originalPrice > product.price && (
                    <span className="text-lg text-gray-400 line-through">${product.originalPrice}</span>
                  )}
                </div>

                {/* Add to Cart Button */}
                <Button
                  onClick={() => void handleAddToCart(product)}
                  disabled={busyProductId === product.id || product.availableStock <= 0 || (!isOwnerUser && !product.isActive)}
                  className="w-full bg-gradient-to-r from-blue-600 to-indigo-600 text-white py-3 rounded-lg font-semibold hover:shadow-lg transition flex items-center justify-center gap-2 group/btn"
                >
                  <ShoppingCart size={20} />
                  <span>
                    {product.availableStock <= 0
                      ? 'Out of Stock'
                      : busyProductId === product.id
                        ? 'Adding...'
                        : !product.isActive && isOwnerUser
                          ? 'Add to Cart (Owner)'
                          : !product.isActive
                            ? 'Inactive'
                            : 'Add to Cart'}
                  </span>
                </Button>
                <Button
                  variant="outline"
                  onClick={() => void openQuickView(product)}
                  className="w-full"
                >
                  <MessageCircle className="mr-2 h-4 w-4" /> Quick View
                </Button>
              </div>
            </div>
          ))}
        </div>

        {selectedProduct && (
          <>
            <button
              className="fixed inset-0 z-[85] bg-black/40"
              onClick={() => setSelectedProduct(null)}
              aria-label="Close quick view"
            />
            <aside className="fixed right-0 top-0 z-[90] h-full w-full max-w-xl overflow-y-auto border-l border-slate-200 bg-white shadow-2xl">
              <div className="sticky top-0 z-10 flex items-center justify-between border-b border-slate-200 bg-white/95 px-5 py-4 backdrop-blur">
                <h3 className="text-lg font-bold text-slate-900">Quick View</h3>
                <button
                  onClick={() => setSelectedProduct(null)}
                  className="rounded-md p-2 text-slate-500 transition hover:bg-slate-100 hover:text-slate-800"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>

              <div className="space-y-6 p-5">
                <div className="rounded-2xl border border-slate-200 bg-gradient-to-br from-slate-50 to-white p-4">
                  <div className="mb-3 text-6xl">{selectedProduct.image}</div>
                  <h4 className="text-2xl font-bold text-slate-900">{selectedProduct.name}</h4>
                  <p className="mt-1 text-sm text-slate-600">{selectedProductDetail?.description || 'Home quick-view for fast shopping actions.'}</p>
                  <div className="mt-3 flex items-center gap-3">
                    <span className="text-2xl font-bold text-slate-900">${selectedProductDetail?.price ?? selectedProduct.price}</span>
                    <span className="rounded-full bg-emerald-100 px-2 py-1 text-xs font-semibold text-emerald-700">
                      Stock: {selectedProductDetail?.availableStock ?? selectedProduct.availableStock}
                    </span>
                    <span className="rounded-full bg-slate-100 px-2 py-1 text-xs font-semibold text-slate-700">
                      {selectedProductDetail?.currency || ownerCurrency}
                    </span>
                  </div>
                  <Button
                    onClick={() => void handleAddToCart(selectedProduct)}
                    disabled={busyProductId === selectedProduct.id || selectedProduct.availableStock <= 0 || (!isOwnerUser && !selectedProduct.isActive)}
                    className="mt-4 w-full"
                  >
                    <ShoppingCart className="mr-2 h-4 w-4" />
                    {selectedProduct.availableStock <= 0
                      ? 'Out of Stock'
                      : !selectedProduct.isActive && isOwnerUser
                        ? 'Add to Cart (Owner)'
                        : !selectedProduct.isActive
                          ? 'Inactive'
                          : 'Add to Cart'}
                  </Button>
                </div>

                <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4">
                  <p className="mb-2 text-xs font-semibold uppercase tracking-wide text-amber-700">Rating snapshot</p>
                  {isDetailsLoading ? (
                    <p className="text-sm text-slate-600">Loading rating and reviews...</p>
                  ) : reviewStats ? (
                    <div className="space-y-2">
                      <p className="text-3xl font-bold text-slate-900">{reviewStats.averageRating.toFixed(1)} / 5</p>
                      <p className="text-sm text-slate-600">{reviewStats.totalReviews} total reviews</p>
                    </div>
                  ) : (
                    <p className="text-sm text-slate-600">No review stats yet for this product.</p>
                  )}
                </div>

                <div className="rounded-2xl border border-slate-200 p-4">
                  <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-slate-600">Top Reviews</p>
                  {isDetailsLoading ? (
                    <p className="text-sm text-slate-600">Loading reviews...</p>
                  ) : productReviews && productReviews.reviews.length > 0 ? (
                    <div className="space-y-3">
                      {productReviews.reviews.slice(0, 3).map((review) => (
                        <div key={review.id} className="rounded-xl border border-slate-200 bg-slate-50 p-3">
                          <div className="mb-1 flex items-center justify-between gap-3">
                            <p className="text-sm font-semibold text-slate-800">{review.customerName}</p>
                            <p className="text-xs text-slate-500">{new Date(review.createdAt).toLocaleDateString()}</p>
                          </div>
                          <p className="mb-1 text-xs text-amber-600">{'★'.repeat(Math.max(1, Math.min(review.rating, 5)))}</p>
                          <p className="text-sm text-slate-700">{review.title || review.reviewText}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-slate-600">No reviews yet.</p>
                  )}
                </div>

                <div className="rounded-2xl border border-emerald-200 bg-emerald-50 p-4">
                  <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-emerald-700">Frequently Bought Together</p>
                  {isDetailsLoading ? (
                    <p className="text-sm text-slate-600">Loading related products...</p>
                  ) : frequentlyBought.length > 0 ? (
                    <div className="space-y-2">
                      {frequentlyBought.map((item) => (
                        <div key={`fbt-${item.productId}`} className="flex items-center justify-between rounded-lg border border-emerald-200 bg-white px-3 py-2">
                          <span className="mr-2 text-lg">{getRecommendationEmoji(item.productName)}</span>
                          <span className="line-clamp-1 flex-1 text-sm text-slate-700">{item.productName}</span>
                          <span className="text-sm font-semibold text-emerald-700">${item.price}</span>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-sm text-slate-600">No related suggestions yet.</p>
                  )}
                </div>

                {user?.role === 'OWNER' && (
                  <div className="rounded-2xl border border-indigo-200 bg-indigo-50 p-4">
                    <p className="mb-3 text-xs font-semibold uppercase tracking-wide text-indigo-700">Owner mini-toolbar</p>

                    {!selectedProductSupportsOwnerActions && (
                      <p className="mb-3 rounded-md border border-amber-200 bg-amber-50 px-3 py-2 text-xs text-amber-800">
                        Demo product selected. Owner API actions are available only for real backend products.
                      </p>
                    )}

                    {ownerActionStatus && (
                      <p className="mb-2 rounded-md border border-emerald-200 bg-emerald-50 px-3 py-2 text-xs text-emerald-700">{ownerActionStatus}</p>
                    )}
                    {ownerActionError && (
                      <p className="mb-2 rounded-md border border-rose-200 bg-rose-50 px-3 py-2 text-xs text-rose-700">{ownerActionError}</p>
                    )}

                    <div className="space-y-3">
                        <div className="grid gap-2 sm:grid-cols-3">
                          <input
                            value={ownerPrice}
                            onChange={(event) => setOwnerPrice(event.target.value)}
                            placeholder="Price"
                            className="h-10 rounded-md border border-indigo-200 bg-white px-3 text-sm"
                          />
                          <input
                            value={ownerCurrency}
                            onChange={(event) => setOwnerCurrency(event.target.value.toUpperCase())}
                            placeholder="Currency"
                            className="h-10 rounded-md border border-indigo-200 bg-white px-3 text-sm"
                          />
                          <Button
                            size="sm"
                            disabled={isOwnerBusy || !selectedProduct || !selectedProductSupportsOwnerActions}
                            onClick={() =>
                              void runOwnerAction(async () => {
                                if (!selectedProduct) return
                                const nextPrice = Number(ownerPrice)
                                if (!Number.isFinite(nextPrice) || nextPrice <= 0) {
                                  throw new Error('Invalid price')
                                }
                                await productService.updatePrice(selectedProduct.id, nextPrice, ownerCurrency || 'USD')
                              }, 'Price updated successfully.')
                            }
                          >
                            Update Price
                          </Button>
                        </div>

                        <div className="grid gap-2">
                          <input
                            value={ownerName}
                            onChange={(event) => setOwnerName(event.target.value)}
                            placeholder="Product name"
                            className="h-10 rounded-md border border-indigo-200 bg-white px-3 text-sm"
                          />
                          <textarea
                            value={ownerDescription}
                            onChange={(event) => setOwnerDescription(event.target.value)}
                            placeholder="Description"
                            rows={3}
                            className="rounded-md border border-indigo-200 bg-white px-3 py-2 text-sm"
                          />
                          <Button
                            size="sm"
                            disabled={isOwnerBusy || !selectedProduct || !selectedProductSupportsOwnerActions}
                            onClick={() =>
                              void runOwnerAction(async () => {
                                if (!selectedProduct) return
                                await productService.updateProductDetails(selectedProduct.id, ownerName.trim(), ownerDescription.trim())
                              }, 'Product details updated successfully.')
                            }
                          >
                            Update Details
                          </Button>
                        </div>

                        <div className="grid gap-2 grid-cols-2">
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={isOwnerBusy || !selectedProduct || !selectedProductSupportsOwnerActions}
                            onClick={() =>
                              void runOwnerAction(async () => {
                                if (!selectedProduct) return
                                await productService.deactivateProduct(selectedProduct.id)
                              }, 'Product deactivated.')
                            }
                          >
                            Deactivate
                          </Button>
                          <Button
                            size="sm"
                            variant="outline"
                            disabled={isOwnerBusy || !selectedProduct || !selectedProductSupportsOwnerActions}
                            onClick={() =>
                              void runOwnerAction(async () => {
                                if (!selectedProduct) return
                                await productService.discontinueProduct(selectedProduct.id)
                              }, 'Product discontinued.')
                            }
                          >
                            Discontinue
                          </Button>
                        </div>

                        <p className="text-xs text-slate-500">
                          Activate and image upload are available in Admin Products only.
                        </p>
                    </div>
                  </div>
                )}
              </div>
            </aside>
          </>
        )}

      </div>
    </section>
  )
}
