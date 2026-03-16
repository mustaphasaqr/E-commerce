import { useEffect, useMemo, useState } from 'react'
import { ArrowRight, Minus, Plus, RefreshCw, ShoppingBag, Trash2 } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui'
import cartService from '@/features/cart/api/cartService'
import type { CartDTO, CartItem } from '@/features/cart/types'

const formatMoney = (value: number): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0)

const emptyCart: CartDTO = {
  id: null,
  userId: null,
  sessionId: '',
  items: [],
  totalAmount: 0,
  status: 'ACTIVE',
  totalItems: 0,
}

export default function CartPage() {
  const navigate = useNavigate()
  const [cart, setCart] = useState<CartDTO>(emptyCart)
  const [isLoading, setIsLoading] = useState(true)
  const [isMutating, setIsMutating] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const subtotal = useMemo(() => cart.totalAmount ?? 0, [cart.totalAmount])

  const loadCart = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const current = await cartService.getCart()
      setCart(current)
    } catch {
      setError('Failed to load cart. Please retry.')
      setCart(emptyCart)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadCart()
  }, [])

  const runMutation = async (action: () => Promise<CartDTO>) => {
    setIsMutating(true)
    setError(null)
    try {
      const updated = await action()
      setCart(updated)
    } catch {
      setError('Cart update failed. Please retry.')
    } finally {
      setIsMutating(false)
    }
  }

  const updateQty = async (item: CartItem, nextQty: number) => {
    await runMutation(() => cartService.updateCartItem({ productId: item.productId, quantity: nextQty }))
  }

  const removeItem = async (item: CartItem) => {
    await runMutation(() => cartService.removeFromCart(item.productId))
  }

  const clearCart = async () => {
    await runMutation(() => cartService.clearCart())
  }

  if (isLoading) {
    return <div className="mx-auto max-w-6xl p-8 text-sm text-gray-600">Loading cart...</div>
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="grid gap-6 lg:grid-cols-[1.6fr_1fr]">
        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-2xl">
              <ShoppingBag className="h-6 w-6 text-cyan-700" /> Your Cart
            </CardTitle>
            <CardDescription>Update quantities, remove items, and review subtotal before checkout.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            {error && (
              <div className="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</div>
            )}

            {cart.items.length === 0 ? (
              <div className="rounded-lg border border-dashed border-gray-300 p-8 text-center">
                <p className="text-gray-700">Your cart is empty.</p>
                <Button className="mt-4" onClick={() => navigate('/products')}>
                  Browse Products
                </Button>
              </div>
            ) : (
              <>
                {cart.items.map((item) => (
                  <div key={`${item.productId}`} className="rounded-xl border border-gray-200 p-4">
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div>
                        <h3 className="font-semibold text-gray-900">{item.productName}</h3>
                        <p className="text-sm text-gray-500">Unit price: {formatMoney(item.price)}</p>
                      </div>

                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="icon"
                          disabled={isMutating || item.quantity <= 1}
                          onClick={() => void updateQty(item, item.quantity - 1)}
                        >
                          <Minus className="h-4 w-4" />
                        </Button>
                        <span className="min-w-[42px] text-center text-sm font-semibold">{item.quantity}</span>
                        <Button
                          variant="outline"
                          size="icon"
                          disabled={isMutating}
                          onClick={() => void updateQty(item, item.quantity + 1)}
                        >
                          <Plus className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          disabled={isMutating}
                          onClick={() => void removeItem(item)}
                        >
                          <Trash2 className="mr-1 h-4 w-4 text-rose-600" /> Remove
                        </Button>
                      </div>
                    </div>

                    <div className="mt-3 text-right text-sm font-semibold text-gray-900">
                      Item total: {formatMoney(item.subtotal)}
                    </div>
                  </div>
                ))}

                <div className="flex flex-wrap gap-2">
                  <Button variant="outline" onClick={() => void loadCart()} disabled={isMutating}>
                    <RefreshCw className="mr-2 h-4 w-4" /> Refresh
                  </Button>
                  <Button variant="destructive" onClick={() => void clearCart()} disabled={isMutating}>
                    Clear Cart
                  </Button>
                </div>
              </>
            )}
          </CardContent>
        </Card>

        <Card className="h-fit border-gray-200 bg-gradient-to-b from-slate-50 to-white">
          <CardHeader>
            <CardTitle className="text-xl">Order Summary</CardTitle>
            <CardDescription>Subtotal updates instantly with quantity changes.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="flex items-center justify-between text-sm text-gray-600">
              <span>Total items</span>
              <span className="font-semibold text-gray-900">{cart.totalItems}</span>
            </div>
            <div className="flex items-center justify-between text-sm text-gray-600">
              <span>Subtotal</span>
              <span className="text-xl font-bold text-gray-900">{formatMoney(subtotal)}</span>
            </div>
            <Button
              className="w-full"
              onClick={() => navigate('/checkout')}
              disabled={cart.items.length === 0 || isMutating}
            >
              Proceed to Checkout <ArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
