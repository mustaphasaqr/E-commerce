package com.mustapha.ecommerce.analytics.api;

import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartJpaEntity;
import com.mustapha.ecommerce.cart.infrastructure.persistence.entity.CartStatusEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderItemJpaEntity;
import com.mustapha.ecommerce.order.infrastructure.persistence.entity.OrderJpaEntity;
import com.mustapha.ecommerce.product.infrastructure.persistence.entity.ProductJpaEntity;
import com.mustapha.ecommerce.user.infrastructure.persistence.entity.UserJpaEntity;
import com.mustapha.ecommerce.order.domain.model.OrderStatus;
import com.mustapha.ecommerce.config.TestSchedulingConfig;
import com.mustapha.ecommerce.config.TestJpaAuditingConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.test.util.ReflectionTestUtils;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Analytics E2E Integration Tests
 * Tests full flow: create data → query analytics → verify aggregations
 * 
 * Pattern: SpringBootTest with TestRestTemplate (no transaction isolation)
 * Tests real database interactions and API responses
 */
@SpringBootTest(properties = {
    "spring.task.scheduling.enabled=false"  // Disable schedulers during tests
})
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@org.springframework.context.annotation.Import({TestSchedulingConfig.class, TestJpaAuditingConfig.class})
@DisplayName("Analytics End-to-End Integration Tests")
class AnalyticsE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private JwtTokenGenerator jwtTokenGenerator;
    
    @Autowired
    private com.mustapha.ecommerce.analytics.infrastructure.persistence.repository.JpaAnalyticsRepository analyticsRepository;

    private LocalDate startDate;
    private LocalDate endDate;
    private ProductJpaEntity product1;
    private ProductJpaEntity product2;
    private String ownerToken;
    private String employeeToken;

    @BeforeEach
    void setUp() {
        // Use dates 60-30 days in the past (analytics allows historical queries)
        startDate = LocalDate.now().minusDays(60);
        endDate = LocalDate.now().minusDays(30);
        
        // Generate JWT tokens for test users (tokens don't require database)
        ownerToken = jwtTokenGenerator.generateAccessToken("owner-user-id", "OWNER", "test-owner-session");
        employeeToken = jwtTokenGenerator.generateAccessToken("employee-user-id", "EMPLOYEE", "test-employee-session");
    }

    /**
     * Create common test data used by most tests
     * Call this at the start of each test method
     */
    private void createCommonTestData() {
        // Create authenticated test users  
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
        String hashedPassword = passwordEncoder.encode("password");
        
        createAuthenticatedUser("owner-user-id", "owner", "owner@example.com", hashedPassword, UserJpaEntity.RoleType.OWNER);
        createAuthenticatedUser("employee-user-id", "employee", "employee@example.com", hashedPassword, UserJpaEntity.RoleType.EMPLOYEE);
        
        // Create default test user to match default customerId in orders
        createUser("1", "default_user", "default@example.com");
        
        // Create test products
        product1 = createProduct("TEST-PROD-001", "E2E Laptop", "Electronics", 100, 
                                new BigDecimal("1000.00"), new BigDecimal("600.00"));
        product2 = createProduct("TEST-PROD-002", "E2E Mouse", "Electronics", 200, 
                                new BigDecimal("25.00"), new BigDecimal("10.00"));
        
        entityManager.flush();
        // DON'T clear() here - we need product1/product2 references for their IDs
    }

    @Test
    @DisplayName("DEBUG: Test repository direct call with fixed timestamp")
    void testRepositoryDirectCall() {
        System.out.println("=== TEST START ===");
        System.out.println("StartDate: " + startDate);
        System.out.println("EndDate: " + endDate);
        
        // Given: Create test data with TestJpaAuditingConfig providing fixed timestamp
        createCommonTestData();
        
        // Create order with items (timestamp will be auto-set to 45 days ago by TestJpaAuditingConfig)
        createCompletedOrderWithItems("DEBUG-ORD-001", startDate.plusDays(10).atTime(10, 0),
            new OrderItem(product1.getId(), "E2E Laptop", 5, new BigDecimal("1000.00")),
            new OrderItem(product2.getId(), "E2E Mouse", 20, new BigDecimal("25.00"))
        );
        
        entityManager.flush();
        entityManager.clear();
        
        // When: Call repository
        System.out.println("Calling repository with startDate=" + startDate + ", endDate=" + endDate);
        var result = analyticsRepository.getBestSellingProducts(10, startDate, endDate);
        System.out.println("Repository returned " + result.size() + " results");
        
        // Then: Should find data (timestamp from TestJpaAuditingConfig is within date range)
        assertThat(result).as("Repository should return data").isNotEmpty();
        assertThat(result.get(0).getProductName()).isEqualTo("E2E Mouse");
        assertThat(result.get(0).getUnitsSold()).isEqualTo(20);
    }

    @Test
    @DisplayName("E2E: Order creation → Best selling products analytics")
    void testOrderCreationToBestSellingProducts() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
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
        entityManager.clear();  // Clear persistence context like JpaAnalyticsRepositoryTest

        // DEBUG: Call repository directly to verify data exists
        var directResult = analyticsRepository.getBestSellingProducts(10, startDate, endDate);
        assertThat(directResult).as("Direct repository call should return data").isNotEmpty();
        assertThat(directResult.get(0).getProductName()).isEqualTo("E2E Mouse");
        assertThat(directResult.get(0).getUnitsSold()).isEqualTo(35);

        // When/Then: Query analytics API for best sellers
        String url = "/api/v1/owner/analytics/products/best-selling?limit=10&startDate=" 
            + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, ownerToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).isNotEmpty();
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(body, "$[0].productName")).isEqualTo("E2E Mouse");
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$[0].unitsSold")).isEqualTo(35);  // 20 + 15
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(body, "$[1].productName")).isEqualTo("E2E Laptop");
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$[1].unitsSold")).isEqualTo(8);   // 5 + 3
    }

    @Test
    @DisplayName("E2E: Multiple orders → Daily sales aggregation")
    void testMultipleOrdersToDailySales() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Orders on different dates
        createCompletedOrderWithTotal("E2E-ORD-003", startDate.plusDays(5).atTime(10, 0), 
                                     new BigDecimal("1500.00"));
        createCompletedOrderWithTotal("E2E-ORD-004", startDate.plusDays(5).atTime(16, 0), 
                                     new BigDecimal("2500.00"));
        createCompletedOrderWithTotal("E2E-ORD-005", startDate.plusDays(10).atTime(12, 0), 
                                     new BigDecimal("3000.00"));
        
        entityManager.flush();

        // When/Then: Query daily sales
        String url = "/api/v1/owner/analytics/sales/daily?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, ownerToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).hasSize(2);  // 2 distinct days
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.date == '" + startDate.plusDays(5) + "')].orderCount")).get(0)).isEqualTo(2);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.date == '" + startDate.plusDays(5) + "')].revenue")).get(0)).isEqualTo(4000.00);
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.date == '" + startDate.plusDays(10) + "')].orderCount")).get(0)).isEqualTo(1);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.date == '" + startDate.plusDays(10) + "')].revenue")).get(0)).isEqualTo(3000.00);
    }

    @Test
    @DisplayName("E2E: Orders with different statuses → Sales summary")
    void testOrderStatusesToSalesSummary() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
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

        // When/Then: Query sales summary
        String url = "/api/v1/owner/analytics/sales/summary?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, employeeToken);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.totalOrders")).isEqualTo(4);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.completedOrders")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.cancelledOrders")).isEqualTo(1);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.pendingOrders")).isEqualTo(1);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$.totalRevenue")).isEqualTo(3000.00);  // Only completed
    }

    @Test
    @DisplayName("E2E: Customer orders → Top customers ranking")
    void testCustomerOrdersToTopCustomers() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Create additional test users
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

        // When/Then: Query top customers
        String url = "/api/v1/owner/analytics/customers/top-buyers?limit=10&startDate=" 
            + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, ownerToken);
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(body, "$[0].customerName")).isEqualTo("alice_e2e");
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$[0].totalOrders")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$[0].totalSpent")).isEqualTo(8000.00);
        assertThat(com.jayway.jsonpath.JsonPath.<String>read(body, "$[1].customerName")).isEqualTo("bob_e2e");
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$[1].totalOrders")).isEqualTo(1);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$[1].totalSpent")).isEqualTo(4000.00);
    }

    @Test
    @DisplayName("E2E: Historical + current orders → Customer retention")
    void testHistoricalOrdersToCustomerRetention() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Create additional test users
        createUser("201", "returning_customer", "returning@example.com");
        createUser("202", "new_customer", "new@example.com");
        
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

        // When/Then: Query customer retention
        String url = "/api/v1/owner/analytics/customers/retention?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, ownerToken);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.totalCustomers")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.returningCustomers")).isEqualTo(1);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.newCustomers")).isEqualTo(1);
    }

    @Test
    @DisplayName("E2E: Payment methods → Payment stats aggregation")
    void testPaymentMethodsToPaymentStats() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
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

        // When/Then: Query payment method stats
        String url = "/api/v1/owner/analytics/payments/method-stats?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, employeeToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).isNotEmpty();
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.paymentMethod == 'CREDIT_CARD')].transactionCount")).get(0)).isEqualTo(2);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.paymentMethod == 'CREDIT_CARD')].totalAmount")).get(0)).isEqualTo(3000.00);
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.paymentMethod == 'PAYPAL')].transactionCount")).get(0)).isEqualTo(1);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.paymentMethod == 'PAYPAL')].totalAmount")).get(0)).isEqualTo(1500.00);
    }

    @Test
    @DisplayName("E2E: Time-based orders → Hourly sales distribution")
    void testTimeBasedOrdersToHourlySales() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Orders at different hours
        createCompletedOrderWithTotal("E2E-ORD-016", startDate.plusDays(5).atTime(9, 30),
                                     new BigDecimal("1000.00"));
        createCompletedOrderWithTotal("E2E-ORD-017", startDate.plusDays(5).atTime(14, 15),
                                     new BigDecimal("2000.00"));
        createCompletedOrderWithTotal("E2E-ORD-018", startDate.plusDays(10).atTime(14, 45),
                                     new BigDecimal("1500.00"));
        
        entityManager.flush();

        // When/Then: Query hourly sales
        String url = "/api/v1/owner/analytics/sales/by-hour?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, employeeToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).isNotEmpty();
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.hour == 9)].orderCount")).get(0)).isEqualTo(1);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.hour == 9)].revenue")).get(0)).isEqualTo(1000.00);
        assertThat(((List<Integer>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.hour == 14)].orderCount")).get(0)).isEqualTo(2);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.hour == 14)].revenue")).get(0)).isEqualTo(3500.00);
    }

    @Test
    @DisplayName("E2E: Product costs → Profit margin calculation")
    void testProductCostsToProfitMargins() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Order with products that have costs
        createCompletedOrderWithItems("E2E-ORD-019", startDate.plusDays(5).atTime(10, 0),
            new OrderItem(product1.getId(), "E2E Laptop", 5, new BigDecimal("1000.00")),
            new OrderItem(product2.getId(), "E2E Mouse", 20, new BigDecimal("25.00"))
        );
        
        entityManager.flush();

        // When/Then: Query profit margins
        // Laptop: revenue = 5 * 1000 = 5000, cost = 5 * 600 = 3000, profit = 2000, margin = 40%
        // Mouse: revenue = 20 * 25 = 500, cost = 20 * 10 = 200, profit = 300, margin = 60%
        String url = "/api/v1/owner/analytics/profit/margins?limit=10&startDate=" 
            + startDate + "&endDate=" + endDate + "&sortByProfit=true";
        String body = getWithAuth(url, employeeToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).isNotEmpty();
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Laptop')].revenue")).get(0)).isEqualTo(5000.00);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Laptop')].cost")).get(0)).isEqualTo(3000.00);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Laptop')].profit")).get(0)).isEqualTo(2000.00);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Mouse')].revenue")).get(0)).isEqualTo(500.00);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Mouse')].cost")).get(0)).isEqualTo(200.00);
        assertThat(((List<Double>) com.jayway.jsonpath.JsonPath.read(body, "$[?(@.productName == 'E2E Mouse')].profit")).get(0)).isEqualTo(300.00);
    }

    @Test
    @DisplayName("E2E: Cart lifecycle → Cart abandonment stats")
    void testCartLifecycleToAbandonmentStats() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Carts with different statuses
        createCart("E2E-CART-001", "ACTIVE", startDate.plusDays(5).atTime(10, 0), new BigDecimal("100.00"));
        createCart("E2E-CART-002", "ACTIVE", startDate.plusDays(6).atTime(11, 0), new BigDecimal("200.00"));
        createCart("E2E-CART-003", "CONVERTED", startDate.plusDays(7).atTime(12, 0), new BigDecimal("300.00"));
        createCart("E2E-CART-004", "CONVERTED", startDate.plusDays(8).atTime(13, 0), new BigDecimal("400.00"));
        createCart("E2E-CART-005", "ABANDONED", startDate.plusDays(10).atTime(15, 0), new BigDecimal("250.00"));
        createCart("E2E-CART-006", "ABANDONED", startDate.plusDays(11).atTime(16, 0), new BigDecimal("350.00"));
        
        entityManager.flush();

        // When/Then: Query cart abandonment stats
        String url = "/api/v1/owner/analytics/carts/abandonment?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, employeeToken);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.totalCarts")).isEqualTo(6);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.activeCarts")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.convertedCarts")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Integer>read(body, "$.abandonedCarts")).isEqualTo(2);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$.totalAbandonedValue")).isEqualTo(600.00);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$.abandonmentRate")).isCloseTo(33.33, org.assertj.core.data.Offset.offset(0.01));
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$.conversionRate")).isCloseTo(33.33, org.assertj.core.data.Offset.offset(0.01));
    }

    @Test
    @DisplayName("E2E: Date range filtering → Excludes data outside range")
    void testDateRangeFiltering() throws Exception {
        // Given: Create common test data (users + products)
        createCommonTestData();
        
        // Given: Orders before, during, and after period
        createCompletedOrderWithTotal("E2E-ORD-BEFORE", startDate.minusDays(5).atTime(10, 0),
                                     new BigDecimal("1000.00"));
        createCompletedOrderWithTotal("E2E-ORD-DURING", startDate.plusDays(5).atTime(10, 0),
                                     new BigDecimal("2000.00"));
        createCompletedOrderWithTotal("E2E-ORD-AFTER", endDate.plusDays(5).atTime(10, 0),
                                     new BigDecimal("3000.00"));
        
        entityManager.flush();

        // When/Then: Query should only include the one order during period
        String url = "/api/v1/owner/analytics/sales/daily?startDate=" + startDate + "&endDate=" + endDate;
        String body = getWithAuth(url, employeeToken);
        assertThat((List<?>) com.jayway.jsonpath.JsonPath.read(body, "$")).hasSize(1);
        assertThat(com.jayway.jsonpath.JsonPath.<Double>read(body, "$[0].revenue")).isEqualTo(2000.00);
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
        entityManager.flush(); // Force persist to generate SQL
        
        // CRITICAL: Overwrite auto-set timestamp using native SQL (bypasses JPA Auditing)
        entityManager.createNativeQuery(
            "UPDATE orders SET created_at = :timestamp WHERE id = :id"
        ).setParameter("timestamp", createdAt)
         .setParameter("id", orderId)
         .executeUpdate();
        
        return order;
    }

    private void createCompletedOrderWithTotal(String orderId, LocalDateTime createdAt, BigDecimal totalAmount) {
        createOrderWithStatus(orderId, "DELIVERED", createdAt, totalAmount);
    }

    private void createCompletedOrderWithItems(String orderId, LocalDateTime createdAt, OrderItem... items) {
        // Create order WITHOUT persisting yet
        OrderJpaEntity order = new OrderJpaEntity();
        order.setId(orderId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setCreatedAt(createdAt);
        order.setCustomerId("1");
        order.setItems(new ArrayList<>());
        
        BigDecimal total = BigDecimal.ZERO;
        
        // Add items BEFORE persisting
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
        entityManager.persist(order);  // Persist ONCE with all items
        entityManager.flush(); // Force persist to generate SQL
        
        // CRITICAL: Overwrite auto-set timestamp using native SQL (bypasses JPA Auditing)
        entityManager.createNativeQuery(
            "UPDATE orders SET created_at = :timestamp WHERE id = :id"
        ).setParameter("timestamp", createdAt)
         .setParameter("id", orderId)
         .executeUpdate();
    }

    private void createCart(String cartId, String status, LocalDateTime createdAt, BigDecimal totalAmount) {
        CartJpaEntity cart = new CartJpaEntity();
        cart.setStatus(CartStatusEntity.valueOf(status));
        cart.setCreatedAt(createdAt);
        cart.setTotalAmount(totalAmount);
        entityManager.persist(cart);
        entityManager.flush();
        
        // CRITICAL: Overwrite auto-set timestamp using native SQL
        entityManager.createNativeQuery(
            "UPDATE carts SET created_at = :timestamp WHERE id = (SELECT MAX(id) FROM carts)"
        ).setParameter("timestamp", createdAt)
         .executeUpdate();
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

    private UserJpaEntity createAuthenticatedUser(String id, String username, String email, String hashedPassword, UserJpaEntity.RoleType role) {
        UserJpaEntity user = new UserJpaEntity();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setHashedPassword(hashedPassword);
        user.setRole(role);
        user.setStatus(UserJpaEntity.StatusType.ACTIVE);
        user.setEmailVerified(true);
        user.setDeleted(false);
        user.setTermsAccepted(true);
        user.setMarketingConsentGiven(false);
        user.setCreatedAt(LocalDateTime.now());
        entityManager.persist(user);
        return user;
    }

    private String getWithAuth(String url, String token) throws Exception {
        return mockMvc.perform(get(url)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    /**
     * Insert order using native SQL to bypass JPA Auditing
     * This allows setting custom createdAt timestamps for analytics testing
     */
    private void insertOrderWithNativeSQL(String orderId, String customerId, String status, 
                                         LocalDateTime createdAt, List<OrderItem> items) {
        // Insert order
        entityManager.createNativeQuery(
            "INSERT INTO orders (id, customer_id, status, total_amount, " +
            "created_at, updated_at, created_by, updated_by) " +
            "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7, ?8)")
            .setParameter(1, orderId)
            .setParameter(2, customerId)
            .setParameter(3, status)
            .setParameter(4, BigDecimal.ZERO)
            .setParameter(5, createdAt)
            .setParameter(6, createdAt)
            .setParameter(7, "TEST")
            .setParameter(8, "TEST")
            .executeUpdate();
        
        // Insert order items
        for (int i = 0; i < items.size(); i++) {
            OrderItem item = items.get(i);
            entityManager.createNativeQuery(
                "INSERT INTO order_items (id, order_id, product_id, product_name, " +
                "quantity, price, discount_amount) " +
                "VALUES (?1, ?2, ?3, ?4, ?5, ?6, ?7)")
                .setParameter(1, orderId + "-ITEM-" + i)
                .setParameter(2, orderId)
                .setParameter(3, item.productId)
                .setParameter(4, item.productName)
                .setParameter(5, item.quantity)
                .setParameter(6, item.price)
                .setParameter(7, BigDecimal.ZERO)
                .executeUpdate();
        }
        
        entityManager.flush();
    }
}
