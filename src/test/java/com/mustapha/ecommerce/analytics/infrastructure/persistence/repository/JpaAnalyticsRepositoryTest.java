package com.mustapha.ecommerce.analytics.infrastructure.persistence.repository;

import com.mustapha.ecommerce.analytics.domain.model.*;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

/**
 * JpaAnalyticsRepository Integration Tests
 * Tests complex JPQL queries with real database
 * 
 * Pattern: DataJpaTest for repository layer testing
 * Uses in-memory H2 database for fast, isolated tests
 */
@DataJpaTest
@Import(JpaAnalyticsRepository.class)
@ActiveProfiles("test")
@DisplayName("JpaAnalyticsRepository Integration Tests")
class JpaAnalyticsRepositoryTest {

    @Autowired
    private JpaAnalyticsRepository analyticsRepository;

    @Autowired
    private TestEntityManager entityManager;

    private LocalDate startDate;
    private LocalDate endDate;
    private ProductJpaEntity product1;
    private ProductJpaEntity product2;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        
        // Create test products
        product1 = createProduct("PROD-001", "Laptop", 10, new BigDecimal("1000.00"), new BigDecimal("600.00"));
        product2 = createProduct("PROD-002", "Mouse", 50, new BigDecimal("25.00"), new BigDecimal("10.00"));
        
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    @DisplayName("getBestSellingProducts - Should return products ordered by quantity sold DESC")
    void testGetBestSellingProducts() {
        // Given: Orders with different quantities
        OrderJpaEntity order1 = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        addOrderItem(order1, product1.getId(), "Laptop", 5, new BigDecimal("1000.00"));
        addOrderItem(order1, product2.getId(), "Mouse", 20, new BigDecimal("25.00"));
        entityManager.persist(order1);

        OrderJpaEntity order2 = createCompletedOrder("ORD-002", startDate.plusDays(10).atTime(14, 30));
        addOrderItem(order2, product1.getId(), "Laptop", 3, new BigDecimal("1000.00"));
        addOrderItem(order2, product2.getId(), "Mouse", 15, new BigDecimal("25.00"));
        entityManager.persist(order2);

        entityManager.flush();
        entityManager.clear();

        // When: Get best selling products
        List<ProductPerformance> result = analyticsRepository.getBestSellingProducts(10, startDate, endDate);

        // Then: Mouse should be #1 (35 units), Laptop #2 (8 units)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProductName()).isEqualTo("Mouse");
        assertThat(result.get(0).getUnitsSold()).isEqualTo(35);
        assertThat(result.get(1).getProductName()).isEqualTo("Laptop");
        assertThat(result.get(1).getUnitsSold()).isEqualTo(8);
    }

    @Test
    @DisplayName("getDailySales - Should aggregate sales by date")
    void testGetDailySales() {
        // Given: Orders on different dates
        OrderJpaEntity order1 = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        order1.setTotalAmount(new BigDecimal("1500.00"));
        entityManager.persist(order1);

        OrderJpaEntity order2 = createCompletedOrder("ORD-002", startDate.plusDays(5).atTime(16, 30));
        order2.setTotalAmount(new BigDecimal("2500.00"));
        entityManager.persist(order2);

        OrderJpaEntity order3 = createCompletedOrder("ORD-003", startDate.plusDays(10).atTime(12, 0));
        order3.setTotalAmount(new BigDecimal("3000.00"));
        entityManager.persist(order3);

        entityManager.flush();
        entityManager.clear();

        // When: Get daily sales
        List<DailySales> result = analyticsRepository.getDailySales(startDate, endDate);

        // Then: Should have 2 days with correct aggregations
        assertThat(result).hasSize(2);
        
        DailySales day1 = result.stream()
            .filter(ds -> ds.getDate().equals(startDate.plusDays(5)))
            .findFirst()
            .orElseThrow();
        assertThat(day1.getOrderCount()).isEqualTo(2);
        assertThat(day1.getRevenue()).isEqualByComparingTo(new BigDecimal("4000.00"));
        
        DailySales day2 = result.stream()
            .filter(ds -> ds.getDate().equals(startDate.plusDays(10)))
            .findFirst()
            .orElseThrow();
        assertThat(day2.getOrderCount()).isEqualTo(1);
        assertThat(day2.getRevenue()).isEqualByComparingTo(new BigDecimal("3000.00"));
    }

