package com.mustapha.ecommerce.analytics.application.port.out;

import com.mustapha.ecommerce.analytics.domain.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Analytics Query Port - Output Port (Application → Infrastructure)
 * 
 * Responsibility: Define contract for analytics data retrieval
 * Pattern: Port (Hexagonal Architecture), Repository abstraction
 * SOLID: DIP (application depends on interface, infrastructure implements)
 * 
 * This is the APPLICATION LAYER interface that defines what the application needs.
 * The INFRASTRUCTURE LAYER implements this interface (adapter pattern).
 * 
 * Why Port vs Repository?
 * - Repository: Domain concept (part of domain language)
 * - Port: Application concept (what application needs from infrastructure)
 * 
 * In this design:
 * - AnalyticsRepository (domain/repository/) = domain interface (business language)
 * - AnalyticsQueryPort (application/port/out/) = application interface (use case needs)
 * - AnalyticsQueryAdapter (infrastructure/adapter/) = infrastructure implementation
 * 
 * The adapter implements BOTH the port AND delegates to the repository.
 * This provides maximum flexibility and clear layer separation.
 */
public interface AnalyticsQueryPort {

    /**
     * Get best-selling products by units sold
     * @param limit maximum number of products to return (1-100)
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products ordered by units sold (descending)
     */
    List<ProductPerformance> getBestSellingProducts(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get worst-selling products by units sold
     * @param limit maximum number of products to return (1-100)
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products ordered by units sold (ascending)
     */
    List<ProductPerformance> getWorstSellingProducts(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get top revenue generating products
     * @param limit maximum number of products to return (1-100)
     * @param startDate start date for analysis period (inclusive)
     * @ param endDate end date for analysis period (inclusive)
     * @return list of products ordered by total revenue (descending)
     */
    List<ProductPerformance> getTopRevenueProducts(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get daily sales statistics
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return list of daily sales data ordered by date (descending)
     */
    List<DailySales> getDailySales(LocalDate startDate, LocalDate endDate);

    /**
     * Get the day with highest sales
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return daily sales data for peak day, or empty if no data
     */
    Optional<DailySales> getPeakSalesDay(LocalDate startDate, LocalDate endDate);

    /**
     * Get the day with lowest sales
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return daily sales data for slowest day, or empty if no data
     */
    Optional<DailySales> getSlowestSalesDay(LocalDate startDate, LocalDate endDate);

    /**
     * Get aggregate sales summary for a period
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return sales summary with totals and completion rate, or empty if no data
     */
    Optional<SalesSummary> getSalesSummary(LocalDate startDate, LocalDate endDate);

    // ==================== Customer Analytics ====================

    /**
     * Get top customers by total spending
     * @param limit maximum number of customers to return (1-100)
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of customers ordered by total spent (descending)
     */
    List<TopCustomer> getTopCustomers(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get customer retention metrics
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return customer retention statistics
     */
    Optional<CustomerRetention> getCustomerRetention(LocalDate startDate, LocalDate endDate);

    // ==================== Inventory Analytics ====================

    /**
     * Get products with low stock
     * @param threshold stock threshold
     * @return list of low stock products
     */
    List<LowStockProduct> getLowStockProducts(int threshold);

    /**
     * Get products with zero inventory
     * @return list of out-of-stock products
     */
    List<LowStockProduct> getOutOfStockProducts();

    /**
     * Get inventory turnover statistics
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products with turnover rates
     */
    List<InventoryTurnover> getInventoryTurnover(LocalDate startDate, LocalDate endDate);

    /**
     * Get products that have never been sold
     * @return list of dead stock products
     */
    List<LowStockProduct> getDeadStockProducts();

    // ==================== Payment Analytics ====================

    /**
     * Get payment method statistics
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of payment method stats
     */
    List<PaymentMethodStats> getPaymentMethodStats(LocalDate startDate, LocalDate endDate);

    // ==================== Time Analytics ====================

    /**
     * Get sales by hour of day
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of hourly sales (0-23)
     */
    List<HourlySales> getSalesByHour(LocalDate startDate, LocalDate endDate);

    /**
     * Get sales by day of week
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of daily sales by day of week
     */
    List<DailySales> getSalesByDayOfWeek(LocalDate startDate, LocalDate endDate);

    // ==================== Revenue Analytics ====================

    /**
     * Get revenue breakdown by category
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of category revenues
     */
    List<CategoryRevenue> getRevenueByCategory(LocalDate startDate, LocalDate endDate);

    /**
     * Get revenue forecast based on historical trends
     * @param historicalStartDate start of historical analysis period
     * @param historicalEndDate end of historical analysis period
     * @param forecastDays number of days to forecast
     * @return revenue forecast with confidence intervals
     */
    RevenueForecast getRevenueForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays);

    /**
     * Get profit margins for products
     * @param limit maximum number of products
     * @param startDate start of period
     * @param endDate end of period
     * @param sortByProfit sort by profit amount vs margin %
     * @return list of profit margins
     */
    List<ProfitMargin> getProfitMargins(int limit, LocalDate startDate, LocalDate endDate, boolean sortByProfit);

    /**
     * Get refund statistics
     * @param startDate start of period
     * @param endDate end of period
     * @return refund stats
     */
    RefundStats getRefundStats(LocalDate startDate, LocalDate endDate);

    /**
     * Get sales by geographic location
     * @param startDate start of period
     * @param endDate end of period
     * @param groupByCity group by city vs state
     * @return list of geographic sales
     */
    List<GeographicSales> getSalesByLocation(LocalDate startDate, LocalDate endDate, boolean groupByCity);

    /**
     * Get shipping performance by carrier
     * @param startDate start of period
     * @param endDate end of period
     * @return list of shipping performance
     */
    List<ShippingPerformance> getShippingPerformance(LocalDate startDate, LocalDate endDate);

    /**
     * Get marketing attribution data
     * @param startDate start of period
     * @param endDate end of period
     * @return list of marketing attribution
     */
    List<MarketingAttribution> getMarketingAttribution(LocalDate startDate, LocalDate endDate);

    /**
     * Get cart abandonment statistics
     * @param startDate start of period
     * @param endDate end of period
     * @return cart abandonment stats
     */
    CartAbandonment getCartAbandonmentStats(LocalDate startDate, LocalDate endDate);
}
