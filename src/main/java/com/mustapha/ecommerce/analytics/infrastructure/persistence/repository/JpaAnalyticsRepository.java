package com.mustapha.ecommerce.analytics.infrastructure.persistence.repository;

import com.mustapha.ecommerce.analytics.domain.model.*;
import com.mustapha.ecommerce.analytics.domain.repository.AnalyticsRepository;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JPA Implementation of Analytics Repository
 * Uses native SQL and JPQL queries for efficient aggregations
 * 
 * Architectural Note:
 * - This repository reads from Order and OrderItem tables
 * - It's in the analytics bounded context but queries order data
 * - This is acceptable for read-only analytics (CQRS read model pattern)
 */
@Slf4j
@Repository
public class JpaAnalyticsRepository implements AnalyticsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<ProductPerformance> getBestSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching best-selling products: limit={}, period={} to {}", limit, startDate, endDate);
        
        String jpql = """
            SELECT new com.mustapha.ecommerce.analytics.domain.model.ProductPerformance(
                oi.productId,
                oi.productName,
                SUM(oi.quantity),
                SUM(oi.quantity * oi.price),
                COUNT(DISTINCT o.id)
            )
            FROM OrderJpaEntity o
            JOIN o.items oi
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY oi.productId, oi.productName
            ORDER BY SUM(oi.quantity) DESC
            """;

        return entityManager.createQuery(jpql, ProductPerformance.class)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public List<ProductPerformance> getWorstSellingProducts(int limit, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching worst-selling products: limit={}, period={} to {}", limit, startDate, endDate);
        
        String jpql = """
            SELECT new com.mustapha.ecommerce.analytics.domain.model.ProductPerformance(
                oi.productId,
                oi.productName,
                SUM(oi.quantity),
                SUM(oi.quantity * oi.price),
                COUNT(DISTINCT o.id)
            )
            FROM OrderJpaEntity o
            JOIN o.items oi
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY oi.productId, oi.productName
            ORDER BY SUM(oi.quantity) ASC
            """;

        return entityManager.createQuery(jpql, ProductPerformance.class)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public List<DailySales> getDailySales(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching daily sales: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                CAST(o.createdAt AS LocalDate) as date,
                COUNT(o.id) as orderCount,
                SUM(o.totalAmount) as revenue
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY CAST(o.createdAt AS LocalDate)
            ORDER BY CAST(o.createdAt AS LocalDate) DESC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new DailySales(
                (LocalDate) row[0],
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2]
            ))
            .collect(Collectors.toList());
    }

    @Override
    public DailySales getPeakSalesDay(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching peak sales day: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                CAST(o.createdAt AS LocalDate) as date,
                COUNT(o.id) as orderCount,
                SUM(o.totalAmount) as revenue
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY CAST(o.createdAt AS LocalDate)
            ORDER BY SUM(o.totalAmount) DESC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(1)
            .getResultList();

        if (results.isEmpty()) {
            return new DailySales(LocalDate.now(), 0, BigDecimal.ZERO);
        }

        Object[] row = results.get(0);
        return new DailySales(
            (LocalDate) row[0],
            ((Number) row[1]).longValue(),
            (BigDecimal) row[2]
        );
    }

    @Override
    public DailySales getSlowestSalesDay(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching slowest sales day: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                CAST(o.createdAt AS LocalDate) as date,
                COUNT(o.id) as orderCount,
                SUM(o.totalAmount) as revenue
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY CAST(o.createdAt AS LocalDate)
            ORDER BY SUM(o.totalAmount) ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(1)
            .getResultList();

        if (results.isEmpty()) {
            return new DailySales(LocalDate.now(), 0, BigDecimal.ZERO);
        }

        Object[] row = results.get(0);
        return new DailySales(
            (LocalDate) row[0],
            ((Number) row[1]).longValue(),
            (BigDecimal) row[2]
        );
    }

    @Override
    public SalesSummary getSalesSummary(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching sales summary: period={} to {}", startDate, endDate);
        
        // Total orders and revenue (COMPLETED only)
        String deliveredJpql = """
            SELECT COUNT(o.id), COALESCE(SUM(o.totalAmount), 0)
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> deliveredResults = entityManager.createQuery(deliveredJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        long deliveredOrders = ((Number) deliveredResults.get(0)[0]).longValue();
        BigDecimal totalRevenue = (BigDecimal) deliveredResults.get(0)[1];

        // Count by status
        String statusJpql = """
            SELECT o.status, COUNT(o.id)
            FROM OrderJpaEntity o
            WHERE o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY o.status
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> statusResults = entityManager.createQuery(statusJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        long totalOrders = 0;
        long cancelledOrders = 0;
        long pendingOrders = 0;

        for (Object[] row : statusResults) {
            String status = row[0].toString();
            long count = ((Number) row[1]).longValue();
            totalOrders += count;

            if ("CANCELLED".equals(status)) {
                cancelledOrders = count;
            } else if ("PENDING".equals(status)) {
                pendingOrders = count;
            }
        }

        return new SalesSummary(
            totalOrders,
            totalRevenue,
            deliveredOrders,
            cancelledOrders,
            pendingOrders
        );
    }

    @Override
    public List<ProductPerformance> getTopRevenueProducts(int limit, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching top revenue products: limit={}, period={} to {}", limit, startDate, endDate);
        
        String jpql = """
            SELECT new com.mustapha.ecommerce.analytics.domain.model.ProductPerformance(
                oi.productId,
                oi.productName,
                SUM(oi.quantity),
                SUM(oi.quantity * oi.price),
                COUNT(DISTINCT o.id)
            )
            FROM OrderJpaEntity o
            JOIN o.items oi
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY oi.productId, oi.productName
            ORDER BY SUM(oi.quantity * oi.price) DESC
            """;

        return entityManager.createQuery(jpql, ProductPerformance.class)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(limit)
            .getResultList();
    }

    // ==================== Customer Analytics ====================

    @Override
    public List<TopCustomer> getTopCustomers(int limit, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching top customers: limit={}, period={} to {}", limit, startDate, endDate);
        
        String jpql = """
            SELECT new com.mustapha.ecommerce.analytics.domain.model.TopCustomer(
                o.customerId,
                u.username,
                u.email,
                COUNT(o.id),
                SUM(o.totalAmount)
            )
            FROM OrderJpaEntity o
            LEFT JOIN com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity u ON u.id = o.customerId
            WHERE o.status = com.mustapha.ecommerce.order.domain.model.OrderStatus.DELIVERED
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY o.customerId, u.username, u.email
            ORDER BY SUM(o.totalAmount) DESC
            """;

        return entityManager.createQuery(jpql, TopCustomer.class)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(limit)
            .getResultList();
    }

    @Override
    public CustomerRetention getCustomerRetention(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching customer retention: period={} to {}", startDate, endDate);
        
        // Get all unique customers who ordered in the period
        String totalCustomersJpql = """
            SELECT COUNT(DISTINCT o.customerId)
            FROM OrderJpaEntity o
            WHERE o.status = com.mustapha.ecommerce.order.domain.model.OrderStatus.DELIVERED
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            """;
        
        long totalCustomers = ((Number) entityManager.createQuery(totalCustomersJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getSingleResult()).longValue();

        // Get returning customers (ordered before the period and during the period)
        String returningJpql = """
            SELECT COUNT(DISTINCT o.customerId)
            FROM OrderJpaEntity o
            WHERE o.status = com.mustapha.ecommerce.order.domain.model.OrderStatus.DELIVERED
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            AND EXISTS (
                SELECT 1 FROM OrderJpaEntity prev
                WHERE prev.customerId = o.customerId
                AND prev.status = com.mustapha.ecommerce.order.domain.model.OrderStatus.DELIVERED
                AND prev.createdAt < :startDate
            )
            """;
        
        long returningCustomers = ((Number) entityManager.createQuery(returningJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getSingleResult()).longValue();

        long newCustomers = totalCustomers - returningCustomers;

        return new CustomerRetention(totalCustomers, returningCustomers, newCustomers);
    }

    // ==================== Inventory Analytics ====================

    @Override
    public List<LowStockProduct> getLowStockProducts(int threshold) {
        log.debug("Fetching low stock products: threshold={}", threshold);
        
        String jpql = """
            SELECT 
                p.id,
                p.name,
                p.availableStock,
                :threshold,
                COALESCE(
                    (SELECT SUM(oi.quantity)
                     FROM OrderJpaEntity o
                     JOIN o.items oi
                     WHERE oi.productId = p.id
                     AND o.status = 'DELIVERED'), 0L)
            FROM ProductJpaEntity p
            WHERE p.availableStock <= :threshold
            AND p.availableStock > 0
            ORDER BY p.availableStock ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("threshold", threshold)
            .getResultList();

        return results.stream()
            .map(row -> new LowStockProduct(
                (String) row[0],
                (String) row[1],
                (Integer) row[2],
                (Integer) row[3],
                ((Number) row[4]).longValue()
            ))
            .toList();
    }

    @Override
    public List<LowStockProduct> getOutOfStockProducts() {
        log.debug("Fetching out-of-stock products");
        
        String jpql = """
            SELECT 
                p.id,
                p.name,
                p.availableStock,
                0,
                COALESCE(
                    (SELECT SUM(oi.quantity)
                     FROM OrderJpaEntity o
                     JOIN o.items oi
                     WHERE oi.productId = p.id
                     AND o.status = 'DELIVERED'), 0L)
            FROM ProductJpaEntity p
            WHERE p.availableStock = 0
            ORDER BY p.name ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .getResultList();

        return results.stream()
            .map(row -> new LowStockProduct(
                (String) row[0],
                (String) row[1],
                (Integer) row[2],
                (Integer) row[3],
                ((Number) row[4]).longValue()
            ))
            .toList();
    }

    @Override
    public List<InventoryTurnover> getInventoryTurnover(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching inventory turnover: period={} to {}", startDate, endDate);
        
        int periodDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        
        String jpql = """
            SELECT 
                p.id,
                p.name,
                COALESCE(
                    (SELECT SUM(oi.quantity)
                     FROM OrderJpaEntity o
                     JOIN o.items oi
                     WHERE oi.productId = p.id
                     AND o.status = 'DELIVERED'
                     AND o.createdAt >= :startDate
                     AND o.createdAt < :endDate), 0L),
                p.availableStock,
                :periodDays
            FROM ProductJpaEntity p
            ORDER BY p.name ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setParameter("periodDays", periodDays)
            .getResultList();

        return results.stream()
            .map(row -> new InventoryTurnover(
                (String) row[0],
                (String) row[1],
                ((Number) row[2]).longValue(),
                (Integer) row[3],
                (Integer) row[4]
            ))
            .toList();
    }

    @Override
    public List<LowStockProduct> getDeadStockProducts() {
        log.debug("Fetching dead stock products (never sold)");
        
        String jpql = """
            SELECT new com.mustapha.ecommerce.analytics.domain.model.LowStockProduct(
                p.id,
                p.name,
                p.availableStock,
                0,
                0L
            )
            FROM ProductJpaEntity p
            WHERE NOT EXISTS (
                SELECT 1 FROM OrderJpaEntity o
                JOIN o.items oi
                WHERE oi.productId = p.id
                AND o.status = 'DELIVERED'
            )
            ORDER BY p.createdAt ASC
            """;

        return entityManager.createQuery(jpql, LowStockProduct.class)
            .getResultList();
    }

    // ==================== Payment Analytics ====================

    @Override
    public List<PaymentMethodStats> getPaymentMethodStats(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching payment method stats: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                o.paymentMethod,
                COUNT(o.id),
                COALESCE(SUM(o.totalAmount), 0),
                SUM(CASE WHEN o.status = 'DELIVERED' THEN 1 ELSE 0 END),
                SUM(CASE WHEN o.status = 'CANCELLED' THEN 1 ELSE 0 END)
            FROM OrderJpaEntity o
            WHERE o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY o.paymentMethod
            ORDER BY COUNT(o.id) DESC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new PaymentMethodStats(
                (String) row[0],
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2],
                ((Number) row[3]).longValue(),
                ((Number) row[4]).longValue()
            ))
            .collect(Collectors.toList());
    }

    // ==================== Time Analytics ====================

    @Override
    public List<HourlySales> getSalesByHour(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching sales by hour: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                EXTRACT(HOUR FROM o.createdAt),
                COUNT(o.id),
                COALESCE(SUM(o.totalAmount), 0)
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY EXTRACT(HOUR FROM o.createdAt)
            ORDER BY EXTRACT(HOUR FROM o.createdAt) ASC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new HourlySales(
                ((Number) row[0]).intValue(),
                ((Number) row[1]).longValue(),
                (BigDecimal) row[2]
            ))
            .collect(Collectors.toList());
    }

    @Override
    public List<DailySales> getSalesByDayOfWeek(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching sales by day of week: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                FUNCTION('DAYOFWEEK', o.createdAt),
                COUNT(o.id),
                COALESCE(SUM(o.totalAmount), 0)
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY FUNCTION('DAYOFWEEK', o.createdAt)
            ORDER BY FUNCTION('DAYOFWEEK', o.createdAt) ASC
            """;

        LocalDate monday = LocalDate.of(2026, 1, 5);  // A Monday for reference (Jan 5, 2026 is a Monday)
        
        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> {
                int dayOfWeek = ((Number) row[0]).intValue();
                // MySQL DAYOFWEEK: 1=Sunday, 2=Monday, ..., 7=Saturday
                // Map to actual dates starting from Monday
                LocalDate date = monday.plusDays((dayOfWeek == 1 ? 6 : dayOfWeek - 2));
                return new DailySales(
                    date,
                    ((Number) row[1]).longValue(),
                    (BigDecimal) row[2]
                );
            })
            .collect(Collectors.toList());
    }

    // ==================== Revenue Analytics ====================

    @Override
    public List<CategoryRevenue> getRevenueByCategory(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching revenue by category: period={} to {}", startDate, endDate);
        
        // Note: ProductJpaEntity doesn't have a category field, so we group all products as "General"
        String jpql = """
            SELECT 
                'General',
                COUNT(DISTINCT p.id),
                COALESCE(SUM(oi.quantity), 0),
                COALESCE(SUM(oi.quantity * oi.price), 0)
            FROM OrderJpaEntity o
            JOIN o.items oi
            JOIN ProductJpaEntity p ON p.id = oi.productId
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        // Return empty list if no data found
        if (results.isEmpty() || ((Number) results.get(0)[1]).longValue() == 0) {
            return List.of();
        }

        return results.stream()
            .map(row -> new CategoryRevenue(
                (String) row[0],
                ((Number) row[1]).longValue(),
                ((Number) row[2]).longValue(),
                (BigDecimal) row[3]
            ))
            .collect(Collectors.toList());
    }

    // ==================== Revenue Forecasting ====================

    @Override
    public RevenueForecast getRevenueForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays) {
        log.debug("Generating revenue forecast: historical period={} to {}, forecast days={}", 
            historicalStartDate, historicalEndDate, forecastDays);
        
        // Step 1: Get historical daily sales data
        List<DailySales> historicalData = getDailySales(historicalStartDate, historicalEndDate);
        
        if (historicalData.isEmpty()) {
            log.warn("No historical data available for forecasting");
            return createEmptyForecast(historicalStartDate, historicalEndDate, forecastDays);
        }
        
        int historicalDays = historicalData.size();
        log.debug("Analyzing {} days of historical data", historicalDays);
        
        // Step 2: Calculate statistical metrics
        BigDecimal totalRevenue = historicalData.stream()
            .map(DailySales::getRevenue)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal averageDailyRevenue = totalRevenue.divide(
            BigDecimal.valueOf(historicalDays), 
            2, 
            RoundingMode.HALF_UP
        );
        
        log.debug("Average daily revenue: {}", averageDailyRevenue);
        
        // Step 3: Calculate linear regression for trend
        double[] dailyRevenues = historicalData.stream()
            .mapToDouble(ds -> ds.getRevenue().doubleValue())
            .toArray();
        
        LinearRegressionResult regression = calculateLinearRegression(dailyRevenues);
        BigDecimal dailyGrowthRate = BigDecimal.valueOf(regression.slope / averageDailyRevenue.doubleValue() * 100)
            .setScale(2, RoundingMode.HALF_UP);
        
        log.debug("Daily growth rate: {}%", dailyGrowthRate);
        
        // Step 4: Calculate variance for confidence interval
        double variance = calculateVariance(dailyRevenues, averageDailyRevenue.doubleValue());
        double stdDev = Math.sqrt(variance);
        BigDecimal confidence = RevenueForecast.calculateConfidence(
            BigDecimal.valueOf(variance), 
            averageDailyRevenue
        );
        
        log.debug("Confidence level: {}%", confidence);
        
        // Step 5: Project future revenue
        LocalDate forecastStartDate = historicalEndDate.plusDays(1);
        LocalDate forecastEndDate = forecastStartDate.plusDays(forecastDays - 1);
        
        BigDecimal predictedRevenue = BigDecimal.ZERO;
        List<RevenueForecast.DailyForecast> dailyForecasts = new ArrayList<>();
        
        for (int day = 0; day < forecastDays; day++) {
            // Use linear regression: revenue = intercept + slope * dayNumber
            double daysSinceStart = historicalDays + day;
            double predicted = regression.intercept + (regression.slope * daysSinceStart);
            
            // Apply confidence interval (±2 standard deviations = ~95% confidence)
            double lowerBound = predicted - (2 * stdDev);
            double upperBound = predicted + (2 * stdDev);
            
            // Ensure non-negative values
            predicted = Math.max(0, predicted);
            lowerBound = Math.max(0, lowerBound);
            upperBound = Math.max(0, upperBound);
            
            BigDecimal dailyPrediction = BigDecimal.valueOf(predicted).setScale(2, RoundingMode.HALF_UP);
            predictedRevenue = predictedRevenue.add(dailyPrediction);
            
            dailyForecasts.add(new RevenueForecast.DailyForecast(
                forecastStartDate.plusDays(day),
                dailyPrediction,
                BigDecimal.valueOf(lowerBound).setScale(2, RoundingMode.HALF_UP),
                BigDecimal.valueOf(upperBound).setScale(2, RoundingMode.HALF_UP)
            ));
        }
        
        // Step 6: Calculate overall bounds
        BigDecimal lowerBoundTotal = dailyForecasts.stream()
            .map(RevenueForecast.DailyForecast::getLowerBound)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal upperBoundTotal = dailyForecasts.stream()
            .map(RevenueForecast.DailyForecast::getUpperBound)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        String trend = RevenueForecast.determineTrend(dailyGrowthRate);
        
        log.debug("Forecast complete: predicted={}, trend={}, confidence={}%", 
            predictedRevenue, trend, confidence);
        
        return new RevenueForecast(
            forecastStartDate,
            forecastEndDate,
            forecastDays,
            predictedRevenue,
            lowerBoundTotal,
            upperBoundTotal,
            averageDailyRevenue,
            dailyGrowthRate,
            trend,
            confidence,
            historicalDays,
            dailyForecasts
        );
    }
    
    /**
     * Calculate linear regression (least squares method)
     * Returns slope and intercept for trend line
     */
    private LinearRegressionResult calculateLinearRegression(double[] values) {
        int n = values.length;
        
        // Calculate means
        double sumX = 0, sumY = 0, sumXY = 0, sumX2 = 0;
        for (int i = 0; i < n; i++) {
            sumX += i;
            sumY += values[i];
            sumXY += i * values[i];
            sumX2 += i * i;
        }
        
        double meanX = sumX / n;
        double meanY = sumY / n;
        
        // Calculate slope and intercept
        double slope = (sumXY - n * meanX * meanY) / (sumX2 - n * meanX * meanX);
        double intercept = meanY - slope * meanX;
        
        return new LinearRegressionResult(slope, intercept);
    }
    
    /**
     * Calculate variance of revenue data
     */
    private double calculateVariance(double[] values, double mean) {
        double sumSquaredDiff = 0;
        for (double value : values) {
            double diff = value - mean;
            sumSquaredDiff += diff * diff;
        }
        return sumSquaredDiff / values.length;
    }
    
    /**
     * Create empty forecast when no historical data exists
     */
    private RevenueForecast createEmptyForecast(LocalDate historicalStartDate, LocalDate historicalEndDate, int forecastDays) {
        LocalDate forecastStartDate = historicalEndDate.plusDays(1);
        LocalDate forecastEndDate = forecastStartDate.plusDays(forecastDays - 1);
        
        return new RevenueForecast(
            forecastStartDate,
            forecastEndDate,
            forecastDays,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            "STABLE",
            BigDecimal.ZERO,
            0,
            List.of()
        );
    }
    
    /**
     * Linear regression result holder
     */
    private static class LinearRegressionResult {
        final double slope;
        final double intercept;
        
        LinearRegressionResult(double slope, double intercept) {
            this.slope = slope;
            this.intercept = intercept;
        }
    }

    // ==================== Profit Analytics ====================

    @Override
    public List<ProfitMargin> getProfitMargins(int limit, LocalDate startDate, LocalDate endDate, boolean sortByProfit) {
        log.debug("Fetching profit margins: limit={}, period={} to {}, sortByProfit={}", 
            limit, startDate, endDate, sortByProfit);
        
        String jpql = """
            SELECT 
                p.id,
                p.name,
                SUM(oi.quantity),
                SUM(oi.quantity * oi.price),
                SUM(oi.quantity * COALESCE(p.costOfGoods, 0))
            FROM OrderJpaEntity o
            JOIN o.items oi
            JOIN ProductJpaEntity p ON p.id = oi.productId
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY p.id, p.name
            ORDER BY """ + (sortByProfit 
                ? " SUM(oi.quantity * oi.price) - SUM(oi.quantity * COALESCE(p.costOfGoods, 0)) DESC" 
                : " (SUM(oi.quantity * oi.price) - SUM(oi.quantity * COALESCE(p.costOfGoods, 0))) / NULLIF(SUM(oi.quantity * oi.price), 0) DESC");

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .setMaxResults(limit)
            .getResultList();

        return results.stream()
            .map(row -> new ProfitMargin(
                (String) row[0],           // productId
                (String) row[1],           // productName
                ((Number) row[2]).longValue(),  // unitsSold
                (BigDecimal) row[3],       // revenue
                (BigDecimal) row[4]        // cost
            ))
            .collect(Collectors.toList());
    }

    // ==================== Refund Analytics ====================

    @Override
    public RefundStats getRefundStats(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching refund stats: period={} to {}", startDate, endDate);
        
        // Get total completed orders (baseline for refund rate)
        String deliveredJpql = """
            SELECT COUNT(o.id)
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            """;
        
        long totaldeliveredOrders = ((Number) entityManager.createQuery(deliveredJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getSingleResult()).longValue();

        // Get refund statistics grouped by status
        String refundJpql = """
            SELECT 
                o.refundStatus,
                COUNT(o.id),
                COALESCE(SUM(o.refundAmount), 0)
            FROM OrderJpaEntity o
            WHERE o.createdAt >= :startDate
            AND o.createdAt < :endDate
            AND o.refundStatus != 'NONE'
            GROUP BY o.refundStatus
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(refundJpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        long refundRequestedCount = 0;
        long refundApprovedCount = 0;
        long refundDeliveredCount = 0;
        long refundRejectedCount = 0;
        BigDecimal totalRefundAmount = BigDecimal.ZERO;

        for (Object[] row : results) {
            String status = row[0].toString();
            long count = ((Number) row[1]).longValue();
            BigDecimal amount = (BigDecimal) row[2];

            switch (status) {
                case "REQUESTED" -> refundRequestedCount = count;
                case "APPROVED" -> refundApprovedCount = count;
                case "COMPLETED" -> {
                    refundDeliveredCount = count;
                    totalRefundAmount = amount;
                }
                case "REJECTED" -> refundRejectedCount = count;
            }
        }

        return new RefundStats(
            totaldeliveredOrders,
            refundRequestedCount,
            refundApprovedCount,
            refundDeliveredCount,
            refundRejectedCount,
            totalRefundAmount
        );
    }

    // ==================== Geographic Analytics ====================

    @Override
    public List<GeographicSales> getSalesByLocation(LocalDate startDate, LocalDate endDate, boolean groupByCity) {
        log.debug("Fetching sales by location: period={} to {}, groupByCity={}", 
            startDate, endDate, groupByCity);
        
        String jpql;
        if (groupByCity) {
            jpql = """
                SELECT 
                    o.shippingCity,
                    o.shippingState,
                    o.shippingCountry,
                    COUNT(o.id),
                    SUM(o.totalAmount)
                FROM OrderJpaEntity o
                WHERE o.status = 'DELIVERED'
                AND o.createdAt >= :startDate
                AND o.createdAt < :endDate
                AND o.shippingCity IS NOT NULL
                GROUP BY o.shippingCity, o.shippingState, o.shippingCountry
                ORDER BY SUM(o.totalAmount) DESC
                """;
        } else {
            jpql = """
                SELECT 
                    NULL,
                    o.shippingState,
                    o.shippingCountry,
                    COUNT(o.id),
                    SUM(o.totalAmount)
                FROM OrderJpaEntity o
                WHERE o.status = 'DELIVERED'
                AND o.createdAt >= :startDate
                AND o.createdAt < :endDate
                AND o.shippingState IS NOT NULL
                GROUP BY o.shippingState, o.shippingCountry
                ORDER BY SUM(o.totalAmount) DESC
                """;
        }

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new GeographicSales(
                (String) row[0],           // city
                (String) row[1],           // state
                (String) row[2],           // country
                ((Number) row[3]).longValue(),  // orderCount
                (BigDecimal) row[4]        // totalRevenue
            ))
            .collect(Collectors.toList());
    }

    // ==================== Shipping Analytics ====================

    @Override
    public List<ShippingPerformance> getShippingPerformance(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching shipping performance: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                o.carrier,
                COUNT(o.id),
                SUM(CASE WHEN o.deliveredAt IS NOT NULL THEN 1 ELSE 0 END),
                AVG(CAST(CASE 
                    WHEN o.shippedAt IS NOT NULL 
                    THEN FUNCTION('TIMESTAMPDIFF', HOUR, o.createdAt, o.shippedAt)
                    ELSE 0 
                END AS DOUBLE)),
                AVG(CAST(CASE 
                    WHEN o.deliveredAt IS NOT NULL AND o.shippedAt IS NOT NULL
                    THEN FUNCTION('TIMESTAMPDIFF', HOUR, o.shippedAt, o.deliveredAt)
                    ELSE 0 
                END AS DOUBLE))
            FROM OrderJpaEntity o
            WHERE o.status IN ('SHIPPED', 'DELIVERED')
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            AND o.carrier IS NOT NULL
            GROUP BY o.carrier
            ORDER BY COUNT(o.id) DESC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new ShippingPerformance(
                (String) row[0],                                    // carrier
                ((Number) row[1]).longValue(),                      // totalShipments
                ((Number) row[2]).longValue(),                      // deliveredCount
                row[3] != null ? ((Number) row[3]).doubleValue() : 0.0,  // avgTimeToShip
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0   // avgDeliveryTime
            ))
            .collect(Collectors.toList());
    }

    // ==================== Marketing Analytics ====================

    @Override
    public List<MarketingAttribution> getMarketingAttribution(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching marketing attribution: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                COALESCE(o.utmSource, 'direct'),
                COALESCE(o.utmCampaign, 'none'),
                COUNT(o.id),
                SUM(o.totalAmount),
                COUNT(DISTINCT o.customerId),
                COUNT(o.id)
            FROM OrderJpaEntity o
            WHERE o.status = 'DELIVERED'
            AND o.createdAt >= :startDate
            AND o.createdAt < :endDate
            GROUP BY o.utmSource, o.utmCampaign
            ORDER BY SUM(o.totalAmount) DESC
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        return results.stream()
            .map(row -> new MarketingAttribution(
                (String) row[0],                    // source
                (String) row[1],                    // campaign
                ((Number) row[2]).longValue(),      // orderCount
                (BigDecimal) row[3],                // totalRevenue
                ((Number) row[4]).longValue(),      // customerCount
                ((Number) row[5]).longValue()       // totalSessions (using orderCount as proxy)
            ))
            .collect(Collectors.toList());
    }

    // ==================== Cart Abandonment Analytics ====================

    @Override
    public CartAbandonment getCartAbandonmentStats(LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching cart abandonment stats: period={} to {}", startDate, endDate);
        
        String jpql = """
            SELECT 
                c.status,
                COUNT(c.id),
                COALESCE(SUM(CASE WHEN c.status = 'ABANDONED' THEN c.totalAmount ELSE 0 END), 0)
            FROM Cart c
            WHERE c.createdAt >= :startDate
            AND c.createdAt < :endDate
            GROUP BY c.status
            """;

        @SuppressWarnings("unchecked")
        List<Object[]> results = entityManager.createQuery(jpql)
            .setParameter("startDate", startDate.atStartOfDay())
            .setParameter("endDate", endDate.plusDays(1).atStartOfDay())
            .getResultList();

        long totalCarts = 0;
        long activeCarts = 0;
        long convertedCarts = 0;
        long abandonedCarts = 0;
        BigDecimal totalAbandonedValue = BigDecimal.ZERO;

        for (Object[] row : results) {
            String status = row[0].toString();
            long count = ((Number) row[1]).longValue();
            BigDecimal abandonedValue = (BigDecimal) row[2];

            totalCarts += count;
            
            switch (status) {
                case "ACTIVE" -> activeCarts = count;
                case "CONVERTED" -> convertedCarts = count;
                case "ABANDONED" -> {
                    abandonedCarts = count;
                    totalAbandonedValue = abandonedValue;
                }
            }
        }

        return new CartAbandonment(
            totalCarts,
            activeCarts,
            convertedCarts,
            abandonedCarts,
            totalAbandonedValue
        );
    }
}
