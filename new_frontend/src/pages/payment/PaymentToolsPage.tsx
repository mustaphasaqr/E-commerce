import { FormEvent, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle, Input } from '@/shared/components/ui'
import paymentService from '@/features/payments/api/paymentService'
import { getLastCheckoutId } from '@/features/payments/api/paymentService'

export default function PaymentToolsPage() {
  const navigate = useNavigate()
  const [checkoutId, setCheckoutId] = useState('')
  const [result, setResult] = useState<string>('')
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    setCheckoutId(getLastCheckoutId())
  }, [])

  const run = async (title: string, action: () => Promise<unknown>) => {
    setIsLoading(true)
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
    if (!checkoutId.trim()) {
      setResult('Please enter checkout ID first.')
      return
    }

    await run('PaymentController verify (/api/v1/payments/verify)', () =>
      paymentService.verifyCheckout(checkoutId.trim())
    )
  }

  const submitWebhookVerify = async (event: FormEvent) => {
    event.preventDefault()
    if (!checkoutId.trim()) {
      setResult('Please enter checkout ID first.')
      return
    }

    await run('Webhook verify (/api/webhooks/payment/verify)', () =>
      paymentService.webhookVerify(checkoutId.trim())
    )
  }

  return (
    <div className="mx-auto max-w-5xl p-4 sm:p-8 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle>Payment Tools</CardTitle>
          <CardDescription>
            Test payment and webhook endpoints, and navigate payment result pages.
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex flex-wrap gap-2">
            <Button disabled={isLoading} onClick={() => void run('Payment health (/api/v1/payments/health)', () => paymentService.paymentHealth())}>
              Check Payment Health
            </Button>
            <Button variant="outline" disabled={isLoading} onClick={() => void run('Webhook health (/api/webhooks/payment/health)', () => paymentService.webhookHealth())}>
              Check Webhook Health
            </Button>
          </div>

          <form className="flex flex-col gap-2 sm:flex-row" onSubmit={(event) => void submitVerify(event)}>
            <Input
              value={checkoutId}
              onChange={(event) => setCheckoutId(event.target.value)}
              placeholder="Enter checkoutId"
            />
            <Button type="submit" disabled={isLoading}>Verify via PaymentController</Button>
            <Button type="button" variant="outline" disabled={isLoading} onClick={() => setCheckoutId(getLastCheckoutId())}>
              Use Last Checkout ID
            </Button>
          </form>

          <form className="flex flex-col gap-2 sm:flex-row" onSubmit={(event) => void submitWebhookVerify(event)}>
            <Input
              value={checkoutId}
              onChange={(event) => setCheckoutId(event.target.value)}
              placeholder="Enter checkoutId"
            />
            <Button type="submit" disabled={isLoading} variant="outline">Verify via WebhookController</Button>
          </form>

          <div className="flex flex-wrap gap-2">
            <Button variant="outline" onClick={() => navigate('/payment/success?transactionId=sample_tx')}>Open Success UI</Button>
            <Button variant="outline" onClick={() => navigate('/payment/pending?transactionId=sample_tx')}>Open Pending UI</Button>
            <Button variant="outline" onClick={() => navigate('/payment/failure?reason=sample_failure')}>Open Failure UI</Button>
            <Button variant="outline" onClick={() => navigate('/payment/cancelled')}>Open Cancelled UI</Button>
          </div>

          <pre className="min-h-[180px] overflow-auto rounded-lg border border-gray-200 bg-gray-50 p-3 text-xs text-gray-800">{result || 'Results will appear here.'}</pre>
        </CardContent>
      </Card>
    </div>
  )
}
