import { useNavigate } from 'react-router-dom'
import { Ban } from 'lucide-react'
import { Button, Card, CardContent, CardHeader, CardTitle } from '@/shared/components/ui'

export default function PaymentCancelledPage() {
  const navigate = useNavigate()

  return (
    <div className="mx-auto max-w-2xl p-6 sm:p-8">
      <Card className="border-slate-300 bg-slate-50">
        <CardHeader>
          <CardTitle className="flex items-center gap-2 text-2xl text-slate-800">
            <Ban className="h-6 w-6" /> Payment Cancelled
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-4 text-sm text-slate-800">
          <p>The payment was cancelled before completion.</p>
          <div className="flex flex-wrap gap-2">
            <Button onClick={() => navigate('/cart')}>Return to Checkout</Button>
            <Button variant="outline" onClick={() => navigate('/account?tab=orders')}>Back to My Orders</Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