    @Test
    @DisplayName("getPeakSalesDay - Should return day with maximum revenue")
    void testGetPeakSalesDay() {
        // Given: Orders with different revenue amounts
        OrderJpaEntity order1 = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        order1.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persist(order1);

        OrderJpaEntity order2 = createCompletedOrder("ORD-002", startDate.plusDays(10).atTime(14, 0));
        order2.setTotalAmount(new BigDecimal("5000.00")); // Peak day
        entityManager.persist(order2);

        OrderJpaEntity order3 = createCompletedOrder("ORD-003", startDate.plusDays(15).atTime(12, 0));
        order3.setTotalAmount(new BigDecimal("2000.00"));
        entityManager.persist(order3);

        entityManager.flush();
        entityManager.clear();

        // When: Get peak sales day
        DailySales peak = analyticsRepository.getPeakSalesDay(startDate, endDate);

        // Then: Should return day with $5000 revenue
        assertThat(peak.getDate()).isEqualTo(startDate.plusDays(10));
        assertThat(peak.getRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(peak.getOrderCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("getSalesSummary - Should aggregate order status counts and revenue")
    void testGetSalesSummary() {
        // Given: Orders with different statuses
        OrderJpaEntity completed1 = createOrderWithStatus("ORD-001", "DELIVERED", startDate.plusDays(5).atTime(10, 0));
        completed1.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persist(completed1);

        OrderJpaEntity completed2 = createOrderWithStatus("ORD-002", "DELIVERED", startDate.plusDays(10).atTime(14, 0));
        completed2.setTotalAmount(new BigDecimal("2000.00"));
        entityManager.persist(completed2);

        OrderJpaEntity cancelled = createOrderWithStatus("ORD-003", "CANCELLED", startDate.plusDays(15).atTime(12, 0));
        cancelled.setTotalAmount(new BigDecimal("500.00"));
        entityManager.persist(cancelled);

        OrderJpaEntity pending = createOrderWithStatus("ORD-004", "PENDING", startDate.plusDays(20).atTime(9, 0));
        pending.setTotalAmount(new BigDecimal("750.00"));
        entityManager.persist(pending);

        entityManager.flush();
        entityManager.clear();

        // When: Get sales summary
        SalesSummary summary = analyticsRepository.getSalesSummary(startDate, endDate);

        // Then: Should have correct counts and totals
        assertThat(summary.getTotalOrders()).isEqualTo(4);
        assertThat(summary.getCompletedOrders()).isEqualTo(2);
        assertThat(summary.getCancelledOrders()).isEqualTo(1);
        assertThat(summary.getPendingOrders()).isEqualTo(1);
        assertThat(summary.getTotalRevenue()).isEqualByComparingTo(new BigDecimal("3000.00")); // Only completed
    }

    @Test
    @DisplayName("getTopCustomers - Should return customers by total spent DESC")
    void testGetTopCustomers() {
        // Given: Create users first
        createUser("1", "alice_johnson", "alice@example.com");
        createUser("2", "bob_smith", "bob@example.com");
        
        // Given: Orders from different customers
        OrderJpaEntity order1 = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        order1.setCustomerId("1");
        order1.setTotalAmount(new BigDecimal("5000.00"));
        entityManager.persist(order1);

        OrderJpaEntity order2 = createCompletedOrder("ORD-002", startDate.plusDays(10).atTime(14, 0));
        order2.setCustomerId("1");
        order2.setTotalAmount(new BigDecimal("3000.00"));
        entityManager.persist(order2);

        OrderJpaEntity order3 = createCompletedOrder("ORD-003", startDate.plusDays(15).atTime(12, 0));
        order3.setCustomerId("2");
        order3.setTotalAmount(new BigDecimal("4000.00"));
        entityManager.persist(order3);

        entityManager.flush();
        entityManager.clear();

        // When: Get top customers
        List<TopCustomer> result = analyticsRepository.getTopCustomers(10, startDate, endDate);

        // Then: Alice should be #1 ($8000), Bob #2 ($4000)
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getCustomerName()).isEqualTo("alice_johnson");
        assertThat(result.get(0).getTotalOrders()).isEqualTo(2);
        assertThat(result.get(0).getTotalSpent()).isEqualByComparingTo(new BigDecimal("8000.00"));
        
        assertThat(result.get(1).getCustomerName()).isEqualTo("bob_smith");
        assertThat(result.get(1).getTotalOrders()).isEqualTo(1);
        assertThat(result.get(1).getTotalSpent()).isEqualByComparingTo(new BigDecimal("4000.00"));
    }

    @Test
    @DisplayName("getCustomerRetention - Should calculate returning vs new customers")
    void testGetCustomerRetention() {
        // Given: Orders before and during analysis period
        LocalDate beforePeriod = startDate.minusDays(30);
        
        // Customer 1: Ordered before AND during (returning)
        OrderJpaEntity oldOrder1 = createCompletedOrder("ORD-OLD-001", beforePeriod.atTime(10, 0));
        oldOrder1.setCustomerId("1");
        entityManager.persist(oldOrder1);
        
        OrderJpaEntity newOrder1 = createCompletedOrder("ORD-NEW-001", startDate.plusDays(5).atTime(10, 0));
        newOrder1.setCustomerId("1");
        entityManager.persist(newOrder1);

        // Customer 2: Ordered before AND during (returning)
        OrderJpaEntity oldOrder2 = createCompletedOrder("ORD-OLD-002", beforePeriod.atTime(14, 0));
        oldOrder2.setCustomerId("2");
        entityManager.persist(oldOrder2);
        
        OrderJpaEntity newOrder2 = createCompletedOrder("ORD-NEW-002", startDate.plusDays(10).atTime(14, 0));
        newOrder2.setCustomerId("2");
        entityManager.persist(newOrder2);

        // Customer 3: Only ordered during (new)
        OrderJpaEntity newOrder3 = createCompletedOrder("ORD-NEW-003", startDate.plusDays(15).atTime(12, 0));
        newOrder3.setCustomerId("3");
        entityManager.persist(newOrder3);

        entityManager.flush();
        entityManager.clear();

        // When: Get customer retention
        CustomerRetention retention = analyticsRepository.getCustomerRetention(startDate, endDate);

        // Then: Should have 3 total (2 returning, 1 new)
        assertThat(retention.getTotalCustomers()).isEqualTo(3);
        assertThat(retention.getReturningCustomers()).isEqualTo(2);
        assertThat(retention.getNewCustomers()).isEqualTo(1);
    }

    @Test
    @DisplayName("getLowStockProducts - Should return products below threshold with sales data")
    void testGetLowStockProducts() {
        // Given: Products with low stock
        product1.setTotalStock(5); // Below threshold
        product1.setAvailableStock(5);
        product2.setTotalStock(3); // Below threshold
        product2.setAvailableStock(3);
        entityManager.merge(product1);
        entityManager.merge(product2);

        // Add order history
        OrderJpaEntity order = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        addOrderItem(order, product1.getId(), "Laptop", 10, new BigDecimal("1000.00"));
        addOrderItem(order, product2.getId(), "Mouse", 25, new BigDecimal("25.00"));
        entityManager.persist(order);

        entityManager.flush();
        entityManager.clear();

        // When: Get low stock products (threshold = 10)
        List<LowStockProduct> result = analyticsRepository.getLowStockProducts(10);

        // Then: Should return both products with calculated total sold
        assertThat(result).hasSize(2);
        
        LowStockProduct laptop = result.stream()
            .filter(p -> p.getProductName().equals("Laptop"))
            .findFirst()
            .orElseThrow();
        assertThat(laptop.getCurrentStock()).isEqualTo(5);
        assertThat(laptop.getTotalSold()).isEqualTo(10);
        
        LowStockProduct mouse = result.stream()
            .filter(p -> p.getProductName().equals("Mouse"))
            .findFirst()
            .orElseThrow();
        assertThat(mouse.getCurrentStock()).isEqualTo(3);
        assertThat(mouse.getTotalSold()).isEqualTo(25);
    }

    @Test
    @DisplayName("getDeadStockProducts - Should return products never sold")
    void testGetDeadStockProducts() {
        // Given: One product with sales, one without
        OrderJpaEntity order = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        addOrderItem(order, product1.getId(), "Laptop", 5, new BigDecimal("1000.00"));
        entityManager.persist(order);
        
        // product2 has no orders
        entityManager.flush();
        entityManager.clear();

        // When: Get dead stock products
        List<LowStockProduct> result = analyticsRepository.getDeadStockProducts();

        // Then: Only Mouse should be returned (never sold)
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getProductName()).isEqualTo("Mouse");
        assertThat(result.get(0).getTotalSold()).isEqualTo(0);
    }

    @Test
    @DisplayName("getPaymentMethodStats - Should aggregate by payment method")
    void testGetPaymentMethodStats() {
        // Given: Orders with different payment methods
        OrderJpaEntity creditCard1 = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        creditCard1.setPaymentMethod("CREDIT_CARD");
        creditCard1.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persist(creditCard1);

        OrderJpaEntity creditCard2 = createCompletedOrder("ORD-002", startDate.plusDays(10).atTime(14, 0));
        creditCard2.setPaymentMethod("CREDIT_CARD");
        creditCard2.setTotalAmount(new BigDecimal("2000.00"));
        entityManager.persist(creditCard2);

        OrderJpaEntity paypal = createCompletedOrder("ORD-003", startDate.plusDays(15).atTime(12, 0));
        paypal.setPaymentMethod("PAYPAL");
        paypal.setTotalAmount(new BigDecimal("1500.00"));
        entityManager.persist(paypal);

        OrderJpaEntity cancelled = createOrderWithStatus("ORD-004", "CANCELLED", startDate.plusDays(20).atTime(9, 0));
        cancelled.setPaymentMethod("CREDIT_CARD");
        cancelled.setTotalAmount(new BigDecimal("500.00"));
        entityManager.persist(cancelled);

        entityManager.flush();
        entityManager.clear();

        // When: Get payment method stats
        List<PaymentMethodStats> result = analyticsRepository.getPaymentMethodStats(startDate, endDate);

        // Then: Should have 2 payment methods with correct counts
        assertThat(result).hasSize(2);
        
        PaymentMethodStats creditCardStats = result.stream()
            .filter(pm -> pm.getPaymentMethod().equals("CREDIT_CARD"))
            .findFirst()
            .orElseThrow();
        assertThat(creditCardStats.getTransactionCount()).isEqualTo(3); // 2 completed + 1 cancelled
        assertThat(creditCardStats.getSuccessfulCount()).isEqualTo(2);
        assertThat(creditCardStats.getFailedCount()).isEqualTo(1);
        assertThat(creditCardStats.getTotalAmount()).isEqualByComparingTo(new BigDecimal("3500.00"));
    }

    @Test
    @DisplayName("getSalesByHour - Should aggregate sales by hour of day")
    void testGetSalesByHour() {
        // Given: Orders at different hours
        OrderJpaEntity morning = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(9, 30));
        morning.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persist(morning);

        OrderJpaEntity afternoon1 = createCompletedOrder("ORD-002", startDate.plusDays(5).atTime(14, 15));
        afternoon1.setTotalAmount(new BigDecimal("2000.00"));
        entityManager.persist(afternoon1);

        OrderJpaEntity afternoon2 = createCompletedOrder("ORD-003", startDate.plusDays(10).atTime(14, 45));
        afternoon2.setTotalAmount(new BigDecimal("1500.00"));
        entityManager.persist(afternoon2);

        entityManager.flush();
        entityManager.clear();

        // When: Get sales by hour
        List<HourlySales> result = analyticsRepository.getSalesByHour(startDate, endDate);

        // Then: Should have 2 hours with correct aggregations
        assertThat(result).hasSize(2);
        
        HourlySales hour9 = result.stream()
            .filter(h -> h.getHour() == 9)
            .findFirst()
            .orElseThrow();
        assertThat(hour9.getOrderCount()).isEqualTo(1);
        assertThat(hour9.getRevenue()).isEqualByComparingTo(new BigDecimal("1000.00"));
        
        HourlySales hour14 = result.stream()
            .filter(h -> h.getHour() == 14)
            .findFirst()
            .orElseThrow();
        assertThat(hour14.getOrderCount()).isEqualTo(2);
        assertThat(hour14.getRevenue()).isEqualByComparingTo(new BigDecimal("3500.00"));
    }

