/**
 * Analytics Types
 * Models for analytics domain (/api/v1/analytics/*)
 * Business analytics for admin dashboard - NOT application performance metrics
 */

export interface DailySalesMetrics {
  date: string;
  totalSales: number;
  totalOrders: number;
  totalItems: number;
  averageOrderValue: number;
  returnsCount: number;
  refundsAmount: number;
  discountsApplied: number;
  taxCollected: number;
}

export interface SalesChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    borderColor: string;
    backgroundColor?: string;
    tension?: number;
    fill?: boolean;
  }[];
}

export interface TopSellingProduct {
  productId: string;
  name: string;
  sku: string;
  unitsSold: number;
  revenue: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
  trendPercent: number;
}

export interface CategoryAnalytics {
  categoryId: string;
  name: string;
  totalSales: number;
  percentageOfTotal: number;
  productsCount: number;
  ordersCount: number;
  trend: number;
}

export interface RevenueTrendData {
  period: string;
  revenue: number;
  previousPeriod?: number;
  percentageChange?: number;
  trend: 'UP' | 'DOWN' | 'STABLE';
}

export interface CustomerAnalytics {
  totalCustomers: number;
  newCustomersThisMonth: number;
  returningCustomers: number;
  churningCustomers: number;
  averageCustomerLifetimeValue: number;
  avarageCustomerRetention: number;
}

export interface OrderAnalytics {
  totalOrders: number;
  completedOrders: number;
  pendingOrders: number;
  cancelledOrders: number;
  returnedOrders: number;
  averageOrderValue: number;
  averageDeliveryTime: number; // in days
}

export interface PaymentAnalytics {
  successful: number;
  failed: number;
  refunded: number;
  totalTransactionAmount: number;
  averageTransactionAmount: number;
  mostUsedPaymentMethod: string;
  conversionRate: number;
  failureRate: number;
}

export interface InventoryAnalytics {
  totalSkus: number;
  inStock: number;
  lowStock: number;
  outOfStock: number;
  overstock: number;
  averageTurnoverRate: number;
  fastMovingProducts: number;
  slowMovingProducts: number;
}

export interface DiscountAnalytics {
  totalDiscountsApplied: number;
  totalDiscountAmount: number;
  averageDiscountPercentage: number;
  topPromoCode: string;
  promoCodeUsageRate: number;
  discountedOrdersPercentage: number;
}

export interface PeakHourAnalytics {
  hour: number;
  orderCount: number;
  revenue: number;
  conversionRate: number;
  users: number;
}

export interface GeographicalAnalytics {
  country: string;
  state?: string;
  totalOrders: number;
  totalRevenue: number;
  percentageOfTotal: number;
  trend: number;
}

export interface AnalyticsDateRange {
  startDate: string;
  endDate: string;
  period: 'DAY' | 'WEEK' | 'MONTH' | 'QUARTER' | 'YEAR' | 'CUSTOM';
}

export interface AnalyticsQueryRequest {
  metric: AnalyticsMetricType;
  dateRange: AnalyticsDateRange;
  groupBy?: 'DAY' | 'WEEK' | 'MONTH' | 'CATEGORY' | 'CUSTOMER' | 'PAYMENT_METHOD';
  filters?: AnalyticsFilter[];
  limit?: number;
  sort?: 'ASC' | 'DESC';
}

export type AnalyticsMetricType =
  | 'DAILY_SALES'
  | 'TOP_PRODUCTS'
  | 'CATEGORY_SALES'
  | 'REVENUE_TREND'
  | 'CUSTOMER_ANALYTICS'
  | 'ORDER_ANALYTICS'
  | 'PAYMENT_ANALYTICS'
  | 'INVENTORY_ANALYTICS'
  | 'DISCOUNT_ANALYTICS'
  | 'PEAK_HOURS'
  | 'GEOGRAPHICAL';

export interface AnalyticsFilter {
  field: string;
  operator: 'equals' | 'contains' | 'gt' | 'gte' | 'lt' | 'lte' | 'in';
  value: string | number | string[];
}

export interface DashboardMetrics {
  totalRevenue: number;
  totalOrders: number;
  totalCustomers: number;
  conversionRate: number;
  averageOrderValue: number;
  customerAcquisitionCost: number;
  customerLifetimeValue: number;
  inventoryValue: number;
  topProduct: TopSellingProduct;
  topCategory: CategoryAnalytics;
}

export interface AnalyticsExportRequest {
  metric: AnalyticsMetricType;
  dateRange: AnalyticsDateRange;
  format: 'CSV' | 'PDF' | 'EXCEL';
  filters?: AnalyticsFilter[];
}
