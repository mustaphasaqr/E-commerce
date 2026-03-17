import { useCallback, useEffect, useRef, useState } from 'react';
import { ArrowLeft, PackageCheck, RefreshCw, Truck, XCircle, CreditCard, Send, CheckCircle2 } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import { orderService } from '@/features/orders/api/orderService';
import { paymentService, setLastCheckoutId } from '@/features/payments/api/paymentService';
import type { OrderResponse } from '@/features/orders/types';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui';

const formatAmount = (value: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

export default function OrderDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [actionSuccess, setActionSuccess] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  const [cancelReason, setCancelReason] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'VISA' | 'MASTERCARD' | 'MADA'>('VISA');
  const [trackingNumber, setTrackingNumber] = useState('');
  const [carrier, setCarrier] = useState('');

  const loadOrder = useCallback(async () => {
    if (!id) {
      setError('Missing order ID');
      setLoading(false);
      return;
    }

    const currentRequestId = ++requestIdRef.current;
    setLoading(true);
    setError(null);

    try {
      const data = await orderService.getOrder(id);
      if (currentRequestId !== requestIdRef.current) return;
      setOrder(data);
    } catch {
      if (currentRequestId !== requestIdRef.current) return;
      setError('Failed to load order details. Please retry.');
      setOrder(null);
    } finally {
      if (currentRequestId === requestIdRef.current) setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

  const runAction = async (name: string, fn: () => Promise<OrderResponse | void>) => {
    setActionLoading(name);
    setActionError(null);
    setActionSuccess(null);
    try {
      const result = await fn();
      if (result) setOrder(result);
      else await loadOrder();
      setActionSuccess(`${name} completed successfully`);
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Action failed';
      setActionError(`${name} failed: ${msg}`);
    } finally {
      setActionLoading(null);
    }
  };

  const handleCancel = () => {
    if (!id || !cancelReason.trim()) return;
    void runAction('Cancel', () => orderService.cancelOrder(id, cancelReason.trim()));
  };

  const handlePay = () => {
    if (!id || !order) return;
    void runAction('Pay', () =>
      orderService.payOrder(id, {
        paymentMethod,
        paymentToken: `tok_${Date.now()}`,
        amount: order.totalAmount,
      })
    );
  };

  const handleShip = () => {
    if (!id || !trackingNumber.trim() || !carrier.trim()) return;
    void runAction('Ship', () =>
      orderService.shipOrder(id, {
        trackingNumber: trackingNumber.trim(),
        carrier: carrier.trim(),
      })
    );
  };

  const handleDeliver = () => {
    if (!id) return;
    void runAction('Deliver', () => orderService.deliverOrder(id));
  };

  const handlePaymobCheckout = async () => {
    if (!id || !order) return;
    setActionLoading('Paymob Checkout');
    setActionError(null);
    setActionSuccess(null);
    try {
      const res = await paymentService.initiateCheckout({
        orderId: id,
        paymentMethod,
        customerEmail: order.customerId + '@customer.local',
        shopperResultUrl: window.location.origin + '/payment/return',
      });
      if (res.success && res.checkoutId) {
        setLastCheckoutId(res.checkoutId);
        if (res.redirectUrl) {
          window.location.href = res.redirectUrl;
          return;
        }
        setActionSuccess(`Checkout created: ${res.checkoutId}`);
      } else {
        setActionError(res.error || res.message || 'Checkout initiation failed');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : 'Paymob checkout failed';
      setActionError(msg);
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) {
    return <div className="p-8 text-sm text-gray-600">Loading order details...</div>;
  }

  if (error) {
    return (
      <div className="max-w-3xl mx-auto p-4 sm:p-8 space-y-4">
        <Button variant="outline" onClick={() => navigate('/orders')}>
          <ArrowLeft className="mr-2 h-4 w-4" /> Back to Orders
        </Button>
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">{error}</div>
        <Button onClick={loadOrder}>
          <RefreshCw className="mr-2 h-4 w-4" /> Retry
        </Button>
      </div>
    );
  }

  if (!order) return null;

  const status = order.status?.toUpperCase() ?? '';

  return (
    <div className="mx-auto max-w-4xl p-4 sm:p-8 space-y-4">
      <Button variant="outline" onClick={() => navigate('/orders')}>
        <ArrowLeft className="mr-2 h-4 w-4" /> Back to Orders
      </Button>

      <Card>
        <CardHeader>
          <CardTitle className="text-2xl flex items-center gap-2">
            <PackageCheck className="h-6 w-6 text-blue-600" /> Order {order.orderId}
          </CardTitle>
          <CardDescription>Order details and lifecycle actions</CardDescription>
        </CardHeader>
        <CardContent className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <div className="rounded-lg border p-4">
            <p className="text-gray-500">Status</p>
            <p className="font-semibold mt-1">{order.status}</p>
          </div>
          <div className="rounded-lg border p-4">
            <p className="text-gray-500">Total Amount</p>
            <p className="font-semibold mt-1">{formatAmount(order.totalAmount)}</p>
          </div>
          <div className="rounded-lg border p-4">
            <p className="text-gray-500">Created</p>
            <p className="font-semibold mt-1">{new Date(order.createdAt).toLocaleString()}</p>
          </div>
          <div className="rounded-lg border p-4">
            <p className="text-gray-500">Updated</p>
            <p className="font-semibold mt-1">{new Date(order.updatedAt).toLocaleString()}</p>
          </div>
        </CardContent>
      </Card>

      {order.items && order.items.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Items</CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-sm">
            {order.items.map((item, index) => (
              <div key={`${item.productId}-${index}`} className="rounded-lg border p-3 flex items-center justify-between">
                <div>
                  <p className="font-medium">{item.productName}</p>
                  <p className="text-gray-500">Qty: {item.quantity}</p>
                </div>
                <div className="font-semibold">{formatAmount(item.subtotal)}</div>
              </div>
            ))}
          </CardContent>
        </Card>
      )}

      {(order.trackingNumber || order.carrier) && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Truck className="h-5 w-5 text-blue-600" /> Shipping
            </CardTitle>
          </CardHeader>
          <CardContent className="text-sm space-y-1">
            <p>Carrier: <span className="font-semibold">{order.carrier ?? 'N/A'}</span></p>
            <p>Tracking: <span className="font-semibold">{order.trackingNumber ?? 'N/A'}</span></p>
          </CardContent>
        </Card>
      )}

      {actionError && (
        <div className="rounded-md border border-red-200 bg-red-50 p-3 text-red-700 text-sm">{actionError}</div>
      )}
      {actionSuccess && (
        <div className="rounded-md border border-green-200 bg-green-50 p-3 text-green-700 text-sm">{actionSuccess}</div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Order Actions</CardTitle>
          <CardDescription>Trigger order lifecycle endpoints directly</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">

          {(status === 'PENDING' || status === 'CREATED') && (
            <div className="space-y-2 p-4 rounded-lg border border-red-100 bg-red-50/30">
              <p className="font-medium text-sm flex items-center gap-2">
                <XCircle className="h-4 w-4 text-red-500" /> Cancel Order
              </p>
              <input
                type="text"
                placeholder="Cancellation reason"
                value={cancelReason}
                onChange={(e) => setCancelReason(e.target.value)}
                className="w-full px-3 py-2 border rounded-md text-sm"
              />
              <Button
                variant="destructive"
                size="sm"
                onClick={handleCancel}
                disabled={!!actionLoading || !cancelReason.trim()}
              >
                {actionLoading === 'Cancel' ? 'Cancelling...' : 'Cancel Order'}
              </Button>
            </div>
          )}

          {(status === 'PENDING' || status === 'CREATED') && (
            <div className="space-y-2 p-4 rounded-lg border border-blue-100 bg-blue-50/30">
              <p className="font-medium text-sm flex items-center gap-2">
                <CreditCard className="h-4 w-4 text-blue-500" /> Pay Order
              </p>
              <select
                value={paymentMethod}
                onChange={(e) => setPaymentMethod(e.target.value as 'VISA' | 'MASTERCARD' | 'MADA')}
                className="w-full px-3 py-2 border rounded-md text-sm"
              >
                <option value="VISA">VISA</option>
                <option value="MASTERCARD">MASTERCARD</option>
                <option value="MADA">MADA</option>
              </select>
              <div className="flex gap-2">
                <Button size="sm" onClick={handlePay} disabled={!!actionLoading}>
                  {actionLoading === 'Pay' ? 'Processing...' : 'Mark as Paid'}
                </Button>
                <Button size="sm" variant="outline" onClick={() => void handlePaymobCheckout()} disabled={!!actionLoading}>
                  {actionLoading === 'Paymob Checkout' ? 'Redirecting...' : 'Paymob Checkout'}
                </Button>
              </div>
            </div>
          )}

          {status === 'PAID' && (
            <div className="space-y-2 p-4 rounded-lg border border-indigo-100 bg-indigo-50/30">
              <p className="font-medium text-sm flex items-center gap-2">
                <Send className="h-4 w-4 text-indigo-500" /> Ship Order
              </p>
              <input
                type="text"
                placeholder="Tracking number"
                value={trackingNumber}
                onChange={(e) => setTrackingNumber(e.target.value)}
                className="w-full px-3 py-2 border rounded-md text-sm"
              />
              <input
                type="text"
                placeholder="Carrier (e.g. Aramex, DHL)"
                value={carrier}
                onChange={(e) => setCarrier(e.target.value)}
                className="w-full px-3 py-2 border rounded-md text-sm"
              />
              <Button
                size="sm"
                onClick={handleShip}
                disabled={!!actionLoading || !trackingNumber.trim() || !carrier.trim()}
              >
                {actionLoading === 'Ship' ? 'Shipping...' : 'Mark as Shipped'}
              </Button>
            </div>
          )}

          {status === 'SHIPPED' && (
            <div className="space-y-2 p-4 rounded-lg border border-green-100 bg-green-50/30">
              <p className="font-medium text-sm flex items-center gap-2">
                <CheckCircle2 className="h-4 w-4 text-green-500" /> Deliver Order
              </p>
              <Button size="sm" onClick={handleDeliver} disabled={!!actionLoading}>
                {actionLoading === 'Deliver' ? 'Delivering...' : 'Mark as Delivered'}
              </Button>
            </div>
          )}

          {(status === 'DELIVERED' || status === 'CANCELLED') && (
            <p className="text-sm text-gray-500 italic">
              This order is {status.toLowerCase()}. No further actions available.
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
