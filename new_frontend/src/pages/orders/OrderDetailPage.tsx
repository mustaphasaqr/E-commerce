import { useCallback, useEffect, useRef, useState } from 'react';
import { ArrowLeft, PackageCheck, RefreshCw, Truck } from 'lucide-react';
import { useNavigate, useParams } from 'react-router-dom';
import axios from '@/shared/api/axios';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui';

type OrderItem = {
  productId: string;
  productName: string;
  quantity: number;
  unitPrice: number;
  subtotal: number;
};

type OrderDetail = {
  orderId: string;
  customerId: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  updatedAt: string;
  trackingNumber?: string;
  carrier?: string;
  deliveredAt?: string;
  cancellationReason?: string;
  items?: OrderItem[];
};

const formatAmount = (value: number) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

export default function OrderDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const [order, setOrder] = useState<OrderDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

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
      const res = await axios.get<OrderDetail>(`/orders/${id}`);
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setOrder(res.data);
    } catch {
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setError('Failed to load order details. Please retry.');
      setOrder(null);
    } finally {
      if (currentRequestId === requestIdRef.current) {
        setLoading(false);
      }
    }
  }, [id]);

  useEffect(() => {
    loadOrder();
  }, [loadOrder]);

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

  if (!order) {
    return null;
  }

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
          <CardDescription>Detailed order information from backend order API.</CardDescription>
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
    </div>
  );
}
