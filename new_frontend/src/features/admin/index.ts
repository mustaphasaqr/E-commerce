export { default as DashboardPage } from './pages/DashboardPage';
export { default as ProductsPage } from './pages/ProductsPage';
export { default as OrdersPage } from './pages/OrdersPage';
export { default as UsersPage } from './pages/UsersPage';
export { default as AnalyticsPage } from './pages/AnalyticsPage.tsx';
export { default as SettingsPage } from './pages/SettingsPage';
export { default as AdminDashboard } from './components/AdminDashboard';
export { default as AdminMenu } from './components/AdminMenu';

export { adminService } from './api/adminService';
export type {
	CartAbandonmentDTO,
	CategoryRevenueDTO,
	CustomerRetentionDTO,
	SalesSummaryDTO,
	DailySalesDTO,
	GeographicSalesDTO,
	HourlySalesDTO,
	InventoryTurnoverDTO,
	LowStockProductDTO,
	MarketingAttributionDTO,
	PaymentMethodStatsDTO,
	ProfitMarginDTO,
	ProductPerformanceDTO,
	TopCustomerDTO,
	RefundStatsDTO,
	RevenueForecastDTO,
	ShippingPerformanceDTO,
	UserListResponse,
	PaginatedUsersResponse,
} from './types';
