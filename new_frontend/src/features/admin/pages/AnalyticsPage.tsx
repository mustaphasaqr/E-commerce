import React, { useEffect, useState } from 'react';
import {
  AlertTriangle,
  ArrowDownRight,
  ArrowUpRight,
  BarChart3,
  Boxes,
  CreditCard,
  DollarSign,
  Map,
  Megaphone,
  Package,
  ShoppingCart,
  Target,
  Truck,
  Users,
} from 'lucide-react';
import { Button, Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/shared/components/ui';
import { adminService } from '../api/adminService';
import type {
  CartAbandonmentDTO,
  CategoryRevenueDTO,
  CustomerRetentionDTO,
  DailySalesDTO,
  GeographicSalesDTO,
  HourlySalesDTO,
  InventoryTurnoverDTO,
  LowStockProductDTO,
  MarketingAttributionDTO,
  PaymentMethodStatsDTO,
  ProfitMarginDTO,
  ProductPerformanceDTO,
  RevenueForecastDTO,
  SalesSummaryDTO,
  ShippingPerformanceDTO,
  TopCustomerDTO,
} from '../types';

type InsightsData = {
  salesSummary: SalesSummaryDTO | null;
  previousSalesSummary: SalesSummaryDTO | null;
  dailySales: DailySalesDTO[];
  peakSalesDay: DailySalesDTO | null;
  slowestSalesDay: DailySalesDTO | null;
  salesByHour: HourlySalesDTO[];
  salesByDayOfWeek: DailySalesDTO[];
  bestSellingProducts: ProductPerformanceDTO[];
  worstSellingProducts: ProductPerformanceDTO[];
  topRevenueProducts: ProductPerformanceDTO[];
  topCustomers: TopCustomerDTO[];
  retention: CustomerRetentionDTO | null;
  revenueByCategory: CategoryRevenueDTO[];
  revenueForecast: RevenueForecastDTO | null;
  profitMargins: ProfitMarginDTO[];
  lowStockProducts: LowStockProductDTO[];
  outOfStockProducts: LowStockProductDTO[];
  inventoryTurnover: InventoryTurnoverDTO[];
  deadStockProducts: LowStockProductDTO[];
  paymentMethodStats: PaymentMethodStatsDTO[];
  geographicSales: GeographicSalesDTO[];
  marketingAttribution: MarketingAttributionDTO[];
  shippingPerformance: ShippingPerformanceDTO[];
  cartAbandonment: CartAbandonmentDTO | null;
  previousCartAbandonment: CartAbandonmentDTO | null;
  refundStats: { refundRate?: number } | null;
  failedCalls: number;
};

type TimeRange = '7d' | '30d' | '90d';

const formatMoney = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'N/A';
  }
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
};

const formatDate = (date: Date) => date.toISOString().split('T')[0];

const formatPercentage = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'N/A';
  }
  return `${value.toFixed(2)}%`;
};

