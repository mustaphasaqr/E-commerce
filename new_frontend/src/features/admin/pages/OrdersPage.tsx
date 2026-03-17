import React, { useEffect, useMemo, useState } from 'react';
import type { AxiosError } from 'axios';
import { AlertTriangle, ArrowDownRight, ArrowUpRight, CalendarDays, Receipt, RotateCcw, TrendingUp } from 'lucide-react';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui';
import { adminService } from '../api/adminService';
import type { DailySalesDTO, RefundStatsDTO, SalesSummaryDTO } from '../types';

type TimeRange = '7d' | '30d' | '90d';

const formatMoney = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'N/A';
  }
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
};

const formatDate = (date: Date) => date.toISOString().split('T')[0];

const getApiErrorMessage = (error: unknown, fallback: string) => {
  const axiosError = error as AxiosError<{ message?: string; error?: string | { message?: string } }>;
  const payload = axiosError?.response?.data;
  return payload?.message ||
    (typeof payload?.error === 'string' ? payload.error : payload?.error?.message) ||
    axiosError?.message ||
    fallback;
};

const getDeltaPercent = (current?: number, previous?: number) => {
  if (typeof current !== 'number' || typeof previous !== 'number' || previous === 0) {
    return null;
  }
  return ((current - previous) / previous) * 100;
};

const formatDelta = (delta: number | null) => {
  if (delta === null || Number.isNaN(delta)) {
    return 'N/A';
  }
  const sign = delta >= 0 ? '+' : '';
  return `${sign}${delta.toFixed(1)}%`;
};

const getDeltaColor = (delta: number | null, higherIsBetter = true) => {
  if (delta === null || Number.isNaN(delta) || delta === 0) {
    return 'text-gray-500';
  }
  const better = higherIsBetter ? delta > 0 : delta < 0;
  return better ? 'text-emerald-600' : 'text-rose-600';
};

const DeltaBadge: React.FC<{ delta: number | null; higherIsBetter?: boolean }> = ({ delta, higherIsBetter = true }) => {
  if (delta === null || Number.isNaN(delta)) {
    return <span className="text-xs text-gray-500">vs previous period: N/A</span>;
  }

  const cls = getDeltaColor(delta, higherIsBetter);
  const up = delta >= 0;

  return (
    <span className={`inline-flex items-center gap-1 text-xs font-semibold ${cls}`}>
      {up ? <ArrowUpRight className="h-3.5 w-3.5" /> : <ArrowDownRight className="h-3.5 w-3.5" />}
      {formatDelta(delta)} vs previous period
    </span>
  );
};

const runSettledInBatches = async <T,>(
  tasks: Array<() => Promise<T>>,
  batchSize = 2
): Promise<Array<PromiseSettledResult<T>>> => {
  const results: Array<PromiseSettledResult<T>> = [];

  for (let i = 0; i < tasks.length; i += batchSize) {
    const chunk = tasks.slice(i, i + batchSize).map((task) => task());
    const settled = await Promise.allSettled(chunk);
    results.push(...settled);
  }

  return results;
};

const getSettledData = <T,>(
  result: PromiseSettledResult<{ data: T }> | undefined,
  fallback: T
): T => {
  if (result?.status === 'fulfilled') {
    return (result.value?.data as T) ?? fallback;
  }
  return fallback;
};

const getRejectedStatus = (result: PromiseSettledResult<unknown>): number | null => {
  if (result.status !== 'rejected') {
    return null;
  }
  const reason = result.reason as { response?: { status?: number } } | undefined;
  return typeof reason?.response?.status === 'number' ? reason.response.status : null;
};

const extractRetrySeconds = (result: PromiseSettledResult<unknown>): number | null => {
  if (result.status !== 'rejected') {
    return null;
  }

  const reason = result.reason as
    | { response?: { data?: { message?: string; error?: string | { message?: string } } } }
    | undefined;

  const payload = reason?.response?.data;
  const message =
    payload?.message ||
    (typeof payload?.error === 'string' ? payload.error : payload?.error?.message) ||
    '';

  const match = message.match(/(\d+)\s*seconds?/i);
  if (!match) {
    return null;
  }

  const parsed = Number(match[1]);
  return Number.isFinite(parsed) ? parsed : null;
};

