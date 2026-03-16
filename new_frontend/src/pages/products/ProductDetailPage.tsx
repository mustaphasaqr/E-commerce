import { useEffect, useMemo, useState } from 'react'
import { ArrowLeft, Box, Minus, Plus, ShoppingCart } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui'
import productService from '@/features/products/api/productService'
import cartService from '@/features/cart/api/cartService'
import type { ProductDetail } from '@/features/products/types'

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

  const [product, setProduct] = useState<ProductDetail | null>(null)
  const [quantity, setQuantity] = useState(1)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

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
      } catch {
        setError('Failed to load product details. Please retry.')
        setProduct(null)
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [id])

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
      navigate('/cart')
    } catch {
      setError('Failed to add product to cart. Please retry.')
    } finally {
      setIsSubmitting(false)
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

            <Button
              className="w-full"
              onClick={() => void addToCart()}
              disabled={isSubmitting || product.availableStock <= 0}
            >
              <ShoppingCart className="mr-2 h-4 w-4" />
              {product.availableStock <= 0 ? 'Out of Stock' : isSubmitting ? 'Adding...' : 'Add to Cart'}
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
