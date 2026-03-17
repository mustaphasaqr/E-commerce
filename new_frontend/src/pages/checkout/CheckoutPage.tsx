import { FormEvent, useCallback, useEffect, useMemo, useState } from 'react'
import { isAxiosError } from 'axios'
import { CreditCard, MapPin, Receipt, ShieldCheck } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui'
import cartService from '@/features/cart/api/cartService'
import orderService from '@/features/orders/api/orderService'
import { paymentService, setLastCheckoutId } from '@/features/payments/api/paymentService'
import type { CartDTO } from '@/features/cart/types'
import type { CreateOrderRequest, TaxCalculationResponse } from '@/features/orders/types'

const formatMoney = (value: number): string =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0)

const PAYMENT_OPTIONS = ['PAYMOB', 'VISA', 'CASH_ON_DELIVERY'] as const
type PaymentOption = (typeof PAYMENT_OPTIONS)[number]

const PAYMENT_LABELS: Record<PaymentOption, string> = {
  PAYMOB: 'Paymob (Online)',
  VISA: 'Visa / Card',
  CASH_ON_DELIVERY: 'Cash on Delivery',
}

const getAuthUserId = (): string | null => {
  const raw = localStorage.getItem('authUser')
  if (!raw) return null
  try {
    const parsed = JSON.parse(raw) as { id?: string }
    return parsed.id ?? null
  } catch {
    return null
  }
}

const getAuthUserEmail = (): string => {
  const raw = localStorage.getItem('authUser')
  if (!raw) return ''
  try {
    const parsed = JSON.parse(raw) as { email?: string }
    return parsed.email ?? ''
  } catch {
    return ''
  }
}

