import axios from '@/shared/api/axios'
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
  PaginatedUsersResponse,
  PaymentMethodStatsDTO,
  ProfitMarginDTO,
  ProductPerformanceDTO,
  RefundStatsDTO,
  RevenueForecastDTO,
  SalesSummaryDTO,
  ShippingPerformanceDTO,
  TopCustomerDTO,
} from '../types'

const ADMIN_USERS_BASE = '/admin/users'
const OWNER_ANALYTICS_BASE = '/owner/analytics'

export const adminService = {
  getSalesSummary(startDate: string, endDate: string) {
    return axios.get<SalesSummaryDTO>(`${OWNER_ANALYTICS_BASE}/sales/summary`, {
      params: { startDate, endDate },
    })
  },

  getDailySales(startDate: string, endDate: string) {
    return axios.get<DailySalesDTO[]>(`${OWNER_ANALYTICS_BASE}/sales/daily`, {
      params: { startDate, endDate },
    })
  },

  getTopRevenueProducts(startDate: string, endDate: string, limit = 5) {
    return axios.get<ProductPerformanceDTO[]>(`${OWNER_ANALYTICS_BASE}/products/top-revenue`, {
      params: { startDate, endDate, limit },
    })
  },

  getBestSellingProducts(startDate: string, endDate: string, limit = 10) {
    return axios.get<ProductPerformanceDTO[]>(`${OWNER_ANALYTICS_BASE}/products/best-selling`, {
      params: { startDate, endDate, limit },
    })
  },

  getWorstSellingProducts(startDate: string, endDate: string, limit = 10) {
    return axios.get<ProductPerformanceDTO[]>(`${OWNER_ANALYTICS_BASE}/products/worst-selling`, {
      params: { startDate, endDate, limit },
    })
  },

  getTopCustomers(startDate: string, endDate: string, limit = 5) {
    return axios.get<TopCustomerDTO[]>(`${OWNER_ANALYTICS_BASE}/customers/top-buyers`, {
      params: { startDate, endDate, limit },
    })
  },

  getRefundStats(startDate: string, endDate: string) {
    return axios.get<RefundStatsDTO>(`${OWNER_ANALYTICS_BASE}/refunds/stats`, {
      params: { startDate, endDate },
    })
  },

  getCustomerRetention(startDate: string, endDate: string) {
    return axios.get<CustomerRetentionDTO>(`${OWNER_ANALYTICS_BASE}/customers/retention`, {
      params: { startDate, endDate },
    })
  },

  getRevenueByCategory(startDate: string, endDate: string) {
    return axios.get<CategoryRevenueDTO[]>(`${OWNER_ANALYTICS_BASE}/revenue/by-category`, {
      params: { startDate, endDate },
    })
  },

  getPaymentMethodStats(startDate: string, endDate: string) {
    return axios.get<PaymentMethodStatsDTO[]>(`${OWNER_ANALYTICS_BASE}/payments/method-stats`, {
      params: { startDate, endDate },
    })
  },

  getShippingPerformance(startDate: string, endDate: string) {
    return axios.get<ShippingPerformanceDTO[]>(`${OWNER_ANALYTICS_BASE}/shipping/performance`, {
      params: { startDate, endDate },
    })
  },

  getCartAbandonmentStats(startDate: string, endDate: string) {
    return axios.get<CartAbandonmentDTO>(`${OWNER_ANALYTICS_BASE}/carts/abandonment`, {
      params: { startDate, endDate },
    })
  },

  getPeakSalesDay(startDate: string, endDate: string) {
    return axios.get<DailySalesDTO>(`${OWNER_ANALYTICS_BASE}/sales/peak-day`, {
      params: { startDate, endDate },
    })
  },

  getSlowestSalesDay(startDate: string, endDate: string) {
    return axios.get<DailySalesDTO>(`${OWNER_ANALYTICS_BASE}/sales/slowest-day`, {
      params: { startDate, endDate },
    })
  },

  getLowStockProducts(threshold = 10) {
    return axios.get<LowStockProductDTO[]>(`${OWNER_ANALYTICS_BASE}/inventory/low-stock`, {
      params: { threshold },
    })
  },

  getOutOfStockProducts() {
    return axios.get<LowStockProductDTO[]>(`${OWNER_ANALYTICS_BASE}/inventory/out-of-stock`)
  },

  getInventoryTurnover(startDate: string, endDate: string) {
    return axios.get<InventoryTurnoverDTO[]>(`${OWNER_ANALYTICS_BASE}/inventory/turnover`, {
      params: { startDate, endDate },
    })
  },

  getDeadStockProducts() {
    return axios.get<LowStockProductDTO[]>(`${OWNER_ANALYTICS_BASE}/inventory/dead-stock`)
  },

  getSalesByHour(startDate: string, endDate: string) {
    return axios.get<HourlySalesDTO[]>(`${OWNER_ANALYTICS_BASE}/sales/by-hour`, {
      params: { startDate, endDate },
    })
  },

  getSalesByDayOfWeek(startDate: string, endDate: string) {
    return axios.get<DailySalesDTO[]>(`${OWNER_ANALYTICS_BASE}/sales/by-day-of-week`, {
      params: { startDate, endDate },
    })
  },

  getRevenueForecast(historicalStartDate: string, historicalEndDate: string, forecastDays = 30) {
    return axios.get<RevenueForecastDTO>(`${OWNER_ANALYTICS_BASE}/revenue/forecast`, {
      params: { historicalStartDate, historicalEndDate, forecastDays },
    })
  },

  getProfitMargins(startDate: string, endDate: string, limit = 50, sortByProfit = true) {
    return axios.get<ProfitMarginDTO[]>(`${OWNER_ANALYTICS_BASE}/profit/margins`, {
      params: { startDate, endDate, limit, sortByProfit },
    })
  },

  getGeographicSales(startDate: string, endDate: string, groupByCity = false) {
    return axios.get<GeographicSalesDTO[]>(`${OWNER_ANALYTICS_BASE}/geographic/sales`, {
      params: { startDate, endDate, groupByCity },
    })
  },

  getMarketingAttribution(startDate: string, endDate: string) {
    return axios.get<MarketingAttributionDTO[]>(`${OWNER_ANALYTICS_BASE}/marketing/attribution`, {
      params: { startDate, endDate },
    })
  },

  getUsers(page: number, size: number) {
    return axios.get<PaginatedUsersResponse>(ADMIN_USERS_BASE, {
      params: { page, size },
    })
  },

  searchUsers(
    page: number,
    size: number,
    query: string,
    status?: string,
    role?: string
  ) {
    return axios.get<PaginatedUsersResponse>(`${ADMIN_USERS_BASE}/search`, {
      params: {
        page,
        size,
        email: query,
        username: query,
        status,
        role,
      },
    })
  },

  blockUser(id: string, reason: string) {
    return axios.post(`${ADMIN_USERS_BASE}/${id}/block`, { userId: id, reason })
  },

  unblockUser(id: string, reason: string) {
    return axios.post(`${ADMIN_USERS_BASE}/${id}/unblock`, { reason })
  },

  activateUser(id: string, activationNote: string) {
    return axios.post(`${ADMIN_USERS_BASE}/${id}/activate`, { activationNote })
  },

  deactivateUser(id: string, reason: string) {
    return axios.post(`${ADMIN_USERS_BASE}/${id}/deactivate`, { reason })
  },

  changeUserRole(id: string, newRole: string) {
    return axios.post(`${ADMIN_USERS_BASE}/${id}/role`, { newRole })
  },

  deleteUser(id: string, reason: string) {
    return axios.delete(`${ADMIN_USERS_BASE}/${id}`, {
      data: { reason },
    })
  },
}
