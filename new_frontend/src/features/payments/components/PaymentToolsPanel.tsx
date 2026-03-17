import { FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AlertCircle, Ban, CheckCircle2, Clock3, CreditCard, X } from 'lucide-react'
import { Button, Card, CardContent, Input } from '@/shared/components/ui'
import paymentService from '@/features/payments/api/paymentService'
import { getLastCheckoutId } from '@/features/payments/api/paymentService'

type StatusPreview = 'success' | 'pending' | 'failure' | 'cancelled' | null

interface PaymentToolsPanelProps {
  onClose: () => void
  onOpenCart: () => void
  onOpenAccount: () => void
}

export default function PaymentToolsPanel({ onClose, onOpenCart, onOpenAccount }: PaymentToolsPanelProps) {
  const navigate = useNavigate()
  const [checkoutId, setCheckoutId] = useState('')
  const [result, setResult] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [statusPreview, setStatusPreview] = useState<StatusPreview>(null)

  useEffect(() => {
    setCheckoutId(getLastCheckoutId())
  }, [])

  const run = async (title: string, action: () => Promise<unknown>) => {
    setIsLoading(true)
    setStatusPreview(null)
    try {
      const data = await action()
      setResult(`${title}\n${JSON.stringify(data, null, 2)}`)
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Request failed'
      setResult(`${title}\nERROR: ${message}`)
    } finally {
      setIsLoading(false)
    }
  }

  const submitVerify = async (event: FormEvent) => {
    event.preventDefault()
    if (!checkoutId.trim()) { setResult('Please enter checkout ID first.'); return }
    await run('PaymentController verify', () => paymentService.verifyCheckout(checkoutId.trim()))
  }

  const submitWebhookVerify = async (event: FormEvent) => {
    event.preventDefault()
    if (!checkoutId.trim()) { setResult('Please enter checkout ID first.'); return }
    await run('Webhook verify', () => paymentService.webhookVerify(checkoutId.trim()))
  }

  return (
    <div className="fixed inset-0 z-50 flex justify-end">
      <div className="absolute inset-0 bg-black/40" onClick={onClose} />

      <div className="relative z-10 flex h-full w-full max-w-lg flex-col bg-white shadow-2xl">
        {/* Header */}
        <div className="flex items-center justify-between border-b border-gray-200 px-5 py-4">
          <h2 className="flex items-center gap-2 text-xl font-bold text-gray-900">
            <CreditCard className="h-5 w-5 text-indigo-600" /> Payment Tools
          </h2>
          <button onClick={onClose} className="rounded-md p-1 text-gray-400 hover:bg-gray-100 hover:text-gray-600">
            <X className="h-5 w-5" />
          </button>
        </div>

        {/* Body */}
        <div className="flex-1 overflow-y-auto px-5 py-4 space-y-4">
          {/* Back from status preview */}
          {statusPreview && (
            <Button variant="outline" size="sm" onClick={() => setStatusPreview(null)}>
              ← Back to Tools
            </Button>
          )}

          {/* Status previews */}
          {statusPreview === 'success' && (
            <Card className="border-emerald-200 bg-emerald-50">
              <CardContent className="pt-4 space-y-3 text-sm text-emerald-900">
                <h3 className="flex items-center gap-2 text-lg font-bold text-emerald-800">
                  <CheckCircle2 className="h-5 w-5" /> Payment Successful
                </h3>
                <p>Your payment has been verified successfully.</p>
                <p>Transaction ID: <span className="font-semibold">sample_tx</span></p>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" onClick={() => { onClose(); onOpenAccount() }}>Go to My Orders</Button>
                  <Button size="sm" variant="outline" onClick={() => navigate('/')}>Back to Home</Button>
                </div>
              </CardContent>
            </Card>
          )}

          {statusPreview === 'pending' && (
            <Card className="border-amber-200 bg-amber-50">
              <CardContent className="pt-4 space-y-3 text-sm text-amber-900">
                <h3 className="flex items-center gap-2 text-lg font-bold text-amber-800">
                  <Clock3 className="h-5 w-5" /> Payment Pending
                </h3>
                <p>Your payment is still pending confirmation.</p>
                <p>Transaction ID: <span className="font-semibold">sample_tx</span></p>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" onClick={() => setStatusPreview(null)}>Check Status</Button>
                  <Button size="sm" variant="outline" onClick={() => { onClose(); onOpenAccount() }}>My Orders</Button>
                </div>
              </CardContent>
            </Card>
          )}

          {statusPreview === 'failure' && (
            <Card className="border-rose-200 bg-rose-50">
              <CardContent className="pt-4 space-y-3 text-sm text-rose-900">
                <h3 className="flex items-center gap-2 text-lg font-bold text-rose-800">
                  <AlertCircle className="h-5 w-5" /> Payment Failed
                </h3>
                <p>The payment did not complete successfully.</p>
                <p>Reason: <span className="font-semibold">sample_failure</span></p>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" onClick={() => setStatusPreview(null)}>Back to Tools</Button>
                  <Button size="sm" variant="outline" onClick={() => { onClose(); onOpenCart() }}>Retry Checkout</Button>
                </div>
              </CardContent>
            </Card>
          )}

          {statusPreview === 'cancelled' && (
            <Card className="border-slate-300 bg-slate-50">
              <CardContent className="pt-4 space-y-3 text-sm text-slate-800">
                <h3 className="flex items-center gap-2 text-lg font-bold text-slate-800">
                  <Ban className="h-5 w-5" /> Payment Cancelled
                </h3>
                <p>The payment was cancelled before completion.</p>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" onClick={() => { onClose(); onOpenCart() }}>Return to Checkout</Button>
                  <Button size="sm" variant="outline" onClick={() => { onClose(); onOpenAccount() }}>My Orders</Button>
                </div>
              </CardContent>
            </Card>
          )}

          {/* Main tools (hidden during preview) */}
          {!statusPreview && (
            <>
              {/* Health checks */}
              <div className="flex flex-wrap gap-2">
                <Button size="sm" disabled={isLoading} onClick={() => void run('Payment health', () => paymentService.paymentHealth())}>
                  Check Payment Health
                </Button>
                <Button size="sm" variant="outline" disabled={isLoading} onClick={() => void run('Webhook health', () => paymentService.webhookHealth())}>
                  Check Webhook Health
                </Button>
              </div>

              {/* Verify via PaymentController */}
              <form className="space-y-2" onSubmit={(e) => void submitVerify(e)}>
                <Input
                  value={checkoutId}
                  onChange={(e) => setCheckoutId(e.target.value)}
                  placeholder="Enter checkoutId"
                  className="text-xs"
                />
                <div className="flex flex-wrap gap-2">
                  <Button type="submit" size="sm" disabled={isLoading}>Verify via PaymentController</Button>
                  <Button type="button" size="sm" variant="outline" disabled={isLoading} onClick={() => setCheckoutId(getLastCheckoutId())}>
                    Use Last Checkout ID
                  </Button>
                </div>
              </form>

              {/* Verify via WebhookController */}
              <form className="space-y-2" onSubmit={(e) => void submitWebhookVerify(e)}>
                <Input
                  value={checkoutId}
                  onChange={(e) => setCheckoutId(e.target.value)}
                  placeholder="Enter checkoutId"
                  className="text-xs"
                />
                <Button type="submit" size="sm" variant="outline" disabled={isLoading}>Verify via WebhookController</Button>
              </form>

              {/* Status preview buttons */}
              <div className="space-y-2">
                <p className="text-xs font-medium text-gray-500 uppercase tracking-wide">Preview Status Pages</p>
                <div className="flex flex-wrap gap-2">
                  <Button size="sm" variant="outline" onClick={() => setStatusPreview('success')}>Success UI</Button>
                  <Button size="sm" variant="outline" onClick={() => setStatusPreview('pending')}>Pending UI</Button>
                  <Button size="sm" variant="outline" onClick={() => setStatusPreview('failure')}>Failure UI</Button>
                  <Button size="sm" variant="outline" onClick={() => setStatusPreview('cancelled')}>Cancelled UI</Button>
                </div>
              </div>

              {/* Results */}
              <pre className="min-h-[120px] overflow-auto rounded-lg border border-gray-200 bg-gray-50 p-3 text-xs text-gray-800 break-all whitespace-pre-wrap">
                {result || 'Results will appear here.'}
              </pre>
            </>
          )}
        </div>
      </div>
    </div>
  )
}
