package com.mustapha.ecommerce.analytics.infrastructure.adapter.persistence;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.domain.model.*;
import com.mustapha.ecommerce.analytics.domain.repository.AnalyticsRepository;
import com.mustapha.ecommerce.analytics.infrastructure.exception.QueryExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Analytics Query Adapter - Implements Port Interface
 * 
 * Responsibility: Adapt analytics repository to application port
 * Pattern: Adapter (Hexagonal Architecture)
 * SOLID: DIP (implements application interface, uses domain repository)
 * 
 * This adapter:
 * 1. Implements AnalyticsQueryPort (application layer interface)
 * 2. Delegates to AnalyticsRepository (domain layer interface)
 * 3. Adds error handling and logging
 * 4. Translates infrastructure exceptions to application exceptions
 * 
 * Hexagonal Architecture Flow:
 * UseCase → AnalyticsQueryPort (port) → AnalyticsQueryAdapter (adapter) → AnalyticsRepository (domain) → JpaAnalyticsRepository (infrastructure)
 * 
 * Why this extra layer?
 * - Decouples application from infrastructure
 * - Allows different implementations (SQL, NoSQL, REST API, etc.)
 * - Provides single place for infrastructure error handling
 * - Makes testing easier (mock the port, not the repository)
 */
@Slf4j
@Component
public class AnalyticsQueryAdapter implements AnalyticsQueryPort {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsQueryAdapter(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public List<ProductPerformance> getBestSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching best-selling products: limit={}, period={} to {}", limit, startDate, endDate);
            List<ProductPerformance> result = analyticsRepository.getBestSellingProducts(limit, startDate, endDate);
            log.debug("Found {} best-selling products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch best-selling products", ex);
            throw new QueryExecutionException("best-selling products query", ex);
        }
    }

    @Override
    public List<ProductPerformance> getWorstSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching worst-selling products: limit={}, period={} to {}", limit, startDate, endDate);
            List<ProductPerformance> result = analyticsRepository.getWorstSellingProducts(limit, startDate, endDate);
            log.debug("Found {} worst-selling products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch worst-selling products", ex);
            throw new QueryExecutionException("worst-selling products query", ex);
        }
    }

    @Override
    public List<ProductPerformance> getTopRevenueProducts(int limit, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching top revenue products: limit={}, period={} to {}", limit, startDate, endDate);
            List<ProductPerformance> result = analyticsRepository.getTopRevenueProducts(limit, startDate, endDate);
            log.debug("Found {} top revenue products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch top revenue products", ex);
            throw new QueryExecutionException("top revenue products query", ex);
        }
    }

    @Override
    public List<DailySales> getDailySales(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching daily sales: period={} to {}", startDate, endDate);
            List<DailySales> result = analyticsRepository.getDailySales(startDate, endDate);
            log.debug("Found {} daily sales records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch daily sales", ex);
            throw new QueryExecutionException("daily sales query", ex);
        }
    }

    @Override
    public Optional<DailySales> getPeakSalesDay(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching peak sales day: period={} to {}", startDate, endDate);
            Optional<DailySales> result = Optional.ofNullable(analyticsRepository.getPeakSalesDay(startDate, endDate));
            log.debug("Peak sales day: {}", result.isPresent() ? result.get().getDate() : "no data");
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch peak sales day", ex);
            throw new QueryExecutionException("peak sales day query", ex);
        }
    }

    @Override
    public Optional<DailySales> getSlowestSalesDay(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching slowest sales day: period={} to {}", startDate, endDate);
            Optional<DailySales> result = Optional.ofNullable(analyticsRepository.getSlowestSalesDay(startDate, endDate));
            log.debug("Slowest sales day: {}", result.isPresent() ? result.get().getDate() : "no data");
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch slowest sales day", ex);
            throw new QueryExecutionException("slowest sales day query", ex);
        }
    }

    @Override
    public Optional<SalesSummary> getSalesSummary(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching sales summary: period={} to {}", startDate, endDate);
            Optional<SalesSummary> result = Optional.ofNullable(analyticsRepository.getSalesSummary(startDate, endDate));
            log.debug("Sales summary: {}", result.isPresent() ? "found" : "no data");
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch sales summary", ex);
            throw new QueryExecutionException("sales summary query", ex);
        }
    }

    // ==================== Customer Analytics ====================

    @Override
    public List<TopCustomer> getTopCustomers(int limit, LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching top customers: limit={}, period={} to {}", limit, startDate, endDate);
            List<TopCustomer> result = analyticsRepository.getTopCustomers(limit, startDate, endDate);
            log.debug("Found {} top customers", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch top customers", ex);
            throw new QueryExecutionException("top customers query", ex);
        }
    }

    @Override
    public Optional<CustomerRetention> getCustomerRetention(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching customer retention: period={} to {}", startDate, endDate);
            Optional<CustomerRetention> result = Optional.ofNullable(analyticsRepository.getCustomerRetention(startDate, endDate));
            log.debug("Customer retention: {}", result.isPresent() ? "found" : "no data");
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch customer retention", ex);
            throw new QueryExecutionException("customer retention query", ex);
        }
    }

    // ==================== Inventory Analytics ====================

    @Override
    public List<LowStockProduct> getLowStockProducts(int threshold) {
        try {
            log.debug("Fetching low stock products: threshold={}", threshold);
            List<LowStockProduct> result = analyticsRepository.getLowStockProducts(threshold);
            log.debug("Found {} low stock products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch low stock products", ex);
            throw new QueryExecutionException("low stock products query", ex);
        }
    }

    @Override
    public List<LowStockProduct> getOutOfStockProducts() {
        try {
            log.debug("Fetching out-of-stock products");
            List<LowStockProduct> result = analyticsRepository.getOutOfStockProducts();
            log.debug("Found {} out-of-stock products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch out-of-stock products", ex);
            throw new QueryExecutionException("out-of-stock products query", ex);
        }
    }

    @Override
    public List<InventoryTurnover> getInventoryTurnover(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching inventory turnover: period={} to {}", startDate, endDate);
            List<InventoryTurnover> result = analyticsRepository.getInventoryTurnover(startDate, endDate);
            log.debug("Found {} inventory turnover records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch inventory turnover", ex);
            throw new QueryExecutionException("inventory turnover query", ex);
        }
    }

    @Override
    public List<LowStockProduct> getDeadStockProducts() {
        try {
            log.debug("Fetching dead stock products");
            List<LowStockProduct> result = analyticsRepository.getDeadStockProducts();
            log.debug("Found {} dead stock products", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch dead stock products", ex);
            throw new QueryExecutionException("dead stock products query", ex);
        }
    }

    // ==================== Payment Analytics ====================

    @Override
    public List<PaymentMethodStats> getPaymentMethodStats(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching payment method stats: period={} to {}", startDate, endDate);
            List<PaymentMethodStats> result = analyticsRepository.getPaymentMethodStats(startDate, endDate);
            log.debug("Found {} payment method stats", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch payment method stats", ex);
            throw new QueryExecutionException("payment method stats query", ex);
        }
    }

    // ==================== Time Analytics ====================

    @Override
    public List<HourlySales> getSalesByHour(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching sales by hour: period={} to {}", startDate, endDate);
            List<HourlySales> result = analyticsRepository.getSalesByHour(startDate, endDate);
            log.debug("Found {} hourly sales records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch sales by hour", ex);
            throw new QueryExecutionException("sales by hour query", ex);
        }
    }

    @Override
    public List<DailySales> getSalesByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching sales by day of week: period={} to {}", startDate, endDate);
            List<DailySales> result = analyticsRepository.getSalesByDayOfWeek(startDate, endDate);
            log.debug("Found {} day-of-week sales records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch sales by day of week", ex);
            throw new QueryExecutionException("sales by day of week query", ex);
        }
    }

    // ==================== Revenue Analytics ====================

    @Override
    public List<CategoryRevenue> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching revenue by category: period={} to {}", startDate, endDate);
            List<CategoryRevenue> result = analyticsRepository.getRevenueByCategory(startDate, endDate);
            log.debug("Found {} category revenue records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch revenue by category", ex);
            throw new QueryExecutionException("revenue by category query", ex);
        }
    }

    @Override
    public RevenueForecast getRevenueForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays) {
        try {
            log.debug("Fetching revenue forecast: historical={} to {}, forecast days={}", 
                historicalStartDate, historicalEndDate, forecastDays);
            RevenueForecast result = analyticsRepository.getRevenueForecast(historicalStartDate, historicalEndDate, forecastDays);
            log.debug("Revenue forecast generated: predicted={}, confidence={}%", 
                result.getPredictedRevenue(), result.getConfidence());
            return result;
        } catch (Exception ex) {
            log.error("Failed to generate revenue forecast", ex);
            throw new QueryExecutionException("revenue forecast query", ex);
        }
    }

    @Override
    public List<ProfitMargin> getProfitMargins(int limit, LocalDate startDate, LocalDate endDate, boolean sortByProfit) {
        try {
            log.debug("Fetching profit margins: limit={}, period={} to {}, sortByProfit={}", 
                limit, startDate, endDate, sortByProfit);
            List<ProfitMargin> result = analyticsRepository.getProfitMargins(limit, startDate, endDate, sortByProfit);
            log.debug("Retrieved {} profit margin records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch profit margins", ex);
            throw new QueryExecutionException("profit margins query", ex);
        }
    }

    @Override
    public RefundStats getRefundStats(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching refund stats: period={} to {}", startDate, endDate);
            RefundStats result = analyticsRepository.getRefundStats(startDate, endDate);
            log.debug("Refund rate: {}%, Total refunded: {}", 
                result.getRefundRate(), result.getTotalRefundAmount());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch refund stats", ex);
            throw new QueryExecutionException("refund stats query", ex);
        }
    }

    @Override
    public List<GeographicSales> getSalesByLocation(LocalDate startDate, LocalDate endDate, boolean groupByCity) {
        try {
            log.debug("Fetching sales by location: period={} to {}, groupByCity={}", 
                startDate, endDate, groupByCity);
            List<GeographicSales> result = analyticsRepository.getSalesByLocation(startDate, endDate, groupByCity);
            log.debug("Retrieved {} location records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch geographic sales", ex);
            throw new QueryExecutionException("geographic sales query", ex);
        }
    }

    @Override
    public List<ShippingPerformance> getShippingPerformance(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching shipping performance: period={} to {}", startDate, endDate);
            List<ShippingPerformance> result = analyticsRepository.getShippingPerformance(startDate, endDate);
            log.debug("Retrieved {} carrier records", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch shipping performance", ex);
            throw new QueryExecutionException("shipping performance query", ex);
        }
    }

    @Override
    public List<MarketingAttribution> getMarketingAttribution(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching marketing attribution: period={} to {}", startDate, endDate);
            List<MarketingAttribution> result = analyticsRepository.getMarketingAttribution(startDate, endDate);
            log.debug("Retrieved {} marketing channels", result.size());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch marketing attribution", ex);
            throw new QueryExecutionException("marketing attribution query", ex);
        }
    }

    @Override
    public CartAbandonment getCartAbandonmentStats(LocalDate startDate, LocalDate endDate) {
        try {
            log.debug("Fetching cart abandonment stats: period={} to {}", startDate, endDate);
            CartAbandonment result = analyticsRepository.getCartAbandonmentStats(startDate, endDate);
            log.debug("Abandonment rate: {}%, Total abandoned: {}", 
                result.getAbandonmentRate(), result.getAbandonedCarts());
            return result;
        } catch (Exception ex) {
            log.error("Failed to fetch cart abandonment stats", ex);
            throw new QueryExecutionException("cart abandonment stats query", ex);
        }
    }
}
