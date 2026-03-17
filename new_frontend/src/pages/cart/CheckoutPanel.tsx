import { FormEvent, useCallback, useEffect, useState } from 'react'
import { isAxiosError } from 'axios'
import { CreditCard, MapPin, Receipt, X } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, CardContent, CardHeader, CardTitle, Input } from '@/shared/components/ui'
import orderService from '@/features/orders/api/orderService'
import { paymentService, setLastCheckoutId } from '@/features/payments/api/paymentService'
import type { CartDTO } from '@/features/cart/types'
import type { CreateOrderRequest, TaxCalculationResponse } from '@/features/orders/types'
import cartService from '@/features/cart/api/cartService'

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

interface CheckoutPanelProps {
  cart: CartDTO
  onClose: () => void
  onOrderPlaced: () => void
}

export default function CheckoutPanel({ cart, onClose, onOrderPlaced }: CheckoutPanelProps) {
  const navigate = useNavigate()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [shippingCity, setShippingCity] = useState('')
  const [shippingState, setShippingState] = useState('')
  const [shippingCountry, setShippingCountry] = useState('')
  const [shippingZipCode, setShippingZipCode] = useState('')
  const [paymentMethod, setPaymentMethod] = useState<PaymentOption>('PAYMOB')

  const [taxResult, setTaxResult] = useState<TaxCalculationResponse | null>(null)
  const [isTaxLoading, setIsTaxLoading] = useState(false)

  const canSubmit =
    cart.items.length > 0 && shippingCity.trim() && shippingCountry.trim() && shippingZipCode.trim()

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

  // Reset tax when country changes
  useEffect(() => {
    setTaxResult(null)
  }, [shippingCountry])

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
      onOrderPlaced()

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

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      {/* Backdrop */}
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      {/* Panel */}
      <div className="relative z-10 flex h-full w-full max-w-lg flex-col overflow-y-auto bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4">
          <h2 className="flex items-center gap-2 text-xl font-bold text-gray-900">
            <MapPin className="h-5 w-5 text-indigo-600" /> Checkout
          </h2>
          <button onClick={onClose} className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-5 py-4">
          <form id="checkout-form" className="space-y-5" onSubmit={(e) => void submitOrder(e)}>
            {/* Shipping */}
            <p className="text-xs font-semibold uppercase tracking-wide text-gray-500">Shipping address</p>
            <div className="grid gap-3 sm:grid-cols-2">
              <Input value={shippingCity} onChange={(e) => setShippingCity(e.target.value)} placeholder="City" />
              <Input value={shippingState} onChange={(e) => setShippingState(e.target.value)} placeholder="State / Province" />
              <Input value={shippingCountry} onChange={(e) => setShippingCountry(e.target.value)} placeholder="Country code (e.g. EG, SA, US)" />
              <Input value={shippingZipCode} onChange={(e) => setShippingZipCode(e.target.value)} placeholder="ZIP / Postal code" />
            </div>

            {/* Payment method */}
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
              <p className="mt-2 text-xs text-gray-500">
                Only Paymob is connected to gateway checkout. VISA and Cash on Delivery are UI-only.
              </p>
            </div>

            {/* Error */}
            {error && (
              <div className="rounded-md border border-red-200 bg-red-50 p-3 text-sm text-red-700">{error}</div>
            )}

            {/* Buttons row */}
            <div className="flex gap-2">
              <Button
                type="button"
                variant="outline"
                disabled={!shippingCountry.trim() || isTaxLoading || cart.items.length === 0}
                onClick={() => void estimateTax()}
              >
                {isTaxLoading ? 'Calculating...' : 'Estimate Tax'}
              </Button>
              <Button
                type="submit"
                className="flex-1"
                disabled={!canSubmit || isSubmitting || cart.items.length === 0}
              >
                {isSubmitting
                  ? 'Placing order...'
                  : paymentMethod === 'PAYMOB'
                    ? 'Place Order & Pay with Paymob'
                    : 'Place Order'}
              </Button>
            </div>

            {/* Tax result */}
            {taxResult && (
              <Card className="border-gray-200 bg-green-50/60">
                <CardHeader className="pb-1 pt-3 px-4">
                  <CardTitle className="flex items-center gap-2 text-sm font-semibold text-gray-800">
                    <Receipt className="h-4 w-4 text-amber-600" /> Tax Estimate
                  </CardTitle>
                </CardHeader>
                <CardContent className="px-4 pb-3 pt-1 text-sm space-y-1">
                  {taxResult.isTaxExempt ? (
                    <p className="font-medium text-green-700">Tax exempt</p>
                  ) : (
                    <>
                      <p>
                        Tax amount: <span className="font-semibold">{formatMoney(taxResult.taxAmount)}</span>
                        {taxResult.taxRate && <span className="ml-1 text-gray-500">({taxResult.taxRate})</span>}
                      </p>
                      <p>
                        Total with tax: <span className="font-semibold">{formatMoney(taxResult.total)}</span>
                      </p>
                      <p>
                        Jurisdiction: <span className="font-bold">{taxResult.jurisdiction}</span>
                        {taxResult.taxType && <span className="ml-1 text-gray-500">&middot; {taxResult.taxType}</span>}
                      </p>
                    </>
                  )}
                </CardContent>
              </Card>
            )}
          </form>
        </div>
      </div>
    </div>
  )
}
