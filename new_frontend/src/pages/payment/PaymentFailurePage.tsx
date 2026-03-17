import { useNavigate, useSearchParams } from 'react-router-dom'
import { AlertCircle } from 'lucide-react'
import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui'

export default function PaymentFailurePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const reason = searchParams.get('reason') || searchParams.get('error') || 'payment_failed'

  return (
    <div className="mx-auto max-w-2xl p-6 sm:p-8">
      <Card className="border-rose-200 bg-rose-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-2xl text-rose-800">
            <AlertCircle className="h-6 w-6" /> Payment Failed
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm text-rose-900">
          <p>The payment did not complete successfully.</p>
          <p>Reason: <span className="font-semibold break-all">{reason}</span></p>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => navigate('/')}>Back to Home</Button>
            <Button variant="outline" onClick={() => navigate('/cart')}>Retry Checkout</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