const idempotencyKey = (): string => {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`
}

const emptyCart: CartDTO = {
  id: null,
  userId: null,
  sessionId: '',
  items: [],
  totalAmount: 0,
  status: 'ACTIVE',
  totalItems: 0,
}

export default function CheckoutPage() {
  const navigate = useNavigate()
  const [cart, setCart] = useState<CartDTO>(emptyCart)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [shippingCity, setShippingCity] = useState('')
  const [shippingState, setShippingState] = useState('')
  const [shippingCountry, setShippingCountry] = useState('')
  const [shippingZipCode, setShippingZipCode] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<PaymentOption>('PAYMOB')

  const [taxResult, setTaxResult] = useState<TaxCalculationResponse | null>(null)
  const [isTaxLoading, setIsTaxLoading] = useState(false)

  useEffect(() => {
    const load = async () => {
      setIsLoading(true)
      setError(null)
      try {
        const current = await cartService.getCart()
        setCart(current)
      } catch {
        setError('Failed to load cart for checkout.')
      } finally {
        setIsLoading(false)
      }
    }

    void load()
  }, [])

  const estimateTax = useCallback(async () => {
    const customerId = getAuthUserId()
    if (!customerId || !shippingCountry.trim() || cart.items.length === 0) return

    setIsTaxLoading(true)
    try {
      const result = await orderService.calculateTax({
        orderId: null,
        customerId: Number(customerId),
        subtotal: cart.totalAmount,
        shippingCountryCode: shippingCountry.trim().toUpperCase(),
        billingCountryCode: shippingCountry.trim().toUpperCase(),
        customerType: 'INDIVIDUAL',
        items: cart.items.map((item) => ({
          productId: Number(item.productId),
          productName: item.productName,
          unitPrice: item.price,
          quantity: item.quantity,
          taxCategory: 'STANDARD' as const,
        })),
      })
      setTaxResult(result)
    } catch {
      setTaxResult(null)
    } finally {
      setIsTaxLoading(false)
    }
  }, [cart.items, cart.totalAmount, shippingCountry])

  useEffect(() => {
    if (shippingCountry.trim().length >= 2) {
      const timer = setTimeout(() => void estimateTax(), 600)
      return () => clearTimeout(timer)
    }
    setTaxResult(null)
  }, [shippingCountry, estimateTax])

  const canSubmit = useMemo(() => {
    return cart.items.length > 0 && shippingCity.trim() && shippingCountry.trim() && shippingZipCode.trim()
  }, [cart.items.length, shippingCity, shippingCountry, shippingZipCode])

  const submitOrder = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    const customerId = getAuthUserId()
    if (!customerId) {
      setError('Unable to identify customer. Please login again.')
      return
    }

    if (!canSubmit) {
      setError('Please complete shipping details before placing order.')
      return
    }

    const payload: CreateOrderRequest = {
      customerId,
      items: cart.items.map((item) => ({
        productId: String(item.productId),
        productName: item.productName,
        quantity: item.quantity,
        price: item.price,
      })),
      paymentMethod,
      shippingCity,
      shippingState,
      shippingCountry,
      shippingZipCode,
      referrer: 'web-checkout',
      utmSource: 'frontend',
      utmCampaign: 'standard-checkout',
      cartId: cart.id ?? undefined,
    }

    setIsSubmitting(true)
    setError(null)
    try {
      const order = await orderService.createOrder(payload, idempotencyKey())
      await cartService.clearCart()

      // If Paymob, initiate checkout and redirect
      if (paymentMethod === 'PAYMOB') {
        try {
          const checkout = await paymentService.initiateCheckout({
            orderId: order.orderId,
            paymentMethod: 'VISA',
            customerEmail: getAuthUserEmail(),
            shopperResultUrl: `${window.location.origin}/payment/return`,
          })
          if (checkout.success && checkout.redirectUrl) {
            setLastCheckoutId(checkout.checkoutId)
            window.location.href = checkout.redirectUrl
            return
          }
        } catch {
          // Payment initiation failed but order was created — navigate to order detail
        }
      }

      navigate(`/orders/${order.orderId}`)
    } catch (err: unknown) {
      if (isAxiosError(err) && err.response?.data) {
        const data = err.response.data as { message?: string; error?: string; detail?: string }
        const backendMsg = data.message || data.detail || data.error
        setError(backendMsg || 'Failed to create order. Please retry.')
      } else {
        setError('Failed to create order. Please check your connection and retry.')
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isLoading) {
    return <div className="mx-auto max-w-6xl p-8 text-sm text-gray-600">Preparing checkout...</div>
  }

  return (
    <div className="mx-auto max-w-6xl px-4 py-8 sm:px-6 lg:px-8">
      <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
        <Card className="border-gray-200">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-2xl">
              <MapPin className="h-6 w-6 text-indigo-600" /> Checkout
            </CardTitle>
            <CardDescription>Shipping details and payment method selection.</CardDescription>
          </CardHeader>
          <CardContent>
            <form className="space-y-4" onSubmit={(event) => void submitOrder(event)}>
              <div className="grid gap-3 sm:grid-cols-2">
                <Input value={shippingCity} onChange={(e) => setShippingCity(e.target.value)} placeholder="City" />
                <Input value={shippingState} onChange={(e) => setShippingState(e.target.value)} placeholder="State / Province" />
                <Input value={shippingCountry} onChange={(e) => setShippingCountry(e.target.value)} placeholder="Country code (e.g. EG, SA, US)" />
                <Input value={shippingZipCode} onChange={(e) => setShippingZipCode(e.target.value)} placeholder="ZIP / Postal code" />
              </div>

              <div className="rounded-xl border border-gray-200 bg-gray-50 p-4">
                <p className="mb-2 flex items-center gap-2 text-sm font-semibold text-gray-800">
                  <CreditCard className="h-4 w-4" /> Payment method
                </p>
                <div className="grid gap-2 sm:grid-cols-3">
                  {PAYMENT_OPTIONS.map((option) => (
                    <label
                      key={option}
                      className={`flex cursor-pointer items-center gap-2 rounded-md border px-3 py-2 text-sm transition-colors ${
                        paymentMethod === option
                          ? 'border-indigo-500 bg-indigo-50 font-medium text-indigo-700'
                          : 'border-gray-300 bg-white text-gray-700'
                      }`}
                    >
                      <input
                        type="radio"
                        name="paymentMethod"
                        value={option}
                        checked={paymentMethod === option}
                        onChange={() => setPaymentMethod(option)}
                        className="accent-indigo-600"
                      />
                      {PAYMENT_LABELS[option]}
                    </label>
                  ))}
                </div>
              </div>

              {error && <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>}

              <Button type="submit" className="w-full" disabled={!canSubmit || isSubmitting || cart.items.length === 0}>
                {isSubmitting
                  ? 'Placing order...'
                  : paymentMethod === 'PAYMOB'
                    ? 'Place Order & Pay with Paymob'
                    : 'Place Order'}
              </Button>
            </form>
          </CardContent>
        </Card>

        <div className="space-y-4">
          <Card className="border-gray-200 bg-gradient-to-b from-emerald-50 to-white">
            <CardHeader>
              <CardTitle className="text-xl">Order Summary</CardTitle>
              <CardDescription>Review items and totals before submit.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3">
              {cart.items.map((item) => (
                <div key={`${item.productId}`} className="flex items-center justify-between text-sm">
                  <span className="text-gray-700">{item.productName} x{item.quantity}</span>
                  <span className="font-semibold text-gray-900">{formatMoney(item.subtotal)}</span>
                </div>
              ))}
              <div className="border-t border-gray-200 pt-3 space-y-1">
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Subtotal</span>
                  <span className="text-lg font-bold text-gray-900">{formatMoney(cart.totalAmount)}</span>
                </div>
              </div>
            </CardContent>
          </Card>

          {(taxResult || isTaxLoading) && (
            <Card className="border-gray-200">
              <CardHeader className="pb-2">
                <CardTitle className="flex items-center gap-2 text-base">
                  <Receipt className="h-4 w-4 text-amber-600" /> Tax Estimate
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2 text-sm">
                {isTaxLoading ? (
                  <p className="text-gray-500">Calculating tax...</p>
                ) : taxResult ? (
                  <>
                    {taxResult.isTaxExempt ? (
                      <p className="text-green-700 font-medium">Tax exempt</p>
                    ) : (
                      <>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Tax ({taxResult.taxRate})</span>
                          <span className="font-medium text-gray-900">{formatMoney(taxResult.taxAmount)}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-600">Jurisdiction</span>
                          <span className="text-gray-700">{taxResult.jurisdiction} &middot; {taxResult.taxType}</span>
                        </div>
                        <div className="flex justify-between border-t border-gray-200 pt-2">
                          <span className="font-semibold text-gray-800">Total (incl. tax)</span>
                          <span className="text-lg font-bold text-gray-900">{formatMoney(taxResult.total)}</span>
                        </div>
                      </>
                    )}
                  </>
                ) : null}
              </CardContent>
            </Card>
          )}

          <p className="flex items-center gap-2 px-1 text-xs text-gray-500">
            <ShieldCheck className="h-3.5 w-3.5" /> Secure checkout powered by Paymob Accept.
          </p>
        </div>
      </div>
    </div>
  )
}