const OrdersPage: React.FC = () => {
  const [summary, setSummary] = useState<SalesSummaryDTO | null>(null);
  const [previousSummary, setPreviousSummary] = useState<SalesSummaryDTO | null>(null);
  const [dailySales, setDailySales] = useState<DailySalesDTO[]>([]);
  const [refundStats, setRefundStats] = useState<RefundStatsDTO | null>(null);
  const [previousRefundStats, setPreviousRefundStats] = useState<RefundStatsDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [range, setRange] = useState<TimeRange>('30d');

  useEffect(() => {
    setLoading(true);
    setError(null);

    const days = range === '7d' ? 7 : range === '90d' ? 90 : 30;

    const end = new Date();
    const start = new Date();
    start.setDate(end.getDate() - (days - 1));

    const previousEnd = new Date(start);
    previousEnd.setDate(start.getDate() - 1);
    const previousStart = new Date(previousEnd);
    previousStart.setDate(previousEnd.getDate() - (days - 1));

    const startDate = formatDate(start);
    const endDate = formatDate(end);
    const previousStartDate = formatDate(previousStart);
    const previousEndDate = formatDate(previousEnd);

    runSettledInBatches(
      [
        () => adminService.getSalesSummary(startDate, endDate),
        () => adminService.getSalesSummary(previousStartDate, previousEndDate),
        () => adminService.getDailySales(startDate, endDate),
        () => adminService.getRefundStats(startDate, endDate),
        () => adminService.getRefundStats(previousStartDate, previousEndDate),
      ],
      2
    )
      .then((results) => {
        const [summaryRes, previousSummaryRes, dailyRes, refundRes, previousRefundRes] =
          results as Array<PromiseSettledResult<{ data: any }>>;

        const failedCalls = results.filter((item) => item.status === 'rejected').length;
        if (failedCalls === results.length) {
          const statuses = results.map(getRejectedStatus).filter((s): s is number => s !== null);
          const allUnauthorized = statuses.length > 0 && statuses.every((status) => status === 401 || status === 403);
          const allRateLimited = statuses.length > 0 && statuses.every((status) => status === 429);
          const retrySeconds = results.map(extractRetrySeconds).find((v): v is number => v !== null);

          if (allUnauthorized) {
            setError('Session expired or unauthorized. Please log in again with your owner account.');
          } else if (allRateLimited) {
            setError(`Rate limited by backend. Please wait ${retrySeconds ?? 60} seconds and retry.`);
          } else {
            setError('Failed to load orders summary');
          }

          setSummary(null);
          setPreviousSummary(null);
          setDailySales([]);
          setRefundStats(null);
          setPreviousRefundStats(null);
          return;
        }

        setSummary(getSettledData<SalesSummaryDTO | null>(summaryRes, null));
        setPreviousSummary(getSettledData<SalesSummaryDTO | null>(previousSummaryRes, null));
        setDailySales(getSettledData<DailySalesDTO[]>(dailyRes, []));
        setRefundStats(getSettledData<RefundStatsDTO | null>(refundRes, null));
        setPreviousRefundStats(getSettledData<RefundStatsDTO | null>(previousRefundRes, null));
      })
      .catch((e) => setError(getApiErrorMessage(e, 'Failed to load orders summary')))
      .finally(() => setLoading(false));
  }, [range]);

  const sortedRecentDays = useMemo(
    () => [...dailySales].sort((a, b) => b.date.localeCompare(a.date)).slice(0, 10),
    [dailySales]
  );

  const trendDays = useMemo(
    () => [...dailySales].sort((a, b) => a.date.localeCompare(b.date)).slice(-Math.min(30, dailySales.length)),
    [dailySales]
  );

  const maxRevenue = Math.max(...trendDays.map((item) => item.revenue || 0), 1);

  const totalOrdersDelta = getDeltaPercent(summary?.totalOrders, previousSummary?.totalOrders);
  const completedOrdersDelta = getDeltaPercent(summary?.completedOrders, previousSummary?.completedOrders);
  const pendingOrdersDelta = getDeltaPercent(summary?.pendingOrders, previousSummary?.pendingOrders);
  const refundRateDelta = getDeltaPercent(refundStats?.refundRate, previousRefundStats?.refundRate);

  if (loading) {
    return (
      <div className="mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-10 text-sm text-gray-600">
        Loading orders summary...
      </div>
    );
  }

  if (error) {
    return (
      <div className="mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-10 text-sm text-red-600">
        Error: {error}
      </div>
    );
  }

  return (
    <div className="relative">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-44 bg-gradient-to-b from-slate-100 to-transparent" />

      <div className="relative mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-6">
        <div className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <h2 className="text-3xl font-extrabold tracking-tight mb-2 flex items-center gap-2">
            <Receipt className="h-7 w-7 text-blue-600" />
            Orders Command Snapshot
          </h2>
          <p className="text-gray-600">Temporary operational view powered by analytics endpoints until full order-control API is added.</p>

          <div className="mt-4 flex flex-wrap items-center gap-2">
            <span className="text-sm text-gray-500 mr-1">Period:</span>
            {(['7d', '30d', '90d'] as TimeRange[]).map((item) => (
              <Button
                key={item}
                size="sm"
                variant={range === item ? 'default' : 'outline'}
                onClick={() => setRange(item)}
              >
                Last {item}
              </Button>
            ))}
          </div>
        </div>

        <section className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm space-y-4">
          <h3 className="text-lg font-semibold text-slate-900">Order KPIs</h3>

          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="pb-2">
                <CardDescription className="flex items-center gap-2"><Receipt className="h-4 w-4" /> Total Orders</CardDescription>
                <CardTitle>{summary?.totalOrders ?? 'N/A'}</CardTitle>
                <DeltaBadge delta={totalOrdersDelta} higherIsBetter />
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription className="flex items-center gap-2"><TrendingUp className="h-4 w-4" /> Completed Orders</CardDescription>
                <CardTitle>{summary?.completedOrders ?? 'N/A'}</CardTitle>
                <DeltaBadge delta={completedOrdersDelta} higherIsBetter />
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription className="flex items-center gap-2"><CalendarDays className="h-4 w-4" /> Pending Orders</CardDescription>
                <CardTitle>{summary?.pendingOrders ?? 'N/A'}</CardTitle>
                <DeltaBadge delta={pendingOrdersDelta} higherIsBetter={false} />
              </CardHeader>
            </Card>
            <Card>
              <CardHeader className="pb-2">
                <CardDescription className="flex items-center gap-2"><RotateCcw className="h-4 w-4" /> Refund Rate</CardDescription>
                <CardTitle>{typeof refundStats?.refundRate === 'number' ? `${refundStats.refundRate.toFixed(2)}%` : 'N/A'}</CardTitle>
                <DeltaBadge delta={refundRateDelta} higherIsBetter={false} />
              </CardHeader>
            </Card>
          </div>
        </section>

        <section className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <Card>
            <CardHeader>
              <CardTitle>Order Trend</CardTitle>
              <CardDescription>Compact daily revenue bars for the selected range.</CardDescription>
            </CardHeader>
            <CardContent>
              {trendDays.length > 0 ? (
                <div className="h-36 flex items-end gap-1">
                  {trendDays.map((point) => {
                    const h = Math.max(6, Math.round((point.revenue / maxRevenue) * 120));
                    return (
                      <div
                        key={point.date}
                        title={`${point.date}: ${formatMoney(point.revenue)} (${point.orderCount} orders)`}
                        className="flex-1 rounded-t bg-blue-500/70 hover:bg-blue-600 transition-colors"
                        style={{ height: `${h}px` }}
                      />
                    );
                  })}
                </div>
              ) : (
                <p className="text-sm text-gray-500">No trend data available for selected period.</p>
              )}
            </CardContent>
          </Card>
        </section>

        <section className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <Card>
            <CardHeader>
              <CardTitle>Revenue and Order Health</CardTitle>
              <CardDescription>High-level commercial metrics for the last 30 days.</CardDescription>
            </CardHeader>
            <CardContent className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="rounded-lg border border-gray-200 p-4">
                <p className="text-sm text-gray-500">Revenue</p>
                <p className="text-xl font-semibold">{formatMoney(summary?.totalRevenue)}</p>
              </div>
              <div className="rounded-lg border border-gray-200 p-4">
                <p className="text-sm text-gray-500">Average Order Value</p>
                <p className="text-xl font-semibold">{formatMoney(summary?.averageOrderValue)}</p>
              </div>
              <div className="rounded-lg border border-gray-200 p-4">
                <p className="text-sm text-gray-500">Completion Rate</p>
                <p className="text-xl font-semibold">{typeof summary?.completionRate === 'number' ? `${summary.completionRate.toFixed(2)}%` : 'N/A'}</p>
              </div>
              {refundStats?.isRefundRateHigh && (
                <div className="md:col-span-3 rounded-lg border border-amber-300 bg-amber-50 p-3 text-amber-800 text-sm flex items-start gap-2">
                  <AlertTriangle className="h-4 w-4 mt-0.5" />
                  Refund rate is flagged as high. Consider reviewing fulfillment quality and cancellation reasons.
                </div>
              )}
            </CardContent>
          </Card>
        </section>

        <section className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <Card>
            <CardHeader>
              <CardTitle>Daily Order Timeline</CardTitle>
              <CardDescription>Recent 10 active days from analytics data.</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-gray-200 text-sm">
                  <thead className="bg-gray-50">
                    <tr>
                      <th className="px-4 py-3 text-left font-medium text-gray-600">Date</th>
                      <th className="px-4 py-3 text-left font-medium text-gray-600">Orders</th>
                      <th className="px-4 py-3 text-left font-medium text-gray-600">Revenue</th>
                      <th className="px-4 py-3 text-left font-medium text-gray-600">Avg Order Value</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-100 bg-white">
                    {sortedRecentDays.length > 0 ? (
                      sortedRecentDays.map((day) => (
                        <tr key={day.date} className="hover:bg-gray-50">
                          <td className="px-4 py-3 text-gray-700">{day.date}</td>
                          <td className="px-4 py-3 text-gray-700">{day.orderCount}</td>
                          <td className="px-4 py-3 text-gray-700">{formatMoney(day.revenue)}</td>
                          <td className="px-4 py-3 text-gray-700">{formatMoney(day.averageOrderValue)}</td>
                        </tr>
                      ))
                    ) : (
                      <tr>
                        <td className="px-4 py-4 text-gray-500" colSpan={4}>No daily order data available.</td>
                      </tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>
        </section>
      </div>
    </div>
  );
};

export default OrdersPage;
