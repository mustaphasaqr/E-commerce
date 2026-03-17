import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Loader2 } from 'lucide-react'
import { Button } from '@/shared/components/ui'
import paymentService from '@/features/payments/api/paymentService'
import { setLastCheckoutId } from '@/features/payments/api/paymentService'

export default function PaymentReturnPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const [message, setMessage] = useState('Verifying payment with backend...')
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const verify = async () => {
      const checkoutId = searchParams.get('id') || searchParams.get('checkoutId')

      if (!checkoutId) {
        setError('Missing checkout ID in return URL.')
        return
      }

      try {
        setLastCheckoutId(checkoutId)
        const result = await paymentService.verifyCheckout(checkoutId)
        const status = String(result.status || '').toUpperCase()
        const txId = encodeURIComponent(result.transactionId ?? '')
        const reason = encodeURIComponent(result.message || result.error || 'payment_verification_failed')

        if (status === 'SUCCESS') {
          navigate(`/payment/success?transactionId=${txId}`, { replace: true })
          return
        }
        if (status === 'PENDING') {
          navigate(`/payment/pending?transactionId=${txId}`, { replace: true })
          return
        }
        if (status === 'CANCELLED') {
          navigate('/payment/cancelled', { replace: true })
          return
        }

        navigate(`/payment/failure?reason=${reason}`, { replace: true })
      } catch {
        setError('Failed to verify payment. You can retry or check status from Payment Tools.')
      }
    }

    void verify()
  }, [navigate, searchParams])

  if (error) {
    return (
      <div className="mx-auto max-w-xl p-8 space-y-4">
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">{error}</div>
        <div className="flex gap-2">
          <Button onClick={() => navigate('/payment/tools')}>Open Payment Tools</Button>
          <Button variant="outline" onClick={() => navigate('/account?tab=orders')}>Back to Orders</Button>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-xl p-8">
      <div className="rounded-xl border border-blue-200 bg-blue-50 p-6 text-blue-900 flex items-center gap-3">
        <Loader2 className="h-5 w-5 animate-spin" />
        <span>{message}</span>
      </div>
    </div>
  )
}
