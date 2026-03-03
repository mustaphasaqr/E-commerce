package com.mustapha.ecommerce.analytics.domain.repository;

import com.mustapha.ecommerce.analytics.domain.model.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Analytics Repository Interface - Domain Layer
 * Responsibility: Define contract for business analytics queries
 * Pattern: Repository (abstraction)
 * SOLID: DIP (interface in domain, implementation in infrastructure)
 * 
 * This repository aggregates data from orders for business intelligence
 */
public interface AnalyticsRepository {

    /**
     * Get best-selling products by units sold
     * @param limit maximum number of products to return
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products ordered by units sold (descending)
     */
    List<ProductPerformance> getBestSellingProducts(int limit, LocalDate startDate, LocalDate endDate);

    /**
     * Get worst-selling products by units sold
     * @param limit maximum number of products to return
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products ordered by units sold (ascending)
     */
    List<ProductPerformance> getWorstSellingProducts(int limit, LocalDate startDate, LocalDate endDate);

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
     * @return daily sales data for peak day
     */
    DailySales getPeakSalesDay(LocalDate startDate, LocalDate endDate);

    /**
     * Get the day with lowest sales
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return daily sales data for slowest day
     */
    DailySales getSlowestSalesDay(LocalDate startDate, LocalDate endDate);

    /**
     * Get overall sales summary for a period
     * @param startDate start date (inclusive)
     * @param endDate end date (inclusive)
     * @return aggregated sales statistics
     */
    SalesSummary getSalesSummary(LocalDate startDate, LocalDate endDate);

    /**
     * Get products by revenue (not units sold)
     * Useful for identifying high-value products
     * @param limit maximum number of products to return
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of products ordered by revenue (descending)
     */
    List<ProductPerformance> getTopRevenueProducts(int limit, LocalDate startDate, LocalDate endDate);

    // ==================== Customer Analytics ====================

    /**
     * Get top customers by total spending
     * @param limit maximum number of customers to return
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
    CustomerRetention getCustomerRetention(LocalDate startDate, LocalDate endDate);

    // ==================== Inventory Analytics ====================

    /**
     * Get products with low stock
     * @param threshold stock threshold (products below this are included)
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
     * @return list of daily sales (Monday=1 to Sunday=7)
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

    // ==================== Revenue Forecasting ====================

    /**
     * Get revenue forecast based on historical data
     * 
     * Uses statistical analysis:
     * - Linear regression for trend
     * - Moving average for smoothing
     * - Variance analysis for confidence
     * 
     * @param historicalStartDate start of historical period (e.g., last 90 days)
     * @param historicalEndDate end of historical period (e.g., today)
     * @param forecastDays number of days to forecast (1-365)
     * @return revenue forecast with confidence intervals
     */
    RevenueForecast getRevenueForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays);

    // ==================== Profit Analytics ====================

    /**
     * Get profit margins for products
     * Requires COGS (Cost of Goods Sold) data to be populated
     * 
     * @param limit maximum number of products to return
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @param sortByProfit if true, sort by profit amount; if false, sort by margin %
     * @return list of products with profit margin data
     */
    List<ProfitMargin> getProfitMargins(int limit, LocalDate startDate, LocalDate endDate, boolean sortByProfit);

    // ==================== Refund Analytics ====================

    /**
     * Get refund statistics for a period
     * Analyzes refund requests, approvals, completions
     * 
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return refund statistics including refund rate
     */
    RefundStats getRefundStats(LocalDate startDate, LocalDate endDate);

    // ==================== Geographic Analytics ====================

    /**
     * Get sales breakdown by geographic location
     * 
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @param groupByCity if true, group by city; if false, group by state
     * @return list of geographic sales data
     */
    List<GeographicSales> getSalesByLocation(LocalDate startDate, LocalDate endDate, boolean groupByCity);

    // ==================== Shipping Analytics ====================

    /**
     * Get shipping performance metrics by carrier
     * 
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of carrier performance metrics
     */
    List<ShippingPerformance> getShippingPerformance(LocalDate startDate, LocalDate endDate);

    // ==================== Marketing Analytics ====================

    /**
     * Get marketing attribution data
     * Analyzes which channels/campaigns drive sales
     * 
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return list of marketing attribution data
     */
    List<MarketingAttribution> getMarketingAttribution(LocalDate startDate, LocalDate endDate);

    // ==================== Cart Abandonment Analytics ====================

    /**
     * Get cart abandonment statistics
     * 
     * @param startDate start date for analysis period (inclusive)
     * @param endDate end date for analysis period (inclusive)
     * @return cart abandonment statistics
     */
    CartAbandonment getCartAbandonmentStats(LocalDate startDate, LocalDate endDate);
}
