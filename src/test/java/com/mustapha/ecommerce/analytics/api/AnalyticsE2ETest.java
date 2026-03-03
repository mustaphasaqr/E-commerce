package com.mustapha.ecommerce.analytics.api;

import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Analytics E2E Integration Tests
 * Tests full flow: create data → query analytics → verify aggregations
 * 
 * Pattern: SpringBootTest with full application context
 * Tests real database interactions and API responses
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Analytics End-to-End Integration Tests")
class AnalyticsE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EntityManager entityManager;

    private LocalDate startDate;
    private LocalDate endDate;
    private ProductJpaEntity product1;
    private ProductJpaEntity product2;

    @BeforeEach
    void setUp() {
        // Use past dates to avoid "future date" validation errors
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now().minusDays(1);
        
        // Create test products
        product1 = createProduct("TEST-PROD-001", "E2E Laptop", "Electronics", 100, 
                                new BigDecimal("1000.00"), new BigDecimal("600.00"));
        product2 = createProduct("TEST-PROD-002", "E2E Mouse", "Electronics", 200, 
                                new BigDecimal("25.00"), new BigDecimal("10.00"));
        
        entityManager.flush();
    }

    @Test
    @DisplayName("E2E: Order creation → Best selling products analytics")
    @WithMockUser(roles = "EMPLOYEE")
    void testOrderCreationToBestSellingProducts() throws Exception {
        // Given: Create multiple orders with product purchases
        createCompletedOrderWithItems("E2E-ORD-001", startDate.plusDays(5).atTime(10, 0),
            new OrderItem(product1.getId(), "E2E Laptop", 5, new BigDecimal("1000.00")),
            new OrderItem(product2.getId(), "E2E Mouse", 20, new BigDecimal("25.00"))
        );
        
        createCompletedOrderWithItems("E2E-ORD-002", startDate.plusDays(10).atTime(14, 0),
            new OrderItem(product1.getId(), "E2E Laptop", 3, new BigDecimal("1000.00")),
            new OrderItem(product2.getId(), "E2E Mouse", 15, new BigDecimal("25.00"))
        );
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query analytics API for best sellers
        mockMvc.perform(get("/api/owner/analytics/products/best-selling")
                .param("limit", "10")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[0].productName").value("E2E Mouse"))
            .andExpect(jsonPath("$[0].unitsSold").value(35))  // 20 + 15
            .andExpect(jsonPath("$[1].productName").value("E2E Laptop"))
            .andExpect(jsonPath("$[1].unitsSold").value(8));  // 5 + 3
    }

    @Test
    @DisplayName("E2E: Multiple orders → Daily sales aggregation")
    @WithMockUser(roles = "EMPLOYEE")
    void testMultipleOrdersToDailySales() throws Exception {
        // Given: Orders on different dates
        createCompletedOrderWithTotal("E2E-ORD-003", startDate.plusDays(5).atTime(10, 0), 
                                     new BigDecimal("1500.00"));
        createCompletedOrderWithTotal("E2E-ORD-004", startDate.plusDays(5).atTime(16, 0), 
                                     new BigDecimal("2500.00"));
        createCompletedOrderWithTotal("E2E-ORD-005", startDate.plusDays(10).atTime(12, 0), 
                                     new BigDecimal("3000.00"));
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query daily sales
        mockMvc.perform(get("/api/owner/analytics/sales/daily")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$.length()").value(2))  // 2 distinct days
            .andExpect(jsonPath("$[?(@.date == '" + startDate.plusDays(5) + "')].orderCount").value(2))
            .andExpect(jsonPath("$[?(@.date == '" + startDate.plusDays(5) + "')].revenue").value(4000.00))
            .andExpect(jsonPath("$[?(@.date == '" + startDate.plusDays(10) + "')].orderCount").value(1))
            .andExpect(jsonPath("$[?(@.date == '" + startDate.plusDays(10) + "')].revenue").value(3000.00));
    }

    @Test
    @DisplayName("E2E: Orders with different statuses → Sales summary")
    @WithMockUser(roles = "EMPLOYEE")
    void testOrderStatusesToSalesSummary() throws Exception {
        // Given: Orders with different statuses
        createOrderWithStatus("E2E-ORD-006", "DELIVERED", startDate.plusDays(5).atTime(10, 0), 
                             new BigDecimal("1000.00"));
        createOrderWithStatus("E2E-ORD-007", "DELIVERED", startDate.plusDays(10).atTime(14, 0), 
                             new BigDecimal("2000.00"));
        createOrderWithStatus("E2E-ORD-008", "CANCELLED", startDate.plusDays(15).atTime(12, 0), 
                             new BigDecimal("500.00"));
        createOrderWithStatus("E2E-ORD-009", "PENDING", startDate.plusDays(20).atTime(9, 0), 
                             new BigDecimal("750.00"));
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query sales summary
        mockMvc.perform(get("/api/owner/analytics/sales/summary")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalOrders").value(4))
            .andExpect(jsonPath("$.completedOrders").value(2))
            .andExpect(jsonPath("$.cancelledOrders").value(1))
            .andExpect(jsonPath("$.pendingOrders").value(1))
            .andExpect(jsonPath("$.totalRevenue").value(3000.00));  // Only completed
    }

    @Test
    @DisplayName("E2E: Customer orders → Top customers ranking")
    @WithMockUser(roles = "EMPLOYEE")
    void testCustomerOrdersToTopCustomers() throws Exception {
        // Given: Create users first
        createUser("101", "alice_e2e", "alice.e2e@example.com");
        createUser("102", "bob_e2e", "bob.e2e@example.com");
        
        // Given: Orders from different customers
        OrderJpaEntity order1 = createOrderWithStatus("E2E-ORD-010", "DELIVERED", 
                                                      startDate.plusDays(5).atTime(10, 0),
                                                      new BigDecimal("5000.00"));
        order1.setCustomerId("101");
        entityManager.merge(order1);

        OrderJpaEntity order2 = createOrderWithStatus("E2E-ORD-011", "DELIVERED", 
                                                      startDate.plusDays(10).atTime(14, 0),
                                                      new BigDecimal("3000.00"));
        order2.setCustomerId("101");
        entityManager.merge(order2);

        OrderJpaEntity order3 = createOrderWithStatus("E2E-ORD-012", "DELIVERED", 
                                                      startDate.plusDays(15).atTime(12, 0),
                                                      new BigDecimal("4000.00"));
        order3.setCustomerId("102");
        entityManager.merge(order3);
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query top customers
        mockMvc.perform(get("/api/owner/analytics/customers/top-buyers")
                .param("limit", "10")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].customerName").value("alice_e2e"))
            .andExpect(jsonPath("$[0].totalOrders").value(2))
            .andExpect(jsonPath("$[0].totalSpent").value(8000.00))
            .andExpect(jsonPath("$[1].customerName").value("bob_e2e"))
            .andExpect(jsonPath("$[1].totalOrders").value(1))
            .andExpect(jsonPath("$[1].totalSpent").value(4000.00));
    }

    @Test
    @DisplayName("E2E: Historical + current orders → Customer retention")
    @WithMockUser(roles = "EMPLOYEE")
    void testHistoricalOrdersToCustomerRetention() throws Exception {
        // Given: Orders before and during period
        LocalDate beforePeriod = startDate.minusDays(30);
        
        // Returning customer: ordered before AND during
        OrderJpaEntity oldOrder1 = createOrderWithStatus("E2E-ORD-OLD-001", "DELIVERED",
                                                         beforePeriod.atTime(10, 0),
                                                         new BigDecimal("1000.00"));
        oldOrder1.setCustomerId("201");
        entityManager.merge(oldOrder1);
        
        OrderJpaEntity newOrder1 = createOrderWithStatus("E2E-ORD-NEW-001", "DELIVERED",
                                                         startDate.plusDays(5).atTime(10, 0),
                                                         new BigDecimal("2000.00"));
        newOrder1.setCustomerId("201");
        entityManager.merge(newOrder1);

        // New customer: only ordered during period
        OrderJpaEntity newOrder2 = createOrderWithStatus("E2E-ORD-NEW-002", "DELIVERED",
                                                         startDate.plusDays(10).atTime(14, 0),
                                                         new BigDecimal("1500.00"));
        newOrder2.setCustomerId("202");
        entityManager.merge(newOrder2);
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query customer retention
        mockMvc.perform(get("/api/owner/analytics/customers/retention")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCustomers").value(2))
            .andExpect(jsonPath("$.returningCustomers").value(1))
            .andExpect(jsonPath("$.newCustomers").value(1));
    }

    @Test
    @DisplayName("E2E: Payment methods → Payment stats aggregation")
    @WithMockUser(roles = "EMPLOYEE")
    void testPaymentMethodsToPaymentStats() throws Exception {
        // Given: Orders with different payment methods
        OrderJpaEntity creditCard1 = createOrderWithStatus("E2E-ORD-013", "DELIVERED",
                                                           startDate.plusDays(5).atTime(10, 0),
                                                           new BigDecimal("1000.00"));
        creditCard1.setPaymentMethod("CREDIT_CARD");
        entityManager.merge(creditCard1);

        OrderJpaEntity creditCard2 = createOrderWithStatus("E2E-ORD-014", "DELIVERED",
                                                           startDate.plusDays(10).atTime(14, 0),
                                                           new BigDecimal("2000.00"));
        creditCard2.setPaymentMethod("CREDIT_CARD");
        entityManager.merge(creditCard2);

        OrderJpaEntity paypal = createOrderWithStatus("E2E-ORD-015", "DELIVERED",
                                                      startDate.plusDays(15).atTime(12, 0),
                                                      new BigDecimal("1500.00"));
        paypal.setPaymentMethod("PAYPAL");
        entityManager.merge(paypal);
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query payment method stats
        mockMvc.perform(get("/api/owner/analytics/payments/method-stats")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.paymentMethod == 'CREDIT_CARD')].transactionCount").value(2))
            .andExpect(jsonPath("$[?(@.paymentMethod == 'CREDIT_CARD')].totalAmount").value(3000.00))
            .andExpect(jsonPath("$[?(@.paymentMethod == 'PAYPAL')].transactionCount").value(1))
            .andExpect(jsonPath("$[?(@.paymentMethod == 'PAYPAL')].totalAmount").value(1500.00));
    }

    @Test
    @DisplayName("E2E: Time-based orders → Hourly sales distribution")
    @WithMockUser(roles = "EMPLOYEE")
    void testTimeBasedOrdersToHourlySales() throws Exception {
        // Given: Orders at different hours
        createCompletedOrderWithTotal("E2E-ORD-016", startDate.plusDays(5).atTime(9, 30),
                                     new BigDecimal("1000.00"));
        createCompletedOrderWithTotal("E2E-ORD-017", startDate.plusDays(5).atTime(14, 15),
                                     new BigDecimal("2000.00"));
        createCompletedOrderWithTotal("E2E-ORD-018", startDate.plusDays(10).atTime(14, 45),
                                     new BigDecimal("1500.00"));
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query hourly sales
        mockMvc.perform(get("/api/owner/analytics/sales/by-hour")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.hour == 9)].orderCount").value(1))
            .andExpect(jsonPath("$[?(@.hour == 9)].revenue").value(1000.00))
            .andExpect(jsonPath("$[?(@.hour == 14)].orderCount").value(2))
            .andExpect(jsonPath("$[?(@.hour == 14)].revenue").value(3500.00));
    }

    @Test
    @DisplayName("E2E: Product costs → Profit margin calculation")
    @WithMockUser(roles = "EMPLOYEE")
    void testProductCostsToProfitMargins() throws Exception {
        // Given: Order with products that have costs
        createCompletedOrderWithItems("E2E-ORD-019", startDate.plusDays(5).atTime(10, 0),
            new OrderItem(product1.getId(), "E2E Laptop", 5, new BigDecimal("1000.00")),
            new OrderItem(product2.getId(), "E2E Mouse", 20, new BigDecimal("25.00"))
        );
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query profit margins
        // Laptop: revenue = 5 * 1000 = 5000, cost = 5 * 600 = 3000, profit = 2000, margin = 40%
        // Mouse: revenue = 20 * 25 = 500, cost = 20 * 10 = 200, profit = 300, margin = 60%
        mockMvc.perform(get("/api/owner/analytics/profit/margins")
                .param("limit", "10")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString())
                .param("sortByProfit", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$[?(@.productName == 'E2E Laptop')].revenue").value(5000.00))
            .andExpect(jsonPath("$[?(@.productName == 'E2E Laptop')].cost").value(3000.00))
            .andExpect(jsonPath("$[?(@.productName == 'E2E Laptop')].profit").value(2000.00))
            .andExpect(jsonPath("$[?(@.productName == 'E2E Mouse')].revenue").value(500.00))
            .andExpect(jsonPath("$[?(@.productName == 'E2E Mouse')].cost").value(200.00))
            .andExpect(jsonPath("$[?(@.productName == 'E2E Mouse')].profit").value(300.00));
    }

    @Test
    @DisplayName("E2E: Cart lifecycle → Cart abandonment stats")
    @WithMockUser(roles = "EMPLOYEE")
    void testCartLifecycleToAbandonmentStats() throws Exception {
        // Given: Carts with different statuses
        createCart("E2E-CART-001", "ACTIVE", startDate.plusDays(5).atTime(10, 0), new BigDecimal("100.00"));
        createCart("E2E-CART-002", "ACTIVE", startDate.plusDays(6).atTime(11, 0), new BigDecimal("200.00"));
        createCart("E2E-CART-003", "CONVERTED", startDate.plusDays(7).atTime(12, 0), new BigDecimal("300.00"));
        createCart("E2E-CART-004", "CONVERTED", startDate.plusDays(8).atTime(13, 0), new BigDecimal("400.00"));
        createCart("E2E-CART-005", "ABANDONED", startDate.plusDays(10).atTime(15, 0), new BigDecimal("250.00"));
        createCart("E2E-CART-006", "ABANDONED", startDate.plusDays(11).atTime(16, 0), new BigDecimal("350.00"));
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query cart abandonment stats
        mockMvc.perform(get("/api/owner/analytics/carts/abandonment")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalCarts").value(6))
            .andExpect(jsonPath("$.activeCarts").value(2))
            .andExpect(jsonPath("$.convertedCarts").value(2))
            .andExpect(jsonPath("$.abandonedCarts").value(2))
            .andExpect(jsonPath("$.totalAbandonedValue").value(600.00))
            .andExpect(jsonPath("$.abandonmentRate").value(org.hamcrest.Matchers.closeTo(33.33, 0.01)))
            .andExpect(jsonPath("$.conversionRate").value(org.hamcrest.Matchers.closeTo(33.33, 0.01)));
    }

    @Test
    @DisplayName("E2E: Date range filtering → Excludes data outside range")
    @WithMockUser(roles = "EMPLOYEE")
    void testDateRangeFiltering() throws Exception {
        // Given: Orders before, during, and after period
        createCompletedOrderWithTotal("E2E-ORD-BEFORE", startDate.minusDays(5).atTime(10, 0),
                                     new BigDecimal("1000.00"));
        createCompletedOrderWithTotal("E2E-ORD-DURING", startDate.plusDays(5).atTime(10, 0),
                                     new BigDecimal("2000.00"));
        createCompletedOrderWithTotal("E2E-ORD-AFTER", endDate.plusDays(5).atTime(10, 0),
                                     new BigDecimal("3000.00"));
        
        entityManager.flush();
        entityManager.clear();

        // When/Then: Query should only include the one order during period
        mockMvc.perform(get("/api/owner/analytics/sales/daily")
                .param("startDate", startDate.toString())
                .param("endDate", endDate.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].revenue").value(2000.00));
    }

    // ==================== Helper Methods ====================

    private ProductJpaEntity createProduct(String id, String name, String category, int stock,
                                          BigDecimal price, BigDecimal cost) {
        ProductJpaEntity product = new ProductJpaEntity();
        product.setId(id);
        product.setName(name);
        product.setSku(id + "-SKU");
        product.setPrice(price);
        product.setCostOfGoods(cost);
        product.setCurrency("USD");
        product.setTotalStock(stock);
        product.setAvailableStock(stock);
        product.setReservedStock(0);
        product.setActive(true);
        product.setVisible(true);
        product.setAvailableForPurchase(true);
        product.setDiscontinued(false);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        entityManager.persist(product);
        return product;
    }

    private OrderJpaEntity createOrderWithStatus(String orderId, String status, LocalDateTime createdAt,
                                                 BigDecimal totalAmount) {
        OrderJpaEntity order = new OrderJpaEntity();
        order.setId(orderId);
        order.setStatus(OrderStatus.valueOf(status));
        order.setCreatedAt(createdAt);
        order.setTotalAmount(totalAmount);
        order.setCustomerId("1");
        order.setItems(new ArrayList<>());
        entityManager.persist(order);
        return order;
    }

    private void createCompletedOrderWithTotal(String orderId, LocalDateTime createdAt, BigDecimal totalAmount) {
        createOrderWithStatus(orderId, "DELIVERED", createdAt, totalAmount);
    }

    private void createCompletedOrderWithItems(String orderId, LocalDateTime createdAt, OrderItem... items) {
        OrderJpaEntity order = createOrderWithStatus(orderId, "DELIVERED", createdAt, BigDecimal.ZERO);
        BigDecimal total = BigDecimal.ZERO;
        
        for (OrderItem item : items) {
            OrderItemJpaEntity orderItem = new OrderItemJpaEntity();
            orderItem.setProductId(item.productId);
            orderItem.setProductName(item.productName);
            orderItem.setQuantity((int) item.quantity);
            orderItem.setPrice(item.price);
            order.getItems().add(orderItem);
            
            total = total.add(item.price.multiply(BigDecimal.valueOf(item.quantity)));
        }
        
        order.setTotalAmount(total);
        entityManager.merge(order);
    }

    private void createCart(String cartId, String status, LocalDateTime createdAt, BigDecimal totalAmount) {
        CartJpaEntity cart = new CartJpaEntity();
        cart.setStatus(CartStatusEntity.valueOf(status));
        cart.setCreatedAt(createdAt);
        cart.setTotalAmount(totalAmount);
        entityManager.persist(cart);
    }

    private static class OrderItem {
        final String productId;
        final String productName;
        final long quantity;
        final BigDecimal price;

        OrderItem(String productId, String productName, long quantity, BigDecimal price) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.price = price;
        }
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