const formatNumber = (value?: number) => {
  if (typeof value !== 'number' || Number.isNaN(value)) {
    return 'N/A';
  }
  return new Intl.NumberFormat('en-US').format(value);
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

const DeltaBadge: React.FC<{ delta: number | null; higherIsBetter?: boolean }> = ({
  delta,
  higherIsBetter = true,
}) => {
  if (delta === null || Number.isNaN(delta)) {
    return <span className="text-xs text-gray-500">vs previous period: N/A</span>;
  }

  const color = getDeltaColor(delta, higherIsBetter);
  const up = delta >= 0;

  return (
    <span className={`inline-flex items-center gap-1 text-xs font-semibold ${color}`}>
      {up ? <ArrowUpRight className="h-3.5 w-3.5" /> : <ArrowDownRight className="h-3.5 w-3.5" />}
      {formatDelta(delta)} vs previous period
    </span>
  );
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

type ApiResponse = { data: any };

const runSettledInBatches = async (
  tasks: Array<() => Promise<ApiResponse>>,
  batchSize = 3
): Promise<Array<PromiseSettledResult<ApiResponse>>> => {
  const results: Array<PromiseSettledResult<ApiResponse>> = [];

  for (let i = 0; i < tasks.length; i += batchSize) {
    const chunk = tasks.slice(i, i + batchSize).map((task) => task());
    const settled = await Promise.allSettled(chunk);
    results.push(...settled);
  }

  return results;
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

const AnalyticsPage: React.FC = () => {
  const [data, setData] = useState<InsightsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [range, setRange] = useState<TimeRange>('30d');

  useEffect(() => {
    let cancelled = false;

    const loadInsights = async () => {
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

      const coreResults = await runSettledInBatches(
        [
          () => adminService.getSalesSummary(startDate, endDate),
          () => adminService.getSalesSummary(previousStartDate, previousEndDate),
          () => adminService.getCartAbandonmentStats(startDate, endDate),
          () => adminService.getCartAbandonmentStats(previousStartDate, previousEndDate),
        ],
        2
      );

      if (cancelled) {
        return;
      }

      const [
        summaryRes,
        previousSummaryRes,
        cartRes,
        previousCartRes,
      ] = coreResults as Array<PromiseSettledResult<{ data: any }>>;

      const coreFailedCalls = coreResults.filter((item) => item.status === 'rejected').length;

      if (coreFailedCalls === coreResults.length) {
        const statuses = coreResults.map(getRejectedStatus).filter((s): s is number => s !== null);
        const allUnauthorized = statuses.length > 0 && statuses.every((status) => status === 401 || status === 403);
        const allRateLimited = statuses.length > 0 && statuses.every((status) => status === 429);
        const retrySeconds = coreResults.map(extractRetrySeconds).find((v): v is number => v !== null);

        setError(
          allUnauthorized
            ? 'Session expired or unauthorized. Please log in again with your owner account.'
            : allRateLimited
              ? `Rate limited by backend. Please wait ${retrySeconds ?? 60} seconds and retry.`
              : 'Unable to load analytics data. Please re-authenticate or verify backend availability.'
        );
        setLoading(false);
        return;
      }

      setData({
        salesSummary: getSettledData<SalesSummaryDTO | null>(summaryRes, null),
        previousSalesSummary: getSettledData<SalesSummaryDTO | null>(previousSummaryRes, null),
        dailySales: [],
        peakSalesDay: null,
        slowestSalesDay: null,
        salesByHour: [],
        salesByDayOfWeek: [],
        bestSellingProducts: [],
        worstSellingProducts: [],
        topRevenueProducts: [],
        topCustomers: [],
        retention: null,
        revenueByCategory: [],
        revenueForecast: null,
        profitMargins: [],
        lowStockProducts: [],
        outOfStockProducts: [],
        inventoryTurnover: [],
        deadStockProducts: [],
        paymentMethodStats: [],
        geographicSales: [],
        marketingAttribution: [],
        shippingPerformance: [],
        cartAbandonment: getSettledData<CartAbandonmentDTO | null>(cartRes, null),
        previousCartAbandonment: getSettledData<CartAbandonmentDTO | null>(previousCartRes, null),
        refundStats: null,
        failedCalls: coreFailedCalls,
      });

      setLoading(false);

      const backgroundResults = await runSettledInBatches(
        [
          () => adminService.getTopRevenueProducts(startDate, endDate, 5),
          () => adminService.getTopCustomers(startDate, endDate, 5),
          () => adminService.getCustomerRetention(startDate, endDate),
          () => adminService.getRevenueByCategory(startDate, endDate),
          () => adminService.getPaymentMethodStats(startDate, endDate),
          () => adminService.getShippingPerformance(startDate, endDate),
          () => adminService.getDailySales(startDate, endDate),
          () => adminService.getPeakSalesDay(startDate, endDate),
          () => adminService.getSlowestSalesDay(startDate, endDate),
          () => adminService.getSalesByHour(startDate, endDate),
          () => adminService.getSalesByDayOfWeek(startDate, endDate),
          () => adminService.getBestSellingProducts(startDate, endDate, 5),
          () => adminService.getWorstSellingProducts(startDate, endDate, 5),
          () => adminService.getRevenueForecast(startDate, endDate, 30),
          () => adminService.getProfitMargins(startDate, endDate, 5, true),
          () => adminService.getLowStockProducts(10),
          () => adminService.getOutOfStockProducts(),
          () => adminService.getInventoryTurnover(startDate, endDate),
          () => adminService.getDeadStockProducts(),
          () => adminService.getGeographicSales(startDate, endDate, false),
          () => adminService.getMarketingAttribution(startDate, endDate),
          () => adminService.getRefundStats(startDate, endDate),
        ],
        2
      );

      if (cancelled) {
        return;
      }

      const [
        topRevenueRes,
        topCustomersRes,
        retentionRes,
        categoryRevenueRes,
        paymentStatsRes,
        shippingRes,
        dailySalesRes,
        peakDayRes,
        slowestDayRes,
        byHourRes,
        byDayOfWeekRes,
        bestSellingRes,
        worstSellingRes,
        forecastRes,
        profitMarginsRes,
        lowStockRes,
        outOfStockRes,
        turnoverRes,
        deadStockRes,
        geographicRes,
        marketingRes,
        refundRes,
      ] = backgroundResults as Array<PromiseSettledResult<{ data: any }>>;

      const backgroundFailedCalls = backgroundResults.filter((item) => item.status === 'rejected').length;

      setData((prev) => {
        if (!prev) {
          return prev;
        }

        return {
          ...prev,
          topRevenueProducts: getSettledData<ProductPerformanceDTO[]>(topRevenueRes, []),
          topCustomers: getSettledData<TopCustomerDTO[]>(topCustomersRes, []),
          retention: getSettledData<CustomerRetentionDTO | null>(retentionRes, null),
          revenueByCategory: getSettledData<CategoryRevenueDTO[]>(categoryRevenueRes, []),
          paymentMethodStats: getSettledData<PaymentMethodStatsDTO[]>(paymentStatsRes, []),
          shippingPerformance: getSettledData<ShippingPerformanceDTO[]>(shippingRes, []),
          dailySales: getSettledData<DailySalesDTO[]>(dailySalesRes, []),
          peakSalesDay: getSettledData<DailySalesDTO | null>(peakDayRes, null),
          slowestSalesDay: getSettledData<DailySalesDTO | null>(slowestDayRes, null),
          salesByHour: getSettledData<HourlySalesDTO[]>(byHourRes, []),
          salesByDayOfWeek: getSettledData<DailySalesDTO[]>(byDayOfWeekRes, []),
          bestSellingProducts: getSettledData<ProductPerformanceDTO[]>(bestSellingRes, []),
          worstSellingProducts: getSettledData<ProductPerformanceDTO[]>(worstSellingRes, []),
          revenueForecast: getSettledData<RevenueForecastDTO | null>(forecastRes, null),
          profitMargins: getSettledData<ProfitMarginDTO[]>(profitMarginsRes, []),
          lowStockProducts: getSettledData<LowStockProductDTO[]>(lowStockRes, []),
          outOfStockProducts: getSettledData<LowStockProductDTO[]>(outOfStockRes, []),
          inventoryTurnover: getSettledData<InventoryTurnoverDTO[]>(turnoverRes, []),
          deadStockProducts: getSettledData<LowStockProductDTO[]>(deadStockRes, []),
          geographicSales: getSettledData<GeographicSalesDTO[]>(geographicRes, []),
          marketingAttribution: getSettledData<MarketingAttributionDTO[]>(marketingRes, []),
          refundStats: getSettledData<{ refundRate?: number } | null>(refundRes, null),
          failedCalls: coreFailedCalls + backgroundFailedCalls,
        };
      });
    };

    loadInsights().catch((e) => {
      if (!cancelled) {
        setError(e?.message || 'Failed to load commerce insights');
        setLoading(false);
      }
    });

    return () => {
      cancelled = true;
    };
  }, [range]);

  if (loading) {
    return (
      <div className="mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-10 text-sm text-gray-600">
        Loading commerce insights...
      </div>
    );
  }
  if (error) {
    return (
      <div className="mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-10 text-red-600">
        Error: {error}
      </div>
    );
  }

  const summary = data?.salesSummary;
  const previousSummary = data?.previousSalesSummary;
  const retention = data?.retention;
  const abandonment = data?.cartAbandonment;
  const previousAbandonment = data?.previousCartAbandonment;

  const totalRevenue = formatMoney(summary?.totalRevenue);
  const totalOrders = typeof summary?.totalOrders === 'number' ? summary.totalOrders : 'N/A';
  const averageOrderValue = formatMoney(summary?.averageOrderValue);

  const revenueDelta = getDeltaPercent(summary?.totalRevenue, previousSummary?.totalRevenue);
  const ordersDelta = getDeltaPercent(summary?.totalOrders, previousSummary?.totalOrders);
  const aovDelta = getDeltaPercent(summary?.averageOrderValue, previousSummary?.averageOrderValue);
  const conversionDelta = getDeltaPercent(abandonment?.conversionRate, previousAbandonment?.conversionRate);
  const abandonmentDelta = getDeltaPercent(abandonment?.abandonmentRate, previousAbandonment?.abandonmentRate);

  const categoryPreview = data?.revenueByCategory?.slice(0, 5) ?? [];
  const paymentPreview = data?.paymentMethodStats?.slice(0, 4) ?? [];
  const shippingPreview = data?.shippingPerformance?.slice(0, 4) ?? [];
  const bestSellingPreview = data?.bestSellingProducts?.slice(0, 5) ?? [];
  const worstSellingPreview = data?.worstSellingProducts?.slice(0, 5) ?? [];
  const topCustomersPreview = data?.topCustomers?.slice(0, 5) ?? [];
  const topRevenueProductsPreview = data?.topRevenueProducts?.slice(0, 5) ?? [];

  const dailyTrend = [...(data?.dailySales ?? [])].sort((a, b) => a.date.localeCompare(b.date)).slice(-30);
  const maxRevenue = Math.max(...dailyTrend.map((d) => d.revenue || 0), 1);

  const alerts: string[] = [];
  if (typeof abandonment?.abandonmentRate === 'number' && abandonment.abandonmentRate >= 70) {
    alerts.push(`Cart abandonment is high at ${abandonment.abandonmentRate.toFixed(1)}%.`);
  }
  if (typeof data?.refundStats?.refundRate === 'number' && data.refundStats.refundRate >= 5) {
    alerts.push(`Refund rate reached ${data.refundStats.refundRate.toFixed(1)}%.`);
  }
  if (ordersDelta !== null && ordersDelta < -10) {
    alerts.push(`Orders dropped ${Math.abs(ordersDelta).toFixed(1)}% versus previous period.`);
  }
  if (revenueDelta !== null && revenueDelta < -10) {
    alerts.push(`Revenue dropped ${Math.abs(revenueDelta).toFixed(1)}% versus previous period.`);
  }

  const immediateActions: string[] = [];
  if (typeof abandonment?.abandonmentRate === 'number' && abandonment.abandonmentRate >= 70) {
    immediateActions.push('Optimize checkout flow and launch cart recovery campaign.');
  }
  if ((data?.outOfStockProducts?.length ?? 0) > 0) {
    immediateActions.push(`Replenish out-of-stock products (${data?.outOfStockProducts.length ?? 0} items).`);
  }
  if (ordersDelta !== null && ordersDelta < 0) {
    immediateActions.push('Review acquisition channels and current promotions for this period.');
  }
  if (!immediateActions.length) {
    immediateActions.push('Scale winning products and keep monitoring conversion daily.');
  }

  const analyticsEndpointCalls = 24;

  return (
    <div className="relative">
      <div className="pointer-events-none absolute inset-x-0 top-0 h-52 bg-gradient-to-b from-slate-100 to-transparent" />

      <div className="relative mx-auto max-w-[90rem] px-4 sm:px-6 lg:px-8 py-8 sm:py-10 space-y-8">
        <div className="rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
          <h2 className="text-3xl font-extrabold tracking-tight mb-2 flex items-center gap-2">
            <BarChart3 className="h-7 w-7 text-blue-600" />
            Commerce Insights Hub
          </h2>
          <p className="text-gray-600">Business-first dashboard for fast executive decisions.</p>

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

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Target className="h-5 w-5 text-blue-600" />
          Business Health
        </h3>

        {alerts.length > 0 && (
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base flex items-center gap-2 text-amber-700">
                <AlertTriangle className="h-4 w-4" /> Alerts
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-1 text-sm text-amber-800">
              {alerts.map((item, idx) => (
                <p key={`alert-${idx}`}>- {item}</p>
              ))}
            </CardContent>
          </Card>
        )}

        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-base">Immediate Actions</CardTitle>
          </CardHeader>
          <CardContent className="space-y-1 text-sm text-gray-700">
            {immediateActions.map((item, idx) => (
              <p key={`action-${idx}`}>- {item}</p>
            ))}
          </CardContent>
        </Card>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <DollarSign className="h-5 w-5 text-blue-600" />
          Revenue Metrics
        </h3>

        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
          <Card>
            <CardHeader className="pb-1">
              <CardDescription className="flex items-center gap-2"><DollarSign className="h-4 w-4" /> Revenue</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-4xl font-extrabold leading-tight">{totalRevenue}</p>
              <DeltaBadge delta={revenueDelta} higherIsBetter />
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-1">
              <CardDescription className="flex items-center gap-2"><ShoppingCart className="h-4 w-4" /> Orders</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-4xl font-extrabold leading-tight">{totalOrders}</p>
              <DeltaBadge delta={ordersDelta} higherIsBetter />
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-1">
              <CardDescription className="flex items-center gap-2"><Package className="h-4 w-4" /> Avg Order Value</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-4xl font-extrabold leading-tight">{averageOrderValue}</p>
              <DeltaBadge delta={aovDelta} higherIsBetter />
            </CardContent>
          </Card>

          <Card>
            <CardHeader className="pb-1">
              <CardDescription className="flex items-center gap-2"><Target className="h-4 w-4" /> Conversion Rate</CardDescription>
            </CardHeader>
            <CardContent>
              <p className="text-4xl font-extrabold leading-tight">{formatPercentage(abandonment?.conversionRate)}</p>
              <DeltaBadge delta={conversionDelta} higherIsBetter />
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <BarChart3 className="h-5 w-5 text-blue-600" />
          Revenue Trend
        </h3>

        <Card>
          <CardHeader>
            <CardTitle className="text-base">Revenue trend in selected period</CardTitle>
            <CardDescription>Simple visual pattern to spot momentum quickly.</CardDescription>
          </CardHeader>
          <CardContent>
            {dailyTrend.length ? (
              <div className="h-40 flex items-end gap-1">
                {dailyTrend.map((point) => {
                  const h = Math.max(6, Math.round((point.revenue / maxRevenue) * 140));
                  return (
                    <div
                      key={point.date}
                      title={`${point.date}: ${formatMoney(point.revenue)}`}
                      className="flex-1 rounded-t bg-blue-500/70 hover:bg-blue-600 transition-colors"
                      style={{ height: `${h}px` }}
                    />
                  );
                })}
              </div>
            ) : (
              <p className="text-sm text-gray-500">No daily trend data available.</p>
            )}
          </CardContent>
        </Card>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Boxes className="h-5 w-5 text-blue-600" />
          Product Performance
        </h3>

        <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Top Revenue Products</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-500 border-b">
                      <th className="py-2 pr-3">Product</th>
                      <th className="py-2 pr-3">Units</th>
                      <th className="py-2">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topRevenueProductsPreview.length ? topRevenueProductsPreview.map((item, idx) => (
                      <tr key={`${item.productId || 'rev'}-${idx}`} className="border-b last:border-0">
                        <td className="py-2 pr-3">{item.productName || `Product ${idx + 1}`}</td>
                        <td className="py-2 pr-3">{formatNumber(item.unitsSold)}</td>
                        <td className="py-2 font-semibold">{formatMoney(item.totalRevenue)}</td>
                      </tr>
                    )) : (
                      <tr><td className="py-3 text-gray-500" colSpan={3}>No product analytics available for this period.</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Best vs Worst Selling</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <p className="text-sm font-semibold mb-2">Best-Selling</p>
                {bestSellingPreview.length ? (
                  <ul className="space-y-1 text-sm">
                    {bestSellingPreview.map((item, idx) => (
                      <li key={`${item.productId || 'best'}-${idx}`} className="flex justify-between gap-3">
                        <span>{item.productName}</span>
                        <span className="font-semibold">{formatNumber(item.unitsSold)}</span>
                      </li>
                    ))}
                  </ul>
                ) : <p className="text-sm text-gray-500">No best-selling data.</p>}
              </div>

              <div>
                <p className="text-sm font-semibold mb-2">Worst-Selling</p>
                {worstSellingPreview.length ? (
                  <ul className="space-y-1 text-sm">
                    {worstSellingPreview.map((item, idx) => (
                      <li key={`${item.productId || 'worst'}-${idx}`} className="flex justify-between gap-3">
                        <span>{item.productName}</span>
                        <span className="font-semibold">{formatNumber(item.unitsSold)}</span>
                      </li>
                    ))}
                  </ul>
                ) : <p className="text-sm text-gray-500">No worst-selling data.</p>}
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Users className="h-5 w-5 text-blue-600" />
          Customer Insights
        </h3>

        <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Top Customers</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="overflow-x-auto">
                <table className="min-w-full text-sm">
                  <thead>
                    <tr className="text-left text-gray-500 border-b">
                      <th className="py-2 pr-3">Customer</th>
                      <th className="py-2 pr-3">Orders</th>
                      <th className="py-2">Revenue</th>
                    </tr>
                  </thead>
                  <tbody>
                    {topCustomersPreview.length ? topCustomersPreview.map((item, idx) => (
                      <tr key={`${item.customerId || 'cust'}-${idx}`} className="border-b last:border-0">
                        <td className="py-2 pr-3">{item.customerName || item.customerEmail || `Customer ${idx + 1}`}</td>
                        <td className="py-2 pr-3">{formatNumber(item.totalOrders)}</td>
                        <td className="py-2 font-semibold">{formatMoney(item.totalSpent)}</td>
                      </tr>
                    )) : (
                      <tr><td className="py-3 text-gray-500" colSpan={3}>No customer analytics available for this period.</td></tr>
                    )}
                  </tbody>
                </table>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Retention Health</CardTitle>
            </CardHeader>
            <CardContent className="grid grid-cols-2 gap-4">
              <div className="rounded-lg border p-4">
                <p className="text-sm text-gray-500">Retention</p>
                <p className="text-3xl font-bold text-emerald-600">{formatPercentage(retention?.retentionRate)}</p>
              </div>
              <div className="rounded-lg border p-4">
                <p className="text-sm text-gray-500">Churn</p>
                <p className="text-3xl font-bold text-rose-600">{formatPercentage(retention?.churnRate)}</p>
              </div>
              <div className="rounded-lg border p-4 col-span-2">
                <p className="text-sm text-gray-500">Returning vs Total Customers</p>
                <p className="text-lg font-semibold">{formatNumber(retention?.returningCustomers)} / {formatNumber(retention?.totalCustomers)}</p>
              </div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <ShoppingCart className="h-5 w-5 text-blue-600" />
          Funnel and Problems
        </h3>

        <div className="grid grid-cols-1 xl:grid-cols-2 gap-4">
          <Card>
            <CardHeader>
              <CardTitle className="text-base">Cart Funnel (available tracking)</CardTitle>
              <CardDescription>Built only from existing analytics endpoints.</CardDescription>
            </CardHeader>
            <CardContent className="space-y-3 text-sm">
              <div className="flex justify-between"><span>Tracked Carts</span><span className="font-semibold">{formatNumber(abandonment?.totalCarts)}</span></div>
              <div className="flex justify-between"><span>Converted</span><span className="font-semibold text-emerald-600">{formatNumber(abandonment?.convertedCarts)}</span></div>
              <div className="flex justify-between"><span>Abandoned</span><span className="font-semibold text-amber-600">{formatNumber(abandonment?.abandonedCarts)}</span></div>
              <div className="flex justify-between"><span>Active</span><span className="font-semibold">{formatNumber(abandonment?.activeCarts)}</span></div>
              <div className="pt-2 border-t flex justify-between items-center">
                <span>Abandonment Rate</span>
                <span className={`font-semibold ${getDeltaColor(abandonmentDelta, false)}`}>
                  {formatPercentage(abandonment?.abandonmentRate)}
                </span>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <CardTitle className="text-base">Operational Risk Signals</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              <div className="flex justify-between"><span>Out-of-stock products</span><span className="font-semibold">{formatNumber(data?.outOfStockProducts.length)}</span></div>
              <div className="flex justify-between"><span>Low-stock products</span><span className="font-semibold">{formatNumber(data?.lowStockProducts.length)}</span></div>
              <div className="flex justify-between"><span>Refund rate</span><span className="font-semibold">{formatPercentage(data?.refundStats?.refundRate)}</span></div>
              <div className="flex justify-between"><span>Payment methods tracked</span><span className="font-semibold">{formatNumber(paymentPreview.length)}</span></div>
            </CardContent>
          </Card>
        </div>
      </section>

      <section className="space-y-4 rounded-2xl border border-slate-200/80 bg-white/90 p-5 sm:p-6 shadow-sm">
        <h3 className="text-lg font-semibold flex items-center gap-2">
          <Megaphone className="h-5 w-5 text-blue-600" />
          Additional Signals
        </h3>

        <div className="grid grid-cols-1 xl:grid-cols-3 gap-4">
          <Card>
            <CardHeader><CardTitle className="text-base flex items-center gap-2"><Package className="h-4 w-4" /> Revenue by Category</CardTitle></CardHeader>
            <CardContent>
              {categoryPreview.length ? (
                <ul className="space-y-1 text-sm">
                  {categoryPreview.map((item, index) => (
                    <li key={`${item.category || 'category'}-${index}`} className="flex justify-between">
                      <span>{item.category || `Category ${index + 1}`}</span>
                      <span className="font-semibold">{formatMoney(item.totalRevenue)}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-sm text-gray-500">No category revenue data.</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base flex items-center gap-2"><CreditCard className="h-4 w-4" /> Payment Success</CardTitle></CardHeader>
            <CardContent>
              {paymentPreview.length ? (
                <ul className="space-y-1 text-sm">
                  {paymentPreview.map((item, index) => (
                    <li key={`${item.paymentMethod || 'method'}-${index}`} className="flex justify-between">
                      <span>{item.paymentMethod || `Method ${index + 1}`}</span>
                      <span className="font-semibold">{formatPercentage(item.successRate)}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-sm text-gray-500">No payment method analytics.</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base flex items-center gap-2"><Truck className="h-4 w-4" /> Shipping Performance</CardTitle></CardHeader>
            <CardContent>
              {shippingPreview.length ? (
                <ul className="space-y-1 text-sm">
                  {shippingPreview.map((item, index) => (
                    <li key={`${item.carrier || 'carrier'}-${index}`} className="flex justify-between">
                      <span>{item.carrier || 'Unknown'}</span>
                      <span className="font-semibold">{formatPercentage(item.deliverySuccessRate)}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-sm text-gray-500">No shipping performance analytics.</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base flex items-center gap-2"><Map className="h-4 w-4" /> Geographic Snapshot</CardTitle></CardHeader>
            <CardContent>
              {data?.geographicSales?.length ? (
                <ul className="space-y-1 text-sm">
                  {data.geographicSales.slice(0, 4).map((item, index) => (
                    <li key={`${item.locationKey || 'location'}-${index}`} className="flex justify-between">
                      <span>{item.city || item.state || item.country || 'Unknown'}</span>
                      <span className="font-semibold">{formatMoney(item.totalRevenue)}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-sm text-gray-500">No geographic analytics.</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Inventory Turnover</CardTitle></CardHeader>
            <CardContent>
              {data?.inventoryTurnover?.length ? (
                <ul className="space-y-1 text-sm">
                  {data.inventoryTurnover.slice(0, 4).map((item, index) => (
                    <li key={`${item.productId || 'turnover'}-${index}`} className="flex justify-between">
                      <span>{item.productName}</span>
                      <span className="font-semibold">{typeof item.turnoverRate === 'number' ? item.turnoverRate.toFixed(2) : 'N/A'}</span>
                    </li>
                  ))}
                </ul>
              ) : <p className="text-sm text-gray-500">No turnover data.</p>}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle className="text-base">Endpoint Coverage</CardTitle></CardHeader>
            <CardContent className="text-sm">
              <p>Commerce Insights attempts all {analyticsEndpointCalls} owner analytics endpoints.</p>
              <p className="mt-2">Failed calls this load: <span className="font-semibold">{data?.failedCalls ?? 0}</span></p>
            </CardContent>
          </Card>
        </div>
      </section>
      </div>
    </div>
  );
};

export default AnalyticsPage;
