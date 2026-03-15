import { useCallback, useEffect, useRef, useState } from 'react';
import { ArrowRight, RefreshCw, Receipt } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import axios from '@/shared/api/axios';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui';

interface Order {
  orderId: string;
  status: string;
  totalAmount: number;
  createdAt: string;
}

const statusBadgeClass = (status: string) => {
  const value = status.toUpperCase();
  if (value === 'DELIVERED') return 'bg-emerald-50 text-emerald-700 border-emerald-200';
  if (value === 'SHIPPED') return 'bg-blue-50 text-blue-700 border-blue-200';
  if (value === 'PAID') return 'bg-cyan-50 text-cyan-700 border-cyan-200';
  if (value === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200';
  if (value === 'CANCELLED') return 'bg-rose-50 text-rose-700 border-rose-200';
  return 'bg-gray-50 text-gray-700 border-gray-200';
};

const formatAmount = (value: number) => new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
}).format(value ?? 0);

export default function OrdersPage() {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestIdRef = useRef(0);

  const loadOrders = useCallback(async () => {
    const currentRequestId = ++requestIdRef.current;
    setLoading(true);
    setError(null);

    try {
      const res = await axios.get<Order[]>('/orders');
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setOrders(Array.isArray(res.data) ? res.data : []);
    } catch {
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setError('Failed to load orders. Please retry.');
      setOrders([]);
    } finally {
      if (currentRequestId === requestIdRef.current) {
        setLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  if (loading) {
    return <div className="p-8 text-sm text-gray-600">Loading orders...</div>;
  }

  if (error) {
    return (
      <div className="max-w-4xl mx-auto p-4 sm:p-8 space-y-4">
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">{error}</div>
        <Button onClick={loadOrders}>
          <RefreshCw className="mr-2 h-4 w-4" /> Retry
        </Button>
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-5xl p-4 sm:p-8 space-y-4">
      <Card>
        <CardHeader>
          <CardTitle className="text-2xl flex items-center gap-2">
            <Receipt className="h-6 w-6 text-blue-600" /> My Orders
          </CardTitle>
          <CardDescription>Order history from your authenticated orders API.</CardDescription>
        </CardHeader>
        <CardContent>
          {orders.length === 0 ? (
            <div className="text-gray-600">No orders found.</div>
          ) : (
            <div className="space-y-3">
              {orders.map((order) => (
                <div key={order.orderId} className="rounded-lg border border-gray-200 p-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="space-y-1">
                    <div className="text-sm text-gray-500">Order ID</div>
                    <div className="font-semibold">{order.orderId}</div>
                    <div className="text-sm text-gray-500">Placed: {new Date(order.createdAt).toLocaleString()}</div>
                  </div>

                  <div className="flex items-center gap-3">
                    <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium ${statusBadgeClass(order.status)}`}>
                      {order.status}
                    </span>
                    <div className="font-semibold min-w-[100px] text-right">{formatAmount(order.totalAmount)}</div>
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => navigate(`/orders/${order.orderId}`)}
                    >
                      Details <ArrowRight className="ml-1 h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
