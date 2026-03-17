import { useNavigate, useSearchParams } from 'react-router-dom'
import { Clock3 } from 'lucide-react'
import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui'

export default function PaymentPendingPage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const transactionId = searchParams.get('transactionId') || 'N/A'

  return (
    <div className="mx-auto max-w-2xl p-6 sm:p-8">
      <Card className="border-amber-200 bg-amber-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-2xl text-amber-800">
            <Clock3 className="h-6 w-6" /> Payment Pending
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm text-amber-900">
          <p>Your payment is still pending confirmation.</p>
          <p>Transaction ID: <span className="font-semibold">{transactionId}</span></p>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => navigate('/')}>Back to Home</Button>
            <Button variant="outline" onClick={() => navigate('/account?tab=orders')}>Back to My Orders</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
