package com.mustapha.ecommerce.analytics.application.facade;

import com.mustapha.ecommerce.analytics.api.dto.*;
import com.mustapha.ecommerce.analytics.application.exception.InvalidDateRangeException;
import com.mustapha.ecommerce.analytics.application.exception.InvalidLimitException;
import com.mustapha.ecommerce.analytics.application.exception.InvalidQueryParametersException;
import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.*;
import com.mustapha.ecommerce.analytics.application.usecase.*;
import com.mustapha.ecommerce.analytics.domain.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Analytics Facade - Translation Layer between API and Application
 * 
 * Responsibilities:
 * 1. Accept API parameters (LocalDate primitives, int limits)
 * 2. VALIDATE parameters (business rules)
 * 3. Convert parameters → Query objects
 * 4. Delegate to Use Cases (no business logic here)
 * 5. Convert Domain → API DTOs
 * 
 * What this is NOT:
 * - NOT a business logic layer (that's in Domain)
 * - NOT a transaction manager (that's in Use Cases)
 * - NOT a data access layer (that's in Repository)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 * Think: "Translator + Router + Validator"
 */
@Service
public class AnalyticsFacade {
    
    // Validation constants
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 100;
    private static final int MAX_DATE_RANGE_DAYS = 365;

    private final GetBestSellingProductsUseCase getBestSellingProductsUseCase;
    private final GetWorstSellingProductsUseCase getWorstSellingProductsUseCase;
    private final GetTopRevenueProductsUseCase getTopRevenueProductsUseCase;
    private final GetDailySalesUseCase getDailySalesUseCase;
    private final GetPeakSalesDayUseCase getPeakSalesDayUseCase;
    private final GetSlowestSalesDayUseCase getSlowestSalesDayUseCase;
    private final GetSalesSummaryUseCase getSalesSummaryUseCase;
    private final AnalyticsQueryPort analyticsQueryPort;

    public AnalyticsFacade(GetBestSellingProductsUseCase getBestSellingProductsUseCase,
                          GetWorstSellingProductsUseCase getWorstSellingProductsUseCase,
                          GetTopRevenueProductsUseCase getTopRevenueProductsUseCase,
                          GetDailySalesUseCase getDailySalesUseCase,
                          GetPeakSalesDayUseCase getPeakSalesDayUseCase,
                          GetSlowestSalesDayUseCase getSlowestSalesDayUseCase,
                          GetSalesSummaryUseCase getSalesSummaryUseCase,
                          AnalyticsQueryPort analyticsQueryPort) {
        this.getBestSellingProductsUseCase = getBestSellingProductsUseCase;
        this.getWorstSellingProductsUseCase = getWorstSellingProductsUseCase;
        this.getTopRevenueProductsUseCase = getTopRevenueProductsUseCase;
        this.getDailySalesUseCase = getDailySalesUseCase;
        this.getPeakSalesDayUseCase = getPeakSalesDayUseCase;
        this.getSlowestSalesDayUseCase = getSlowestSalesDayUseCase;
        this.getSalesSummaryUseCase = getSalesSummaryUseCase;
        this.analyticsQueryPort = analyticsQueryPort;
    }

    // ==================== Public API Methods ====================
    
    /**
     * Get Best Selling Products
     * 
     * Flow:
     * 1. VALIDATE parameters
     * 2. Convert to Query object
     * 3. Call Use Case (handles @Transactional, business orchestration)
     * 4. Return API DTOs
     */
    public List<ProductPerformanceDTO> getBestSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        // Step 1: Validate input parameters
        validateLimit(limit);
        validateDateRange(startDate, endDate);
        
        // Step 2: Convert API parameters → Query object
        GetBestSellingProductsQuery query = new GetBestSellingProductsQuery(limit, startDate, endDate);
        
        // Step 3: Delegate to Use Case (handles @Transactional)
        List<ProductPerformance> products = getBestSellingProductsUseCase.execute(query);
        
        // Step 4: Convert Domain → API DTOs
        return products.stream()
            .map(this::toProductPerformanceDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get Worst Selling Products
     */
    public List<ProductPerformanceDTO> getWorstSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        validateLimit(limit);
        validateDateRange(startDate, endDate);
        
        GetWorstSellingProductsQuery query = new GetWorstSellingProductsQuery(limit, startDate, endDate);
        List<ProductPerformance> products = getWorstSellingProductsUseCase.execute(query);
        return products.stream()
            .map(this::toProductPerformanceDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get Top Revenue Products
     */
    public List<ProductPerformanceDTO> getTopRevenueProducts(int limit, LocalDate startDate, LocalDate endDate) {
        validateLimit(limit);
        validateDateRange(startDate, endDate);
        
        GetTopRevenueProductsQuery query = new GetTopRevenueProductsQuery(limit, startDate, endDate);
        List<ProductPerformance> products = getTopRevenueProductsUseCase.execute(query);
        return products.stream()
            .map(this::toProductPerformanceDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get Daily Sales
     */
    public List<DailySalesDTO> getDailySales(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        GetDailySalesQuery query = new GetDailySalesQuery(startDate, endDate);
        List<DailySales> dailySales = getDailySalesUseCase.execute(query);
        return dailySales.stream()
            .map(this::toDailySalesDTO)
            .collect(Collectors.toList());
    }

    /**
     * Get Peak Sales Day
     */
    public Optional<DailySalesDTO> getPeakSalesDay(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        GetPeakSalesDayQuery query = new GetPeakSalesDayQuery(startDate, endDate);
        return getPeakSalesDayUseCase.execute(query)
            .map(this::toDailySalesDTO);
    }

    /**
     * Get Slowest Sales Day
     */
    public Optional<DailySalesDTO> getSlowestSalesDay(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        GetSlowestSalesDayQuery query = new GetSlowestSalesDayQuery(startDate, endDate);
        return getSlowestSalesDayUseCase.execute(query)
            .map(this::toDailySalesDTO);
    }

    /**
     * Get Sales Summary
     */
    public Optional<SalesSummaryDTO> getSalesSummary(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        GetSalesSummaryQuery query = new GetSalesSummaryQuery(startDate, endDate);
        return getSalesSummaryUseCase.execute(query)
            .map(this::toSalesSummaryDTO);
    }

    // ==================== Validation Methods ====================
    
    /**
     * Validate limit parameter
     * @throws InvalidLimitException if limit is out of valid range
     */
    private void validateLimit(int limit) {
        if (limit < MIN_LIMIT || limit > MAX_LIMIT) {
            throw new InvalidLimitException(limit, MIN_LIMIT, MAX_LIMIT);
        }
    }
    
    /**
     * Validate date range
     * @throws InvalidQueryParametersException if dates are null
     * @throws InvalidDateRangeException if date range is invalid
     */
    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        // Check for null dates
        if (startDate == null) {
            throw new InvalidQueryParametersException("startDate", "Start date cannot be null");
        }
        if (endDate == null) {
            throw new InvalidQueryParametersException("endDate", "End date cannot be null");
        }
        
        // Check start date is before or equal to end date
        if (startDate.isAfter(endDate)) {
            throw new InvalidDateRangeException(startDate, endDate);
        }
        
        // Check date range is not too large (performance protection)
        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
        if (daysBetween > MAX_DATE_RANGE_DAYS) {
            throw new InvalidDateRangeException(startDate, endDate, 
                String.format("Date range too large (%d days). Maximum allowed: %d days", 
                    daysBetween, MAX_DATE_RANGE_DAYS));
        }
        
        // Check dates are not in the future
        LocalDate today = LocalDate.now();
        if (startDate.isAfter(today)) {
            throw new InvalidDateRangeException(startDate, endDate, 
                "Start date cannot be in the future");
        }
        if (endDate.isAfter(today)) {
            throw new InvalidDateRangeException(startDate, endDate, 
                "End date cannot be in the future");
        }
    }

    // ==================== Private Mapping Methods ====================

    /**
     * Convert Domain → API DTO
     * Responsibility: Map domain value object to API DTO
     */
    private ProductPerformanceDTO toProductPerformanceDTO(ProductPerformance product) {
        return new ProductPerformanceDTO(
            product.getProductId(),
            product.getProductName(),
            product.getUnitsSold(),
            product.getTotalRevenue(),
            product.getOrderCount()
        );
    }

    /**
     * Convert Domain → API DTO
     */
    private DailySalesDTO toDailySalesDTO(DailySales dailySales) {
        return new DailySalesDTO(
            dailySales.getDate(),
            dailySales.getOrderCount(),
            dailySales.getRevenue(),
            dailySales.getAverageOrderValue()
        );
    }

    /**
     * Convert Domain → API DTO
     * Note: Completion rate calculation done in domain object
     */
    private SalesSummaryDTO toSalesSummaryDTO(SalesSummary summary) {
        return new SalesSummaryDTO(
            summary.getTotalOrders(),
            summary.getTotalRevenue(),
            summary.getAverageOrderValue(),
            summary.getCompletedOrders(),
            summary.getCancelledOrders(),
            summary.getPendingOrders(),
            summary.getCompletionRate()
        );
    }

    // ==================== Customer Analytics ====================

    /**
     * Get Top Customers
     */
    public List<TopCustomerDTO> getTopCustomers(int limit, LocalDate startDate, LocalDate endDate) {
        validateLimit(limit);
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getTopCustomers(limit, startDate, endDate)
            .stream()
            .map(this::toTopCustomerDTO)
            .toList();
    }

    /**
     * Get Customer Retention
     */
    public Optional<CustomerRetentionDTO> getCustomerRetention(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getCustomerRetention(startDate, endDate)
            .map(this::toCustomerRetentionDTO);
    }

    // ==================== Inventory Analytics ====================

    /**
     * Get Low Stock Products
     */
    public List<LowStockProductDTO> getLowStockProducts(int threshold) {
        if (threshold < 0) {
            throw new InvalidQueryParametersException("threshold", "Stock threshold must be non-negative");
        }
        
        return analyticsQueryPort.getLowStockProducts(threshold)
            .stream()
            .map(this::toLowStockProductDTO)
            .toList();
    }

    /**
     * Get Out-of-Stock Products
     */
    public List<LowStockProductDTO> getOutOfStockProducts() {
        return analyticsQueryPort.getOutOfStockProducts()
            .stream()
            .map(this::toLowStockProductDTO)
            .toList();
    }

    /**
     * Get Inventory Turnover
     */
    public List<InventoryTurnoverDTO> getInventoryTurnover(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getInventoryTurnover(startDate, endDate)
            .stream()
            .map(this::toInventoryTurnoverDTO)
            .toList();
    }

    /**
     * Get Dead Stock Products
     */
    public List<LowStockProductDTO> getDeadStockProducts() {
        return analyticsQueryPort.getDeadStockProducts()
            .stream()
            .map(this::toLowStockProductDTO)
            .toList();
    }

    // ==================== Payment Analytics ====================

    /**
     * Get Payment Method Stats
     */
    public List<PaymentMethodStatsDTO> getPaymentMethodStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getPaymentMethodStats(startDate, endDate)
            .stream()
            .map(this::toPaymentMethodStatsDTO)
            .toList();
    }

    // ==================== Time Analytics ====================

    /**
     * Get Sales by Hour
     */
    public List<HourlySalesDTO> getSalesByHour(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getSalesByHour(startDate, endDate)
            .stream()
            .map(this::toHourlySalesDTO)
            .toList();
    }

    /**
     * Get Sales by Day of Week
     */
    public List<DailySalesDTO> getSalesByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getSalesByDayOfWeek(startDate, endDate)
            .stream()
            .map(this::toDailySalesDTO)
            .toList();
    }

    // ==================== Revenue Analytics ====================

    /**
     * Get Revenue by Category
     */
    public List<CategoryRevenueDTO> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        return analyticsQueryPort.getRevenueByCategory(startDate, endDate)
            .stream()
            .map(this::toCategoryRevenueDTO)
            .toList();
    }

    // ==================== DTO Converters ====================

    private TopCustomerDTO toTopCustomerDTO(TopCustomer customer) {
        return new TopCustomerDTO(
            customer.getCustomerId(),
            customer.getCustomerName(),
            customer.getCustomerEmail(),
            customer.getTotalOrders(),
            customer.getTotalSpent(),
            customer.getAverageOrderValue()
        );
    }

    private CustomerRetentionDTO toCustomerRetentionDTO(CustomerRetention retention) {
        return new CustomerRetentionDTO(
            retention.getTotalCustomers(),
            retention.getReturningCustomers(),
            retention.getNewCustomers(),
            retention.getRetentionRate(),
            retention.getChurnRate()
        );
    }

    private LowStockProductDTO toLowStockProductDTO(LowStockProduct product) {
        return new LowStockProductDTO(
            product.getProductId(),
            product.getProductName(),
            product.getCurrentStock(),
            product.getStockThreshold(),
            product.getTotalSold(),
            product.isCritical()
        );
    }

    private InventoryTurnoverDTO toInventoryTurnoverDTO(InventoryTurnover turnover) {
        return new InventoryTurnoverDTO(
            turnover.getProductId(),
            turnover.getProductName(),
            turnover.getUnitsSold(),
            turnover.getAverageStock(),
            turnover.getTurnoverRate(),
            turnover.getDaysToSellOut()
        );
    }

    private PaymentMethodStatsDTO toPaymentMethodStatsDTO(PaymentMethodStats stats) {
        return new PaymentMethodStatsDTO(
            stats.getPaymentMethod(),
            stats.getTransactionCount(),
            stats.getTotalAmount(),
            stats.getSuccessfulCount(),
            stats.getFailedCount(),
            stats.getSuccessRate(),
            stats.getFailureRate()
        );
    }

    private HourlySalesDTO toHourlySalesDTO(HourlySales sales) {
        return new HourlySalesDTO(
            sales.getHour(),
            sales.getOrderCount(),
            sales.getRevenue()
        );
    }

    private CategoryRevenueDTO toCategoryRevenueDTO(CategoryRevenue revenue) {
        return new CategoryRevenueDTO(
            revenue.getCategory(),
            revenue.getProductCount(),
            revenue.getUnitsSold(),
            revenue.getTotalRevenue(),
            revenue.getAverageProductRevenue()
        );
    }

    // ==================== Revenue Forecasting ====================

    /**
     * Get Revenue Forecast
     * 
     * Analyzes historical data and projects future revenue using:
     * - Linear regression for trend detection
     * - Statistical variance for confidence intervals
     * - Moving average for smoothing
     */
    public RevenueForecastDTO getRevenueForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays) {
        validateDateRange(historicalStartDate, historicalEndDate);
        
        if (forecastDays < 1 || forecastDays > 365) {
            throw new InvalidQueryParametersException("forecastDays", 
                "Forecast days must be between 1 and 365 (current: " + forecastDays + ")");
        }
        
        RevenueForecast forecast = analyticsQueryPort.getRevenueForecast(historicalStartDate, historicalEndDate, forecastDays);
        return toRevenueForecastDTO(forecast);
    }

    private RevenueForecastDTO toRevenueForecastDTO(RevenueForecast forecast) {
        List<RevenueForecastDTO.DailyForecastDTO> dailyForecasts = forecast.getDailyForecasts().stream()
            .map(df -> new RevenueForecastDTO.DailyForecastDTO(
                df.getDate(),
                df.getPredictedRevenue(),
                df.getLowerBound(),
                df.getUpperBound()
            ))
            .toList();
        
        return new RevenueForecastDTO(
            forecast.getForecastStartDate(),
            forecast.getForecastEndDate(),
            forecast.getForecastDays(),
            forecast.getPredictedRevenue(),
            forecast.getLowerBound(),
            forecast.getUpperBound(),
            forecast.getAverageDailyRevenue(),
            forecast.getDailyGrowthRate(),
            forecast.getTrend(),
            forecast.getConfidence(),
            forecast.getHistoricalDays(),
            dailyForecasts
        );
    }

    // ==================== Profit Analytics ====================

    /**
     * Get profit margins for products
     * Requires COGS data to be populated
     */
    public List<ProfitMarginDTO> getProfitMargins(int limit, LocalDate startDate, LocalDate endDate, boolean sortByProfit) {
        validateLimit(limit);
        validateDateRange(startDate, endDate);
        
        List<ProfitMargin> margins = analyticsQueryPort.getProfitMargins(limit, startDate, endDate, sortByProfit);
        return margins.stream()
            .map(this::toProfitMarginDTO)
            .toList();
    }

    private ProfitMarginDTO toProfitMarginDTO(ProfitMargin margin) {
        return new ProfitMarginDTO(
            margin.getProductId(),
            margin.getProductName(),
            margin.getUnitsSold(),
            margin.getRevenue(),
            margin.getCost(),
            margin.getProfit(),
            margin.getProfitMarginPercent(),
            margin.isProfitable()
        );
    }

    // ==================== Refund Analytics ====================

    /**
     * Get refund statistics for a period
     */
    public RefundStatsDTO getRefundStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        RefundStats stats = analyticsQueryPort.getRefundStats(startDate, endDate);
        return toRefundStatsDTO(stats);
    }

    private RefundStatsDTO toRefundStatsDTO(RefundStats stats) {
        return new RefundStatsDTO(
            stats.getTotalCompletedOrders(),
            stats.getRefundRequestedCount(),
            stats.getRefundApprovedCount(),
            stats.getRefundCompletedCount(),
            stats.getRefundRejectedCount(),
            stats.getTotalRefundAmount(),
            stats.getRefundRate(),
            stats.getAverageRefundAmount(),
            stats.isRefundRateHigh()
        );
    }

    // ==================== Geographic Analytics ====================

    /**
     * Get sales breakdown by geographic location
     */
    public List<GeographicSalesDTO> getSalesByLocation(LocalDate startDate, LocalDate endDate, boolean groupByCity) {
        validateDateRange(startDate, endDate);
        
        List<GeographicSales> sales = analyticsQueryPort.getSalesByLocation(startDate, endDate, groupByCity);
        return sales.stream()
            .map(this::toGeographicSalesDTO)
            .toList();
    }

    private GeographicSalesDTO toGeographicSalesDTO(GeographicSales sales) {
        return new GeographicSalesDTO(
            sales.getCity(),
            sales.getState(),
            sales.getCountry(),
            sales.getOrderCount(),
            sales.getTotalRevenue(),
            sales.getAverageOrderValue(),
            sales.getLocationKey()
        );
    }

    // ==================== Shipping Analytics ====================

    /**
     * Get shipping performance by carrier
     */
    public List<ShippingPerformanceDTO> getShippingPerformance(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<ShippingPerformance> performance = analyticsQueryPort.getShippingPerformance(startDate, endDate);
        return performance.stream()
            .map(this::toShippingPerformanceDTO)
            .toList();
    }

    private ShippingPerformanceDTO toShippingPerformanceDTO(ShippingPerformance performance) {
        return new ShippingPerformanceDTO(
            performance.getCarrier(),
            performance.getTotalShipments(),
            performance.getDeliveredCount(),
            performance.getAverageTimeToShipHours(),
            performance.getAverageDeliveryTimeHours(),
            performance.getDeliverySuccessRate(),
            performance.isPerformanceGood()
        );
    }

    // ==================== Marketing Analytics ====================

    /**
     * Get marketing attribution data
     */
    public List<MarketingAttributionDTO> getMarketingAttribution(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        List<MarketingAttribution> attribution = analyticsQueryPort.getMarketingAttribution(startDate, endDate);
        return attribution.stream()
            .map(this::toMarketingAttributionDTO)
            .toList();
    }

    private MarketingAttributionDTO toMarketingAttributionDTO(MarketingAttribution attribution) {
        return new MarketingAttributionDTO(
            attribution.getSource(),
            attribution.getCampaign(),
            attribution.getOrderCount(),
            attribution.getTotalRevenue(),
            attribution.getAverageOrderValue(),
            attribution.getCustomerCount(),
            attribution.getConversionRate(),
            attribution.getChannelKey()
        );
    }

    // ==================== Cart Abandonment Analytics ====================

    /**
     * Get cart abandonment statistics
     */
    public CartAbandonmentDTO getCartAbandonmentStats(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        
        CartAbandonment stats = analyticsQueryPort.getCartAbandonmentStats(startDate, endDate);
        return toCartAbandonmentDTO(stats);
    }

    private CartAbandonmentDTO toCartAbandonmentDTO(CartAbandonment stats) {
        return new CartAbandonmentDTO(
            stats.getTotalCarts(),
            stats.getActiveCarts(),
            stats.getConvertedCarts(),
            stats.getAbandonedCarts(),
            stats.getTotalAbandonedValue(),
            stats.getAverageAbandonedValue(),
            stats.getAbandonmentRate(),
            stats.getConversionRate(),
            stats.isAbandonmentRateHigh(),
            stats.getPotentialRecovery()
        );
    }
}
