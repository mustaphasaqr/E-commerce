export interface SalesSummaryDTO {
  totalOrders: number
  totalRevenue: number
  averageOrderValue: number
  completedOrders: number
  cancelledOrders: number
  pendingOrders: number
  completionRate: number
}

export interface DailySalesDTO {
  date: string
  orderCount: number
  revenue: number
  averageOrderValue: number
}

export interface ProductPerformanceDTO {
  productId: string
  productName: string
  unitsSold: number
  totalRevenue: number
  orderCount: number
}

export interface TopCustomerDTO {
  customerId: string
  customerName: string
  customerEmail: string
  totalOrders: number
  totalSpent: number
  averageOrderValue: number
}

export interface RefundStatsDTO {
  totalCompletedOrders: number
  refundRequestedCount: number
  refundApprovedCount: number
  refundCompletedCount: number
  refundRejectedCount: number
  totalRefundAmount: number
  refundRate: number
  averageRefundAmount: number
  isRefundRateHigh: boolean
}

export interface CustomerRetentionDTO {
  totalCustomers: number
  returningCustomers: number
  newCustomers: number
  retentionRate: number
  churnRate: number
}

export interface CategoryRevenueDTO {
  category: string
  productCount: number
  unitsSold: number
  totalRevenue: number
  averageProductRevenue: number
}

export interface PaymentMethodStatsDTO {
  paymentMethod: string
  transactionCount: number
  totalAmount: number
  successfulCount: number
  failedCount: number
  successRate: number
  failureRate: number
}

export interface ShippingPerformanceDTO {
  carrier: string
  totalShipments: number
  deliveredCount: number
  averageTimeToShipHours: number
  averageDeliveryTimeHours: number
  deliverySuccessRate: number
  isPerformanceGood: boolean
}

export interface CartAbandonmentDTO {
  totalCarts: number
  activeCarts: number
  convertedCarts: number
  abandonedCarts: number
  totalAbandonedValue: number
  averageAbandonedValue: number
  abandonmentRate: number
  conversionRate: number
  isAbandonmentRateHigh: boolean
  potentialRecovery: number
}

export interface LowStockProductDTO {
  productId: string
  productName: string
  currentStock: number
  stockThreshold: number
  totalSold: number
  isCritical: boolean
}

export interface InventoryTurnoverDTO {
  productId: string
  productName: string
  unitsSold: number
  averageStock: number
  turnoverRate: number
  daysToSellOut: number
}

export interface HourlySalesDTO {
  hour: number
  orderCount: number
  revenue: number
}

export interface RevenueForecastDailyDTO {
  date: string
  predictedRevenue: number
  lowerBound: number
  upperBound: number
}

export interface RevenueForecastDTO {
  forecastStartDate: string
  forecastEndDate: string
  forecastDays: number
  predictedRevenue: number
  lowerBound: number
  upperBound: number
  averageDailyRevenue: number
  dailyGrowthRate: number
  trend: string
  confidence: number
  historicalDays: number
  dailyForecasts: RevenueForecastDailyDTO[]
}

export interface ProfitMarginDTO {
  productId: string
  productName: string
  unitsSold: number
  revenue: number
  cost: number
  profit: number
  profitMarginPercent: number
  isProfitable: boolean
}

export interface GeographicSalesDTO {
  city: string
  state: string
  country: string
  orderCount: number
  totalRevenue: number
  averageOrderValue: number
  locationKey: string
}

export interface MarketingAttributionDTO {
  source: string
  campaign: string
  orderCount: number
  totalRevenue: number
  averageOrderValue: number
  customerCount: number
  conversionRate: number
  channelKey: string
}

export interface UserListResponse {
  id: string
  username: string
  email: string
  role: 'CUSTOMER' | 'EMPLOYEE' | 'OWNER'
  status: string
}

export interface PaginatedUsersResponse {
  users: UserListResponse[]
  currentPage: number
  pageSize: number
  totalPages: number
  totalElements: number
}
