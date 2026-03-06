package com.mustapha.ecommerce.analytics.api.rest;

import com.mustapha.ecommerce.analytics.api.dto.*;
import com.mustapha.ecommerce.analytics.application.facade.AnalyticsFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Owner Analytics REST Controller
 * 
 * Provides business intelligence endpoints for store owners
 * Requires OWNER or EMPLOYEE role (analytics access)
 * 
 * Responsibilities:
 * - Accept HTTP requests
 * - Validate request parameters (Spring validation)
 * - Delegate to Facade (translation layer)
 * - Return HTTP responses
 * 
 * What this is NOT:
 * - NOT a business logic layer (that's in Domain)
 * - NOT a transaction manager (that's in Use Cases)
 * - NOT a DTO mapper (that's in Facade)
 * 
 * Pattern: REST Controller (Presentation Layer)
 * Think: "HTTP Router"
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/owner/analytics")
@RequiredArgsConstructor
@Tag(name = "Owner Analytics", description = "Business intelligence and reporting for store owners")
@SecurityRequirement(name = "Bearer Authentication")
public class OwnerAnalyticsController {

    private final AnalyticsFacade analyticsFacade;

    /**
     * Get best-selling products by units sold
     * 
     * Example:
     * GET /api/owner/analytics/products/best-selling?limit=10&startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: List of products ordered by units sold (descending)
     */
    @GetMapping("/products/best-selling")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get best-selling products",
        description = "Returns products ranked by units sold (highest to lowest). " +
                     "Useful for inventory planning and marketing decisions."
    )
    public ResponseEntity<List<ProductPerformanceDTO>> getBestSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching best-selling products: limit={}, period={} to {}", limit, startDate, endDate);
        
        List<ProductPerformanceDTO> products = analyticsFacade.getBestSellingProducts(limit, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Get worst-selling products by units sold
     * 
     * Example:
     * GET /api/owner/analytics/products/worst-selling?limit=10&startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: List of products ordered by units sold (ascending)
     */
    @GetMapping("/products/worst-selling")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get worst-selling products",
        description = "Returns products ranked by units sold (lowest to highest). " +
                     "Useful for identifying slow-moving inventory and clearance candidates."
    )
    public ResponseEntity<List<ProductPerformanceDTO>> getWorstSellingProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching worst-selling products: limit={}, period={} to {}", limit, startDate, endDate);
        
        List<ProductPerformanceDTO> products = analyticsFacade.getWorstSellingProducts(limit, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Get top revenue-generating products
     * 
     * Example:
     * GET /api/owner/analytics/products/top-revenue?limit=10&startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: List of products ordered by total revenue (descending)
     */
    @GetMapping("/products/top-revenue")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get top revenue products",
        description = "Returns products ranked by total revenue (highest to lowest). " +
                     "Different from best-selling as it accounts for price (high-priced items may generate more revenue with fewer units)."
    )
    public ResponseEntity<List<ProductPerformanceDTO>> getTopRevenueProducts(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching top revenue products: limit={}, period={} to {}", limit, startDate, endDate);
        
        List<ProductPerformanceDTO> products = analyticsFacade.getTopRevenueProducts(limit, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(products);
    }

    /**
     * Get daily sales report
     * 
     * Example:
     * GET /api/owner/analytics/sales/daily?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Daily breakdown of orders and revenue
     */
    @GetMapping("/sales/daily")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get daily sales report",
        description = "Returns daily sales statistics including order count, revenue, and average order value. " +
                     "Useful for tracking daily performance and identifying trends."
    )
    public ResponseEntity<List<DailySalesDTO>> getDailySales(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching daily sales: period={} to {}", startDate, endDate);
        
        List<DailySalesDTO> dailySales = analyticsFacade.getDailySales(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(dailySales);
    }

    /**
     * Get peak sales day (highest revenue)
     * 
     * Example:
     * GET /api/owner/analytics/sales/peak-day?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Single day with highest revenue
     */
    @GetMapping("/sales/peak-day")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get peak sales day",
        description = "Returns the day with highest revenue in the given period. " +
                     "Useful for identifying successful promotions or high-traffic days."
    )
    public ResponseEntity<DailySalesDTO> getPeakSalesDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching peak sales day: period={} to {}", startDate, endDate);
        
        return analyticsFacade.getPeakSalesDay(startDate, endDate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get slowest sales day (lowest revenue)
     * 
     * Example:
     * GET /api/owner/analytics/sales/slowest-day?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Single day with lowest revenue
     */
    @GetMapping("/sales/slowest-day")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get slowest sales day",
        description = "Returns the day with lowest revenue in the given period. " +
                     "Useful for identifying slow days that may need promotional activity."
    )
    public ResponseEntity<DailySalesDTO> getSlowestSalesDay(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching slowest sales day: period={} to {}", startDate, endDate);
        
        return analyticsFacade.getSlowestSalesDay(startDate, endDate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get overall sales summary
     * 
     * Example:
     * GET /api/owner/analytics/sales/summary?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Aggregated statistics (total orders, revenue, average order value, completion rate)
     */
    @GetMapping("/sales/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get sales summary",
        description = "Returns aggregated sales statistics including total orders, revenue, " +
                     "average order value, and order status breakdown. " +
                     "Useful for high-level performance overview."
    )
    public ResponseEntity<SalesSummaryDTO> getSalesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching sales summary: period={} to {}", startDate, endDate);
        
        return analyticsFacade.getSalesSummary(startDate, endDate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ====================================================================
    // CUSTOMER ANALYTICS ENDPOINTS
    // ====================================================================

    /**
     * Get top customers by spending
     * 
     * Example:
     * GET /api/owner/analytics/customers/top-buyers?limit=20&startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: List of customers ranked by total spending (descending)
     */
    @GetMapping("/customers/top-buyers")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get top customers by spending",
        description = "Returns customers ranked by total spending. " +
                     "Includes total orders, total spent, and average order value. " +
                     "Useful for identifying VIP customers and loyalty program targeting."
    )
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching top customers: limit={}, period={} to {}", limit, startDate, endDate);
        
        List<TopCustomerDTO> topCustomers = analyticsFacade.getTopCustomers(limit, startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(topCustomers);
    }

    /**
     * Get customer retention metrics
     * 
     * Example:
     * GET /api/owner/analytics/customers/retention?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Retention statistics (total customers, returning customers, retention rate, churn rate)
     */
    @GetMapping("/customers/retention")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get customer retention metrics",
        description = "Returns customer retention statistics including total customers, " +
                     "returning customers, new customers, retention rate, and churn rate. " +
                     "Critical metric for measuring customer loyalty and business health."
    )
    public ResponseEntity<CustomerRetentionDTO> getCustomerRetention(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching customer retention metrics: period={} to {}", startDate, endDate);
        
        return analyticsFacade.getCustomerRetention(startDate, endDate)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ====================================================================
    // INVENTORY ANALYTICS ENDPOINTS
    // ====================================================================

    /**
     * Get low stock products
     * 
     * Example:
     * GET /api/owner/analytics/inventory/low-stock?threshold=10
     * 
     * Response: List of products below stock threshold
     */
    @GetMapping("/inventory/low-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get low stock products",
        description = "Returns products with stock levels below the specified threshold. " +
                     "Includes sales velocity to help prioritize reorder decisions. " +
                     "Critical for preventing stockouts and maintaining service levels."
    )
    public ResponseEntity<List<LowStockProductDTO>> getLowStockProducts(
            @RequestParam(defaultValue = "10") int threshold) {
        
        log.info("Fetching low stock products: threshold={}", threshold);
        
        List<LowStockProductDTO> lowStockProducts = analyticsFacade.getLowStockProducts(threshold);
        return ResponseEntity.status(HttpStatus.OK).body(lowStockProducts);
    }

    /**
     * Get out of stock products
     * 
     * Example:
     * GET /api/owner/analytics/inventory/out-of-stock
     * 
     * Response: List of products with zero stock
     */
    @GetMapping("/inventory/out-of-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get out of stock products",
        description = "Returns products that are currently out of stock (stock = 0). " +
                     "Urgent attention required to restore inventory and prevent lost sales."
    )
    public ResponseEntity<List<LowStockProductDTO>> getOutOfStockProducts() {
        
        log.info("Fetching out of stock products");
        
        List<LowStockProductDTO> outOfStockProducts = analyticsFacade.getOutOfStockProducts();
        return ResponseEntity.status(HttpStatus.OK).body(outOfStockProducts);
    }

    /**
     * Get inventory turnover analysis
     * 
     * Example:
     * GET /api/owner/analytics/inventory/turnover?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Products with turnover rates (sales velocity vs stock levels)
     */
    @GetMapping("/inventory/turnover")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get inventory turnover analysis",
        description = "Returns products with calculated turnover rate (units sold / average stock). " +
                     "High turnover = fast-moving inventory (good). " +
                     "Low turnover = slow-moving inventory (may need discounting). " +
                     "Includes estimated days to sell current stock."
    )
    public ResponseEntity<List<InventoryTurnoverDTO>> getInventoryTurnover(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching inventory turnover: period={} to {}", startDate, endDate);
        
        List<InventoryTurnoverDTO> turnover = analyticsFacade.getInventoryTurnover(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(turnover);
    }

    /**
     * Get dead stock products (zero sales)
     * 
     * Example:
     * GET /api/owner/analytics/inventory/dead-stock
     * 
     * Response: Products with no sales ever (lifetime)
     */
    @GetMapping("/inventory/dead-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get dead stock products",
        description = "Returns products with ZERO sales (lifetime, not period-based). " +
                     "Dead stock ties up capital and warehouse space. " +
                     "Candidates for clearance sales, bundling, or removal from product line."
    )
    public ResponseEntity<List<LowStockProductDTO>> getDeadStockProducts() {
        
        log.info("Fetching dead stock products");
        
        List<LowStockProductDTO> deadStock = analyticsFacade.getDeadStockProducts();
        return ResponseEntity.status(HttpStatus.OK).body(deadStock);
    }

    // ====================================================================
    // PAYMENT ANALYTICS ENDPOINTS
    // ====================================================================

    /**
     * Get payment method statistics
     * 
     * Example:
     * GET /api/owner/analytics/payments/method-stats?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Statistics for each payment method (transaction count, total amount, success rate)
     */
    @GetMapping("/payments/method-stats")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get payment method statistics",
        description = "Returns statistics for each payment method including transaction counts, " +
                     "total amounts, success/failure counts, and success rates. " +
                     "Useful for identifying popular payment methods and detecting issues."
    )
    public ResponseEntity<List<PaymentMethodStatsDTO>> getPaymentMethodStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching payment method statistics: period={} to {}", startDate, endDate);
        
        List<PaymentMethodStatsDTO> stats = analyticsFacade.getPaymentMethodStats(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    // ====================================================================
    // TIME-BASED ANALYTICS ENDPOINTS
    // ====================================================================

    /**
     * Get sales by hour of day
     * 
     * Example:
     * GET /api/owner/analytics/sales/by-hour?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Sales aggregated by hour (0-23)
     */
    @GetMapping("/sales/by-hour")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get sales by hour of day",
        description = "Returns sales aggregated by hour of the day (0-23). " +
                     "Useful for staffing decisions and identifying peak shopping hours. " +
                     "Can inform promotional timing and server capacity planning."
    )
    public ResponseEntity<List<HourlySalesDTO>> getSalesByHour(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching sales by hour: period={} to {}", startDate, endDate);
        
        List<HourlySalesDTO> hourlySales = analyticsFacade.getSalesByHour(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(hourlySales);
    }

    /**
     * Get sales by day of week
     * 
     * Example:
     * GET /api/owner/analytics/sales/by-day-of-week?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Sales aggregated by day of week (Monday-Sunday)
     */
    @GetMapping("/sales/by-day-of-week")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get sales by day of week",
        description = "Returns sales aggregated by day of week (Monday-Sunday). " +
                     "Identifies weekly patterns for inventory planning and promotion scheduling. " +
                     "Helps optimize marketing campaigns for high-traffic days."
    )
    public ResponseEntity<List<DailySalesDTO>> getSalesByDayOfWeek(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching sales by day of week: period={} to {}", startDate, endDate);
        
        List<DailySalesDTO> salesByDay = analyticsFacade.getSalesByDayOfWeek(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(salesByDay);
    }

    // ====================================================================
    // REVENUE ANALYTICS ENDPOINTS
    // ====================================================================

    /**
     * Get revenue by product category
     * 
     * Example:
     * GET /api/owner/analytics/revenue/by-category?startDate=2026-01-01&endDate=2026-03-01
     * 
     * Response: Revenue breakdown by category
     */
    @GetMapping("/revenue/by-category")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get revenue by product category",
        description = "Returns revenue breakdown by product category. " +
                     "Shows which categories drive the most revenue and units sold. " +
                     "Critical for merchandising decisions and marketing budget allocation."
    )
    public ResponseEntity<List<CategoryRevenueDTO>> getRevenueByCategory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching revenue by category: period={} to {}", startDate, endDate);
        
        List<CategoryRevenueDTO> categoryRevenue = analyticsFacade.getRevenueByCategory(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(categoryRevenue);
    }

    /**
     * Get revenue forecast based on historical trends
     * 
     * Example:
     * GET /api/owner/analytics/revenue/forecast?historicalStartDate=2025-12-01&historicalEndDate=2026-03-01&forecastDays=30
     * 
     * Response: Revenue predictions with confidence intervals and trend analysis
     */
    @GetMapping("/revenue/forecast")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get revenue forecast",
        description = "Returns revenue forecast based on statistical analysis of historical data. " +
                     "Uses linear regression for trend detection and variance analysis for confidence intervals. " +
                     "Includes daily predictions, growth rate, and confidence level. " +
                     "Critical for budget planning, inventory decisions, and staffing forecasts."
    )
    public ResponseEntity<RevenueForecastDTO> getRevenueForecast(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate historicalStartDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate historicalEndDate,
            @RequestParam(defaultValue = "30") int forecastDays) {
        
        log.info("Fetching revenue forecast: historical period={} to {}, forecast days={}", 
            historicalStartDate, historicalEndDate, forecastDays);
        
        RevenueForecastDTO forecast = analyticsFacade.getRevenueForecast(historicalStartDate, historicalEndDate, forecastDays);
        return ResponseEntity.status(HttpStatus.OK).body(forecast);
    }

    // ==================== Profit Analytics ====================

    /**
     * Get profit margins for products
     * 
     * Example:
     * GET /api/owner/analytics/profit/margins?startDate=2026-01-01&endDate=2026-03-03&limit=50&sortByProfit=true
     * 
     * Response: List of products with revenue, cost, profit, and margin %
     */
    @GetMapping("/profit/margins")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get profit margins by product",
        description = "Returns profitability metrics for products including revenue, COGS, profit, and margin percentage. " +
                     "Requires COGS data to be populated in product records. " +
                     "Essential for identifying high-margin vs low-margin products, pricing decisions, and product mix optimization. " +
                     "Can sort by total profit amount or by margin percentage to find most/least profitable items."
    )
    public ResponseEntity<List<ProfitMarginDTO>> getProfitMargins(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "true") boolean sortByProfit) {
        
        log.info("Fetching profit margins: period={} to {}, limit={}, sortByProfit={}", 
            startDate, endDate, limit, sortByProfit);
        
        List<ProfitMarginDTO> margins = analyticsFacade.getProfitMargins(limit, startDate, endDate, sortByProfit);
        return ResponseEntity.status(HttpStatus.OK).body(margins);
    }

    // ==================== Refund Analytics ====================

    /**
     * Get refund statistics
     * 
     * Example:
     * GET /api/owner/analytics/refunds/stats?startDate=2026-01-01&endDate=2026-03-03
     * 
     * Response: Refund metrics including refund rate, total amount, average refund
     */
    @GetMapping("/refunds/stats")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get refund statistics",
        description = "Returns comprehensive refund metrics including request counts by status, refund rate, total refund amount, and average refund. " +
                     "Refund rate >5% is flagged as potentially concerning and may indicate quality issues. " +
                     "Critical for understanding customer satisfaction, product quality issues, and return policy effectiveness. " +
                     "Helps identify trends that may require intervention or policy adjustments."
    )
    public ResponseEntity<RefundStatsDTO> getRefundStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching refund stats: period={} to {}", startDate, endDate);
        
        RefundStatsDTO stats = analyticsFacade.getRefundStats(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }

    // ==================== Geographic Analytics ====================

    /**
     * Get sales by geographic location
     * 
     * Example:
     * GET /api/owner/analytics/geographic/sales?startDate=2026-01-01&endDate=2026-03-03&groupByCity=true
     * 
     * Response: List of locations with order count and revenue
     */
    @GetMapping("/geographic/sales")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get sales by geographic location",
        description = "Returns sales breakdown by city, state, or country. " +
                     "Can group by city for detailed analysis or by state for regional overview. " +
                     "Includes order count, total revenue, and average order value per location. " +
                     "Essential for identifying high-performing regions, planning regional marketing, " +
                     "optimizing warehouse locations, and understanding geographic customer distribution."
    )
    public ResponseEntity<List<GeographicSalesDTO>> getSalesByLocation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "false") boolean groupByCity) {
        
        log.info("Fetching sales by location: period={} to {}, groupByCity={}", 
            startDate, endDate, groupByCity);
        
        List<GeographicSalesDTO> sales = analyticsFacade.getSalesByLocation(startDate, endDate, groupByCity);
        return ResponseEntity.status(HttpStatus.OK).body(sales);
    }

    // ==================== Shipping Analytics ====================

    /**
     * Get shipping performance by carrier
     * 
     * Example:
     * GET /api/owner/analytics/shipping/performance?startDate=2026-01-01&endDate=2026-03-03
     * 
     * Response: List of carriers with delivery metrics
     */
    @GetMapping("/shipping/performance")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get shipping performance by carrier",
        description = "Returns delivery performance metrics for each carrier including shipment counts, " +
                     "delivery success rate, average time to ship, and average delivery time. " +
                     "Measures efficiency from order creation through shipping to final delivery. " +
                     "Critical for carrier selection, identifying delivery bottlenecks, improving customer satisfaction, " +
                     "and negotiating with carriers. Performance is flagged as 'good' if >95% delivery rate and <48h delivery."
    )
    public ResponseEntity<List<ShippingPerformanceDTO>> getShippingPerformance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching shipping performance: period={} to {}", startDate, endDate);
        
        List<ShippingPerformanceDTO> performance = analyticsFacade.getShippingPerformance(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(performance);
    }

    // ==================== Marketing Analytics ====================

    /**
     * Get marketing attribution data
     * 
     * Example:
     * GET /api/owner/analytics/marketing/attribution?startDate=2026-01-01&endDate=2026-03-03
     * 
     * Response: List of marketing channels with conversion metrics
     */
    @GetMapping("/marketing/attribution")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get marketing attribution data",
        description = "Returns sales attributed to marketing channels and campaigns including order count, " +
                     "total revenue, average order value, customer count, and conversion rate. " +
                     "Tracks UTM parameters (utm_source, utm_campaign) from order creation. " +
                     "Essential for measuring marketing ROI, identifying best-performing channels, " +
                     "optimizing marketing spend allocation, and understanding customer acquisition costs. " +
                     "Orders without UTM data are attributed to 'direct' source."
    )
    public ResponseEntity<List<MarketingAttributionDTO>> getMarketingAttribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching marketing attribution: period={} to {}", startDate, endDate);
        
        List<MarketingAttributionDTO> attribution = analyticsFacade.getMarketingAttribution(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(attribution);
    }

    // ==================== Cart Abandonment Analytics ====================

    /**
     * Get cart abandonment statistics
     * 
     * Example:
     * GET /api/owner/analytics/carts/abandonment?startDate=2026-01-01&endDate=2026-03-03
     * 
     * Response: Cart abandonment metrics including rates and potential recovery
     */
    @GetMapping("/carts/abandonment")
    @PreAuthorize("hasAnyRole('OWNER', 'EMPLOYEE')")
    @Operation(
        summary = "Get cart abandonment statistics",
        description = "Returns shopping cart abandonment metrics including total carts, active, converted, and abandoned counts. " +
                     "Calculates abandonment rate, conversion rate, total abandoned value, and potential recovery revenue. " +
                     "Abandonment rate >80% is flagged as concerning and may indicate checkout friction or technical issues. " +
                     "Industry average abandonment rate is ~70%. " +
                     "Critical for understanding conversion funnel drop-off, optimizing checkout flow, " +
                     "planning cart recovery campaigns, and estimating lost revenue opportunities."
    )
    public ResponseEntity<CartAbandonmentDTO> getCartAbandonmentStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        log.info("Fetching cart abandonment stats: period={} to {}", startDate, endDate);
        
        CartAbandonmentDTO stats = analyticsFacade.getCartAbandonmentStats(startDate, endDate);
        return ResponseEntity.status(HttpStatus.OK).body(stats);
    }
}
