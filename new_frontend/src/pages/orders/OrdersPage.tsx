import { useEffect, useState } from 'react';
import axios from 'axios';

interface Order {
  id: string;
  status: string;
  totalAmount: number;
  createdAt: string;
  // Add more fields as needed
}

export default function OrdersPage() {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    axios.get('/api/v1/orders')
      .then(res => {
        setOrders(res.data);
        setLoading(false);
      })
      .catch(err => {
        setError('Failed to load orders');
        setLoading(false);
      });
  }, []);

  if (loading) return <div className="p-8">Loading orders...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-2xl mx-auto p-8">
      <h2 className="text-2xl font-bold mb-4">My Orders</h2>
      {orders.length === 0 ? (
        <div className="text-gray-600">No orders found.</div>
      ) : (
        <div className="space-y-4">
          {orders.map(order => (
            <div key={order.id} className="bg-white rounded shadow p-4 flex justify-between items-center">
              <div>
                <div><strong>Order ID:</strong> {order.id}</div>
                <div><strong>Status:</strong> {order.status}</div>
                <div><strong>Date:</strong> {new Date(order.createdAt).toLocaleDateString()}</div>
              </div>
              <div className="text-lg font-bold">${order.totalAmount.toFixed(2)}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
