import { useCallback, useEffect, useRef, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Activity, ArrowRight, Crown, Mail, Package2, RefreshCw, ShieldCheck, User } from 'lucide-react';
import axios from '@/shared/api/axios';
import { Button, Card, CardContent } from '@/shared/components/ui';

interface UserProfile {
  id: string;
  email: string;
  username: string;
  role: string;
  status: string;
  emailVerified: boolean;
  createdAt: string;
  updatedAt?: string;
}

type OrderListItem = {
  orderId: string;
  status: string;
  totalAmount: number;
  createdAt: string;
};

export default function AccountPage() {
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [orders, setOrders] = useState<OrderListItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [ordersLoading, setOrdersLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [ordersError, setOrdersError] = useState<string | null>(null);
  const requestIdRef = useRef(0);
  const ordersRequestIdRef = useRef(0);

  const tabParam = searchParams.get('tab');
  const activeTab = tabParam === 'orders' ? 'orders' : 'account';

  const loadProfile = useCallback(async () => {
    const currentRequestId = ++requestIdRef.current;
    setLoading(true);
    setError(null);

    try {
      const res = await axios.get<UserProfile>('/users/me');
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setProfile(res.data);
    } catch {
      if (currentRequestId !== requestIdRef.current) {
        return;
      }
      setError('Failed to load account info. Please retry.');
    } finally {
      if (currentRequestId === requestIdRef.current) {
        setLoading(false);
      }
    }
  }, []);

  const loadOrders = useCallback(async () => {
    const currentRequestId = ++ordersRequestIdRef.current;
    setOrdersLoading(true);
    setOrdersError(null);

    try {
      const res = await axios.get<OrderListItem[]>('/orders');
      if (currentRequestId !== ordersRequestIdRef.current) {
        return;
      }
      setOrders(Array.isArray(res.data) ? res.data : []);
    } catch {
      if (currentRequestId !== ordersRequestIdRef.current) {
        return;
      }
      setOrdersError('Failed to load your orders. Please retry.');
      setOrders([]);
    } finally {
      if (currentRequestId === ordersRequestIdRef.current) {
        setOrdersLoading(false);
      }
    }
  }, []);

  useEffect(() => {
    loadProfile();
  }, [loadProfile]);

  useEffect(() => {
    if (activeTab === 'orders') {
      loadOrders();
    }
  }, [activeTab, loadOrders]);

  if (loading) {
    return <div className="p-8 text-sm text-gray-600">Loading account info...</div>;
  }

  if (error) {
    return (
      <div className="max-w-xl mx-auto p-8 space-y-4">
        <div className="rounded-md border border-red-200 bg-red-50 p-4 text-red-700">{error}</div>
        <Button onClick={loadProfile}>
          <RefreshCw className="mr-2 h-4 w-4" /> Retry
        </Button>
      </div>
    );
  }

  if (!profile) {
    return null;
  }

  const isOwner = profile.role === 'OWNER';
  const roleBadgeClass =
    profile.role === 'OWNER'
      ? 'bg-indigo-100 text-indigo-800 border-indigo-200'
      : 'bg-emerald-100 text-emerald-800 border-emerald-200';
  const statusBadgeClass =
    profile.status === 'ACTIVE'
      ? 'bg-emerald-100 text-emerald-800 border-emerald-200'
      : 'bg-amber-100 text-amber-800 border-amber-200';

  const orderStatusBadgeClass = (status: string) => {
    const value = status.toUpperCase();
    if (value === 'DELIVERED') return 'bg-emerald-50 text-emerald-700 border-emerald-200';
    if (value === 'SHIPPED') return 'bg-blue-50 text-blue-700 border-blue-200';
    if (value === 'PAID') return 'bg-cyan-50 text-cyan-700 border-cyan-200';
    if (value === 'PENDING') return 'bg-amber-50 text-amber-700 border-amber-200';
    if (value === 'CANCELLED') return 'bg-rose-50 text-rose-700 border-rose-200';
    return 'bg-gray-50 text-gray-700 border-gray-200';
  };

  const openAccountTab = () => setSearchParams({ tab: 'account' });
  const openOrdersTab = () => setSearchParams({ tab: 'orders' });

  const formatAmount = (value: number) =>
    new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value ?? 0);

  return (
    <div className="relative mx-auto max-w-[90rem] p-4 sm:p-8 space-y-5">
      <div className="pointer-events-none absolute -top-8 left-1/2 h-40 w-40 -translate-x-1/2 rounded-full bg-blue-300/25 blur-3xl" />

      <Card className="overflow-hidden border-slate-200">
        <div className="bg-gradient-to-r from-slate-900 via-blue-900 to-indigo-900 text-white p-6 sm:p-7">
          <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
            <div>
              <h2 className="text-3xl font-extrabold tracking-tight">My Hub</h2>
              <p className="mt-2 text-blue-100/90">
                {isOwner
                  ? 'Owner identity, quick controls, and order access in one place.'
                  : 'Your account and order history in one unified panel.'}
              </p>
            </div>
            <Button
              variant="secondary"
              className="bg-white text-slate-900 hover:bg-slate-100"
              onClick={activeTab === 'orders' ? loadOrders : loadProfile}
            >
              <RefreshCw className="mr-2 h-4 w-4" /> Refresh
            </Button>
          </div>

          <div className="mt-5 flex flex-wrap gap-2">
            <Button
              variant="outline"
              className={
                activeTab === 'account'
                  ? 'border-white bg-white text-slate-900 hover:bg-slate-100'
                  : 'border-white/50 bg-white/10 text-white hover:bg-white/20 hover:text-white'
              }
              onClick={openAccountTab}
            >
              <User className="mr-2 h-4 w-4" /> My Account
            </Button>
            <Button
              variant="outline"
              className={
                activeTab === 'orders'
                  ? 'border-white bg-white text-slate-900 hover:bg-slate-100'
                  : 'border-white/50 bg-white/10 text-white hover:bg-white/20 hover:text-white'
              }
              onClick={openOrdersTab}
            >
              <Package2 className="mr-2 h-4 w-4" /> My Orders
            </Button>
          </div>
        </div>

        <CardContent className="p-5 sm:p-6 text-sm">
          {activeTab === 'account' ? (
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <div className="rounded-xl border border-blue-200 bg-gradient-to-br from-white to-blue-50 p-4 space-y-3 shadow-sm">
                <div className="inline-flex rounded-full border border-blue-200 bg-blue-100 px-2.5 py-1 text-xs font-semibold uppercase tracking-wide text-blue-800">
                  Identity
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-blue-700"><User className="h-4 w-4" /> Username</div>
                  <p className="text-lg font-semibold text-slate-900">{profile.username}</p>
                </div>
                <div className="space-y-2">
                  <div className="flex items-center gap-2 text-blue-700"><Mail className="h-4 w-4" /> Email</div>
                  <p className="text-lg font-semibold text-slate-900 break-all">{profile.email}</p>
                </div>
                <div className="pt-1 text-slate-600">Joined: {new Date(profile.createdAt).toLocaleString()}</div>
              </div>

              <div className="rounded-xl border border-indigo-200 bg-gradient-to-br from-white to-indigo-50 p-4 space-y-3 shadow-sm">
                <div className="inline-flex rounded-full border border-indigo-200 bg-indigo-100 px-2.5 py-1 text-xs font-semibold uppercase tracking-wide text-indigo-800">
                  Access Profile
                </div>
                <div className="flex flex-wrap items-center gap-2">
                  <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${roleBadgeClass}`}>
                    {profile.role === 'OWNER' ? <Crown className="mr-1 h-3.5 w-3.5" /> : null}
                    {profile.role}
                  </span>
                  <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-semibold ${statusBadgeClass}`}>
                    <Activity className="mr-1 h-3.5 w-3.5" /> {profile.status}
                  </span>
                </div>

                {!isOwner && (
                  <div className="rounded-lg border border-emerald-200 bg-emerald-50 p-3">
                    <p className="text-emerald-800 flex items-center gap-2"><ShieldCheck className="h-4 w-4" /> Email Verification</p>
                    <p className="font-semibold mt-1 text-slate-900">{profile.emailVerified ? 'Verified' : 'Not verified yet'}</p>
                  </div>
                )}

                {isOwner && (
                  <div className="rounded-lg border border-indigo-200 bg-indigo-50 p-3 text-indigo-800">
                    Owner accounts are trusted administrative identities. Email verification status is hidden for this role.
                  </div>
                )}
              </div>

              {isOwner && (
                <div className="lg:col-span-2 rounded-xl border border-slate-200 bg-gradient-to-r from-blue-50 to-indigo-50 p-4">
                  <p className="text-slate-700 font-semibold mb-3">Owner Quick Actions</p>
                  <div className="flex flex-wrap gap-2">
                    <Button onClick={() => navigate('/admin')}>
                      Commerce Insights <ArrowRight className="ml-1 h-4 w-4" />
                    </Button>
                    <Button variant="outline" onClick={() => navigate('/admin/users')}>
                      Manage Users <ArrowRight className="ml-1 h-4 w-4" />
                    </Button>
                    <Button variant="outline" onClick={() => navigate('/admin/orders')}>
                      Review Orders <ArrowRight className="ml-1 h-4 w-4" />
                    </Button>
                  </div>
                </div>
              )}
            </div>
          ) : (
            <div className="rounded-xl border border-cyan-200 bg-gradient-to-br from-white via-cyan-50 to-blue-50 p-4 sm:p-5 space-y-4 shadow-sm">
              <div className="flex items-center justify-between">
                <div className="inline-flex rounded-full border border-cyan-200 bg-cyan-100 px-2.5 py-1 text-xs font-semibold uppercase tracking-wide text-cyan-800">
                  Orders
                </div>
                <div className="inline-flex items-center gap-1 rounded-full border border-blue-200 bg-blue-100 px-2.5 py-1 text-xs font-medium text-blue-700">
                  <Package2 className="h-3.5 w-3.5" />
                  {orders.length} items
                </div>
              </div>

              {ordersLoading ? (
                <div className="rounded-lg border border-blue-200 bg-white/80 p-4 text-sm text-slate-700">Loading orders...</div>
              ) : ordersError ? (
                <div className="space-y-3">
                  <div className="rounded-md border border-red-200 bg-red-50 p-3 text-red-700">{ordersError}</div>
                  <Button onClick={loadOrders}>
                    <RefreshCw className="mr-2 h-4 w-4" /> Retry Orders
                  </Button>
                </div>
              ) : orders.length === 0 ? (
                <div className="rounded-xl border border-dashed border-cyan-300 bg-white/90 p-6 text-center space-y-2">
                  <div className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-cyan-100 text-cyan-700">
                    <Package2 className="h-5 w-5" />
                  </div>
                  <p className="text-base font-semibold text-slate-800">No orders found yet</p>
                  <p className="text-sm text-slate-600">When you place your first order, it will appear here.</p>
                  <div className="pt-1">
                    <Button variant="outline" onClick={() => navigate('/products')}>
                      Browse Products <ArrowRight className="ml-1 h-3.5 w-3.5" />
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="space-y-2">
                  {orders.map((order) => (
                    <div key={order.orderId} className="rounded-lg border border-blue-200 bg-white/95 p-3 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between hover:shadow-sm transition-shadow">
                      <div>
                        <p className="font-semibold text-slate-900">{order.orderId}</p>
                        <p className="text-sm text-slate-600">{new Date(order.createdAt).toLocaleString()}</p>
                      </div>
                      <div className="flex items-center gap-2">
                        <span className={`inline-flex items-center rounded-full border px-2.5 py-1 text-xs font-medium ${orderStatusBadgeClass(order.status)}`}>
                          {order.status}
                        </span>
                        <span className="font-semibold text-slate-900 min-w-[100px] text-right">{formatAmount(order.totalAmount)}</span>
                        <Button
                          size="sm"
                          variant="outline"
                          onClick={() => navigate(`/orders/${order.orderId}`)}
                        >
                          Details <ArrowRight className="ml-1 h-3.5 w-3.5" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