    @Test
    @DisplayName("getProfitMargins - Should calculate revenue minus cost")
    void testGetProfitMargins() {
        // Given: Orders with products that have costs
        OrderJpaEntity order = createCompletedOrder("ORD-001", startDate.plusDays(5).atTime(10, 0));
        addOrderItem(order, product1.getId(), "Laptop", 5, new BigDecimal("1000.00"));
        addOrderItem(order, product2.getId(), "Mouse", 20, new BigDecimal("25.00"));
        entityManager.persist(order);

        entityManager.flush();
        entityManager.clear();

        // When: Get profit margins sorted by profit
        List<ProfitMargin> result = analyticsRepository.getProfitMargins(10, startDate, endDate, true);

        // Then: Should calculate profit correctly
        assertThat(result).hasSize(2);
        
        // Laptop: revenue = 5 * 1000 = 5000, cost = 5 * 600 = 3000, profit = 2000
        ProfitMargin laptop = result.stream()
            .filter(p -> p.getProductName().equals("Laptop"))
            .findFirst()
            .orElseThrow();
        assertThat(laptop.getRevenue()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(laptop.getCost()).isEqualByComparingTo(new BigDecimal("3000.00"));
        assertThat(laptop.getProfit()).isEqualByComparingTo(new BigDecimal("2000.00"));
        
        // Mouse: revenue = 20 * 25 = 500, cost = 20 * 10 = 200, profit = 300
        ProfitMargin mouse = result.stream()
            .filter(p -> p.getProductName().equals("Mouse"))
            .findFirst()
            .orElseThrow();
        assertThat(mouse.getRevenue()).isEqualByComparingTo(new BigDecimal("500.00"));
        assertThat(mouse.getCost()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(mouse.getProfit()).isEqualByComparingTo(new BigDecimal("300.00"));
    }

    @Test
    @DisplayName("getCartAbandonmentStats - Should aggregate cart statuses")
    void testGetCartAbandonmentStats() {
        // Given: Carts with different statuses
        createCart("CART-001", "ACTIVE", startDate.plusDays(5).atTime(10, 0), new BigDecimal("100.00"));
        createCart("CART-002", "ACTIVE", startDate.plusDays(6).atTime(11, 0), new BigDecimal("200.00"));
        createCart("CART-003", "CONVERTED", startDate.plusDays(7).atTime(12, 0), new BigDecimal("300.00"));
        createCart("CART-004", "CONVERTED", startDate.plusDays(8).atTime(13, 0), new BigDecimal("400.00"));
        createCart("CART-005", "CONVERTED", startDate.plusDays(9).atTime(14, 0), new BigDecimal("500.00"));
        createCart("CART-006", "ABANDONED", startDate.plusDays(10).atTime(15, 0), new BigDecimal("250.00"));
        createCart("CART-007", "ABANDONED", startDate.plusDays(11).atTime(16, 0), new BigDecimal("350.00"));

        entityManager.flush();
        entityManager.clear();

        // When: Get cart abandonment stats
        CartAbandonment stats = analyticsRepository.getCartAbandonmentStats(startDate, endDate);

        // Then: Should have correct counts and calculations
        assertThat(stats.getTotalCarts()).isEqualTo(7);
        assertThat(stats.getActiveCarts()).isEqualTo(2);
        assertThat(stats.getConvertedCarts()).isEqualTo(3);
        assertThat(stats.getAbandonedCarts()).isEqualTo(2);
        assertThat(stats.getTotalAbandonedValue()).isEqualByComparingTo(new BigDecimal("600.00"));
        assertThat(stats.getAbandonmentRate()).isEqualTo((2.0 / 7.0) * 100, within(0.01));
        assertThat(stats.getConversionRate()).isEqualTo((3.0 / 7.0) * 100, within(0.01));
    }

    @Test
    @DisplayName("Date range filtering - Should exclude orders outside date range")
    void testDateRangeFiltering() {
        // Given: Orders before, during, and after the analysis period
        OrderJpaEntity beforePeriod = createCompletedOrder("ORD-BEFORE", startDate.minusDays(5).atTime(10, 0));
        beforePeriod.setTotalAmount(new BigDecimal("1000.00"));
        entityManager.persist(beforePeriod);

        OrderJpaEntity duringPeriod = createCompletedOrder("ORD-DURING", startDate.plusDays(5).atTime(10, 0));
        duringPeriod.setTotalAmount(new BigDecimal("2000.00"));
        entityManager.persist(duringPeriod);

        OrderJpaEntity afterPeriod = createCompletedOrder("ORD-AFTER", endDate.plusDays(5).atTime(10, 0));
        afterPeriod.setTotalAmount(new BigDecimal("3000.00"));
        entityManager.persist(afterPeriod);

        entityManager.flush();
        entityManager.clear();

        // When: Get daily sales for the period
        List<DailySales> result = analyticsRepository.getDailySales(startDate, endDate);

        // Then: Only the order during the period should be included
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRevenue()).isEqualByComparingTo(new BigDecimal("2000.00"));
    }

    @Test
    @DisplayName("Empty results - Should handle no data gracefully")
    void testEmptyResults() {
        // Given: No orders in database
        LocalDate emptyStart = LocalDate.of(2025, 1, 1);
        LocalDate emptyEnd = LocalDate.of(2025, 1, 31);

        // When: Get analytics for empty period
        List<ProductPerformance> bestSelling = analyticsRepository.getBestSellingProducts(10, emptyStart, emptyEnd);
        List<DailySales> dailySales = analyticsRepository.getDailySales(emptyStart, emptyEnd);
        CustomerRetention retention = analyticsRepository.getCustomerRetention(emptyStart, emptyEnd);

        // Then: Should return empty lists or zero values
        assertThat(bestSelling).isEmpty();
        assertThat(dailySales).isEmpty();
        assertThat(retention.getTotalCustomers()).isEqualTo(0);
        assertThat(retention.getReturningCustomers()).isEqualTo(0);
        assertThat(retention.getNewCustomers()).isEqualTo(0);
    }

    // ==================== Helper Methods ====================

    private ProductJpaEntity createProduct(String id, String name, int stock, 
                                          BigDecimal price, BigDecimal cost) {
        ProductJpaEntity product = new ProductJpaEntity();
        product.setId(id);
        product.setName(name);
        product.setSku(id + "-SKU");
        product.setTotalStock(stock);
        product.setAvailableStock(stock);
        product.setReservedStock(0);
        product.setPrice(price);
        product.setCostOfGoods(cost);
        product.setCurrency("USD");
        product.setActive(true);
        product.setVisible(true);
        product.setAvailableForPurchase(true);
        product.setDiscontinued(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return entityManager.persist(product);
    }

    private OrderJpaEntity createCompletedOrder(String orderId, LocalDateTime createdAt) {
        return createOrderWithStatus(orderId, "DELIVERED", createdAt);
    }

    private OrderJpaEntity createOrderWithStatus(String orderId, String status, LocalDateTime createdAt) {
        OrderJpaEntity order = new OrderJpaEntity();
        order.setId(orderId);
        order.setStatus(OrderStatus.valueOf(status));
        order.setCreatedAt(createdAt);
        order.setTotalAmount(BigDecimal.ZERO);
        order.setCustomerId("1");
        return order;
    }

    private void addOrderItem(OrderJpaEntity order, String productId, String productName, 
                             long quantity, BigDecimal price) {
        OrderItemJpaEntity item = new OrderItemJpaEntity();
        item.setProductId(productId);
        item.setProductName(productName);
        item.setQuantity((int) quantity);
        item.setPrice(price);
        order.getItems().add(item);
    }

    private void createCart(String cartId, String status, LocalDateTime createdAt, BigDecimal totalAmount) {
        CartJpaEntity cart = new CartJpaEntity();
        // CartJpaEntity from cart.infrastructure has @GeneratedValue - no manual ID
        cart.setStatus(CartStatusEntity.valueOf(status));
        cart.setCreatedAt(createdAt);
        cart.setLastUpdatedAt(createdAt);
        cart.setTotalAmount(totalAmount);
        entityManager.persist(cart);
    }

    private UserJpaEntity createUser(String id, String username, String email) {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setHashedPassword("hashed_password");
        user.setRole(UserJpaEntity.RoleType.CUSTOMER);
        user.setStatus(UserJpaEntity.StatusType.ACTIVE);
        user.setEmailVerified(true);
        user.setDeleted(false);
        user.setTermsAccepted(true);
        user.setMarketingConsentGiven(false);
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persist(user);
        return user;
    }
}
