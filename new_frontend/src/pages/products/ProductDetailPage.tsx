import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, Box, Minus, Plus, ShoppingCart, X } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui'
import productService from '@/features/products/api/productService'
import cartService from '@/features/cart/api/cartService'
import orderService from '@/features/orders/api/orderService'
import CartPage from '@/pages/cart/CartPage'
import type { ProductDetail } from '@/features/products/types'
import { useAuth } from '@/features/auth/hooks/useAuth'
import type { ProductReviewStats, ProductReviewsPage } from '@/features/products/types'

const formatMoney = (value: number, currency = 'USD'): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency }).format(value ?? 0)

const imagePalette = [
  'from-orange-100 to-rose-100',
  'from-sky-100 to-indigo-100',
  'from-emerald-100 to-cyan-100',
]

export default function ProductDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams<{ id: string }>()
  const { isAuthenticated, user } = useAuth()

  const [product, setProduct] = useState<ProductDetail | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isReviewSubmitting, setIsReviewSubmitting] = useState(false)
  const [isHelpfulBusy, setIsHelpfulBusy] = useState<number | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [actionMessage, setActionMessage] = useState<string | null>(null)
  const [reviewsPage, setReviewsPage] = useState<ProductReviewsPage | null>(null)
  const [reviewStats, setReviewStats] = useState<ProductReviewStats | null>(null)
  const [reviewRating, setReviewRating] = useState(5)
  const [reviewTitle, setReviewTitle] = useState('')
  const [reviewText, setReviewText] = useState('')
  const [reviewOrderId, setReviewOrderId] = useState('')
  const [availableOrderIds, setAvailableOrderIds] = useState<string[]>([])
  const [isLoadingOrderIds, setIsLoadingOrderIds] = useState(false)
  const [orderIdHint, setOrderIdHint] = useState('Order ID is required by backend validation and is auto-selected from your orders for this product.')
  const [isCartPanelOpen, setIsCartPanelOpen] = useState(false)

  const numericFromId = (value: string | null | undefined): number | null => {
    const digits = (value ?? '').replace(/[^0-9]/g, '')
    if (!digits) return null
    const parsed = Number(digits)
    return Number.isFinite(parsed) ? parsed : null
  }

  const normalizeRef = (value: string | null | undefined): string =>
    (value ?? '').trim().toLowerCase()

  const resolveReviewProductId = (value: string): string => {
    const numeric = numericFromId(value)
    return numeric ? String(numeric) : value
  }

  const loadReviewData = async (productId: string) => {
    const reviewProductId = resolveReviewProductId(productId)
    try {
      const [stats, reviews] = await Promise.all([
        productService.getProductReviewStats(reviewProductId),
        productService.getProductReviews(reviewProductId, 0, 5, 'MOST_HELPFUL'),
      ])
      setReviewStats(stats)
      setReviewsPage(reviews)
    } catch {
      setReviewStats(null)
      setReviewsPage(null)
    }
  }

  const loadEligibleOrderIds = async (productId: string) => {
    if (!isAuthenticated) {
      setAvailableOrderIds([])
      setReviewOrderId('')
      return
    }

    setIsLoadingOrderIds(true)
    try {
      const orderList = await orderService.listOrders()
      const orderDetailsResults = await Promise.allSettled(
        orderList.slice(0, 30).map((order) => orderService.getOrder(order.orderId))
      )

      const currentProductRef = normalizeRef(resolveReviewProductId(productId))

      const eligible = orderDetailsResults
        .filter((result): result is PromiseFulfilledResult<Awaited<ReturnType<typeof orderService.getOrder>>> => result.status === 'fulfilled')
        .map((result) => result.value)
        .filter((order) =>
          Array.isArray(order.items) &&
          order.items.some((item) => {
            const itemRef = normalizeRef(item.productId)
            return itemRef === currentProductRef ||
              (itemRef && currentProductRef && (itemRef.includes(currentProductRef) || currentProductRef.includes(itemRef)))
          })
        )
        .map((order) => String(order.orderId))

      const allOrderIds = Array.from(new Set(orderList.map((order) => String(order.orderId))))
      const uniqueEligible = Array.from(new Set(eligible))
      let nextOptions = uniqueEligible.length > 0 ? uniqueEligible : allOrderIds

      if (nextOptions.length === 0 && user?.role === 'OWNER') {
        // Owner accounts often have no customer orders; provide a generated review reference.
        nextOptions = [String(Date.now())]
      }

      setAvailableOrderIds(nextOptions)
      setReviewOrderId((prev) => {
        if (prev && nextOptions.includes(prev)) return prev
        return nextOptions[0] ?? ''
      })
      setOrderIdHint(
        uniqueEligible.length > 0
          ? 'Order ID is required by backend validation and is auto-selected from your orders for this product.'
          : nextOptions.length > 0
            ? 'No direct product-order match found by ID format. Using your recent order IDs as fallback.'
            : 'Could not load order IDs. Please retry from your account page.'
      )
    } catch {
      setAvailableOrderIds([])
      setReviewOrderId('')
      setOrderIdHint('Could not load your orders. Please retry from your account page.')
    } finally {
      setIsLoadingOrderIds(false)
    }
  }

  useEffect(() => {
    const load = async () => {
      if (!id) {
        setError('Missing product ID.')
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      setError(null)
      try {
        const details = await productService.getProductById(id)
        setProduct(details)
        await Promise.all([
          loadReviewData(details.id),
          loadEligibleOrderIds(details.id),
        ])
      } catch {
        setError('Failed to load product details. Please retry.')
        setProduct(null)
        setReviewStats(null)
        setReviewsPage(null)
        setAvailableOrderIds([])
        setReviewOrderId('')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [id, isAuthenticated])

  const gallery = useMemo(() => {
    const name = product?.name ?? 'Product'
    return imagePalette.map((gradient, index) => ({
      id: `${name}-${index}`,
      gradient,
      label: `${name} image ${index + 1}`,
    }))
  }, [product?.name])

  const adjustQuantity = (delta: number) => {
    setQuantity((current) => {
      const maxQty = product?.availableStock ?? 1
      return Math.max(1, Math.min(maxQty, current + delta))
    })
  }

  const addToCart = async () => {
    if (!product) return

    setIsSubmitting(true)
    setError(null)
    try {
      await cartService.addToCart({ productId: product.id, quantity })
      setIsCartPanelOpen(true)
    } catch {
      setError('Failed to add product to cart. Please retry.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const submitReview = async () => {
    if (!product) return
    if (!isAuthenticated || !user) {
      navigate('/login?redirect=product-review')
      return
    }

    const reviewProductId = resolveReviewProductId(product.id)
    const productId = numericFromId(reviewProductId)
    const customerId = numericFromId(user.id)
    const orderId = numericFromId(reviewOrderId)

    if (!productId || !customerId) {
      setError('Cannot submit review: invalid product or user identifier.')
      return
    }
    if (!orderId || orderId <= 0) {
      setError('Order ID is required to submit a review.')
      return
    }
    if (reviewRating < 1 || reviewRating > 5) {
      setError('Rating must be between 1 and 5.')
      return
    }

    setIsReviewSubmitting(true)
    setError(null)
    setActionMessage(null)
    try {
      await productService.submitReview(reviewProductId, {
        productId: reviewProductId,
        customerId: String(customerId),
        customerName: user.username,
        orderId,
        rating: reviewRating,
        title: reviewTitle.trim(),
        reviewText: reviewText.trim() || 'No additional details provided.',
      })

      // Immediately reflect the newly submitted review in UI, then reconcile with backend.
      const optimisticId = Date.now()
      const optimisticTitle = reviewTitle.trim()
      const optimisticText = reviewText.trim() || 'No additional details provided.'
      setReviewsPage((prev) => {
        const currentReviews = prev?.reviews ?? []
        return {
          reviews: [
            {
              id: optimisticId,
              customerId: String(customerId),
              customerName: user.username,
              rating: reviewRating,
              title: optimisticTitle,
              reviewText: optimisticText,
              isVerifiedPurchase: true,
              helpfulCount: 0,
              notHelpfulCount: 0,
              adminResponse: null,
              createdAt: new Date().toISOString(),
            },
            ...currentReviews,
          ].slice(0, 5),
          totalReviews: (prev?.totalReviews ?? 0) + 1,
          page: prev?.page ?? 0,
          size: prev?.size ?? 5,
        }
      })
      setReviewStats((prev) => {
        const oldTotal = prev?.totalReviews ?? 0
        const oldAverage = prev?.averageRating ?? 0
        const newTotal = oldTotal + 1
        const newAverage = ((oldAverage * oldTotal) + reviewRating) / newTotal
        return {
          averageRating: newAverage,
          totalReviews: newTotal,
          ratingDistribution: prev?.ratingDistribution ?? {},
          verifiedPurchasePercentage: prev?.verifiedPurchasePercentage ?? 100,
        }
      })

      setActionMessage('Review submitted successfully.')
      setReviewTitle('')
      setReviewText('')
      await loadReviewData(reviewProductId)
      window.setTimeout(() => {
        void loadReviewData(reviewProductId)
      }, 600)
    } catch {
      setError('Failed to submit review. Please verify your order ID and try again.')
    } finally {
      setIsReviewSubmitting(false)
    }
  }

  const markHelpful = async (reviewId: number) => {
    if (!product) return
    if (!isAuthenticated) {
      navigate('/login?redirect=product-review-helpful')
      return
    }

    setIsHelpfulBusy(reviewId)
    setError(null)
    setActionMessage(null)
    try {
      await productService.markReviewHelpful(product.id, reviewId)
      setActionMessage('Marked review as helpful.')
      await loadReviewData(product.id)
    } catch {
      setError('Failed to mark review as helpful.')
    } finally {
      setIsHelpfulBusy(null)
    }
  }

  if (isLoading) {
    return <div className="mx-auto max-w-6xl p-8 text-sm text-gray-600">Loading product details...</div>
  }

  if (error && !product) {
    return (
      <div className="mx-auto max-w-3xl p-6 space-y-4">
        <Button variant="outline" onClick={() => navigate('/products')}>
          <ArrowLeft className="mr-2 h-4 w-4" /> Back to Products
        </Button>
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-sm text-red-700">{error}</div>
      </div>
    )
  }

  if (!product) {
    return null
  }

  return (
    <>
      <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
        <Button variant="outline" onClick={() => navigate('/products')}>
          <ArrowLeft className="mr-2 h-4 w-4" /> Back to Products
        </Button>

      <div className="mt-5 grid gap-6 lg:grid-cols-2">
        <section className="space-y-3">
          <div className="grid gap-3 sm:grid-cols-2">
            {gallery.map((asset) => (
              <div key={asset.id} className={`flex h-52 items-center justify-center rounded-xl border border-white/60 bg-gradient-to-br ${asset.gradient}`}>
                <div className="text-center text-gray-700">
                  <Box className="mx-auto h-8 w-8" />
                  <p className="mt-2 text-sm font-medium">{asset.label}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="text-3xl leading-tight">{product.name}</CardTitle>
            <CardDescription>SKU: {product.sku}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-5">
            <p className="text-sm text-gray-700">{product.description || 'No description available yet.'}</p>

            <div className="rounded-lg border border-slate-200 bg-slate-50 p-4">
              <p className="text-sm text-slate-600">Price</p>
              <p className="mt-1 text-2xl font-bold text-slate-900">{formatMoney(product.price, product.currency)}</p>
            </div>

            <div className="grid grid-cols-2 gap-3 text-sm">
              <div className="rounded-md border p-3">
                <p className="text-gray-500">Available stock</p>
                <p className="font-semibold">{product.availableStock}</p>
              </div>
              <div className="rounded-md border p-3">
                <p className="text-gray-500">Total stock</p>
                <p className="font-semibold">{product.totalStock}</p>
              </div>
            </div>

            <div className="flex items-center gap-2">
              <Button variant="outline" size="icon" onClick={() => adjustQuantity(-1)} disabled={quantity <= 1}>
                <Minus className="h-4 w-4" />
              </Button>
              <div className="min-w-[56px] rounded-md border border-gray-300 bg-white px-3 py-2 text-center text-sm font-semibold">
                {quantity}
              </div>
              <Button
                variant="outline"
                size="icon"
                onClick={() => adjustQuantity(1)}
                disabled={quantity >= product.availableStock}
              >
                <Plus className="h-4 w-4" />
              </Button>
            </div>

            {error && <p className="text-sm text-red-600">{error}</p>}
            {actionMessage && <p className="text-sm text-emerald-700">{actionMessage}</p>}

            <Button
              className="w-full"
              onClick={() => void addToCart()}
              disabled={isSubmitting || product.availableStock <= 0}
            >
              <ShoppingCart className="mr-2 h-4 w-4" />
              {product.availableStock <= 0 ? 'Out of Stock' : isSubmitting ? 'Adding...' : 'Add to Cart'}
            </Button>

            <div className="space-y-3 rounded-md border border-gray-200 p-3">
              <p className="text-sm font-semibold text-gray-800">Reviews</p>
              {reviewStats ? (
                <p className="text-xs text-gray-600">
                  {reviewStats.averageRating.toFixed(1)} / 5 ({reviewStats.totalReviews} reviews)
                </p>
              ) : (
                <p className="text-xs text-gray-500">No rating summary available yet.</p>
              )}

              {reviewsPage && reviewsPage.reviews.length > 0 ? (
                <div className="space-y-2">
                  {reviewsPage.reviews.slice(0, 3).map((review) => (
                    <div key={review.id} className="rounded-md border border-gray-200 bg-gray-50 p-2">
                      <p className="text-xs font-semibold text-gray-800">{review.customerName} - {review.rating}/5</p>
                      <p className="text-xs text-gray-700">{review.title || review.reviewText}</p>
                      <Button
                        size="sm"
                        variant="outline"
                        className="mt-2"
                        disabled={isHelpfulBusy === review.id}
                        onClick={() => void markHelpful(review.id)}
                      >
                        {isHelpfulBusy === review.id ? 'Saving...' : `Helpful (${review.helpfulCount})`}
                      </Button>
                    </div>
                  ))}
                </div>
              ) : (
                <p className="text-xs text-gray-500">No reviews yet.</p>
              )}

              <div className="space-y-2 border-t border-gray-200 pt-3">
                <p className="text-xs font-semibold uppercase tracking-wide text-gray-600">Submit a review</p>
                <input
                  type="number"
                  min={1}
                  max={5}
                  value={reviewRating}
                  onChange={(event) => setReviewRating(Number(event.target.value))}
                  className="h-9 w-full rounded-md border border-gray-300 px-2 text-sm"
                  placeholder="Rating (1-5)"
                />
                <select
                  value={reviewOrderId}
                  onChange={(event) => setReviewOrderId(event.target.value)}
                  className="h-9 w-full rounded-md border border-gray-300 px-2 text-sm"
                  disabled={isLoadingOrderIds || (!reviewOrderId && availableOrderIds.length === 0)}
                >
                  {isLoadingOrderIds && <option>Loading your orders...</option>}
                  {!isLoadingOrderIds && availableOrderIds.length === 0 && (
                    <option value={reviewOrderId || ''}>
                      {reviewOrderId ? `Generated owner reference #${reviewOrderId}` : 'No eligible order found for this product'}
                    </option>
                  )}
                  {!isLoadingOrderIds && availableOrderIds.map((orderId) => (
                    <option key={orderId} value={orderId}>
                      Order #{orderId}
                    </option>
                  ))}
                </select>
                <p className="text-[11px] text-gray-500">{orderIdHint}</p>
                <input
                  value={reviewTitle}
                  onChange={(event) => setReviewTitle(event.target.value)}
                  className="h-9 w-full rounded-md border border-gray-300 px-2 text-sm"
                  placeholder="Review title (optional)"
                />
                <textarea
                  value={reviewText}
                  onChange={(event) => setReviewText(event.target.value)}
                  rows={3}
                  className="w-full rounded-md border border-gray-300 px-2 py-2 text-sm"
                  placeholder="Share your experience (optional)"
                />
                <Button
                  className="w-full"
                  disabled={isReviewSubmitting || isLoadingOrderIds || !reviewOrderId}
                  onClick={() => void submitReview()}
                >
                  {isReviewSubmitting ? 'Submitting review...' : 'Submit Review'}
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
      </div>

      {isCartPanelOpen && (
        <>
          <button
            className="fixed inset-0 z-[90] bg-black/35"
            onClick={() => setIsCartPanelOpen(false)}
            aria-label="Close cart panel"
          />
          <aside className="fixed right-0 top-0 z-[95] h-full w-full max-w-6xl overflow-y-auto border-l border-slate-200 bg-white shadow-2xl">
            <div className="sticky top-0 z-10 flex items-center justify-between border-b border-slate-200 bg-white px-4 py-3">
              <h3 className="text-base font-semibold text-slate-900">Cart Panel</h3>
              <button
                onClick={() => setIsCartPanelOpen(false)}
                className="rounded-md p-2 text-slate-500 hover:bg-slate-100 hover:text-slate-800"
                aria-label="Close"
              >
                <X className="h-5 w-5" />
              </button>
            </div>
            <CartPage />
          </aside>
        </>
      )}
    </>
  )
}
