import { useNavigate, useSearchParams } from 'react-router-dom'
import { CheckCircle2 } from 'lucide-react'
import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui'

export default function PaymentSuccessPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const transactionId = searchParams.get('transactionId') || 'N/A'

  return (
    <div className="mx-auto max-w-2xl p-6 sm:p-8">
      <Card className="border-emerald-200 bg-emerald-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-2xl text-emerald-800">
            <CheckCircle2 className="h-6 w-6" /> Payment Successful
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm text-emerald-900">
          <p>Your payment has been verified successfully.</p>
          <p>Transaction ID: <span className="font-semibold">{transactionId}</span></p>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => navigate('/account?tab=orders')}>Go to My Orders</Button>
            <Button variant="outline" onClick={() => navigate('/')}>Back to Home</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
