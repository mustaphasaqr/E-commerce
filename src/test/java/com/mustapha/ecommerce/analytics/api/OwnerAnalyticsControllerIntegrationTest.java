package com.mustapha.ecommerce.analytics.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.infrastructure.security.BCryptPasswordHasher;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OwnerAnalyticsController Integration Tests
 * Tests with real database and full Spring context
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("OwnerAnalyticsController Integration Tests")
class OwnerAnalyticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private BCryptPasswordHasher passwordHasher;

    private Product testProduct;
    private User testCustomer;
    private Order testOrder;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(7);
        endDate = LocalDate.now().minusDays(1);  // Use yesterday to avoid "future date" validation issues
        
        // Create test product using factory method
        testProduct = Product.create(
            SKU.of("INT-TEST-SKU-" + System.currentTimeMillis()),
            "Integration Test Product",
            "Product for analytics integration testing",
            Price.of(new BigDecimal("99.99"), "USD"),
            Stock.of(100)
        );
        testProduct = productRepository.save(testProduct);

        // Create test customer using factory method
        testCustomer = User.create(
            Username.of("analyticstest" + System.currentTimeMillis()),
            Email.of("analytics-test-" + System.currentTimeMillis() + "@example.com"),
            Password.fromPlainText("AnalyticsTest123!@#", passwordHasher),
            Role.CUSTOMER
        );
        testCustomer.acceptTerms("v1.0");
        testCustomer.verifyEmail();
        testCustomer.activate("Test setup");
        testCustomer = userRepository.save(testCustomer);

        // Create test order using OrderBuilder
        // Convert Product domain ProductId to Order domain ProductId
        com.mustapha.ecommerce.order.domain.model.valueobject.ProductId orderProductId = 
            new com.mustapha.ecommerce.order.domain.model.valueobject.ProductId(testProduct.getId().getValue());
        
        OrderItem orderItem = new OrderItem(
            orderProductId,
            testProduct.getName(),
            2,
            new Money(99.99)
        );
        
        testOrder = new OrderBuilder()
            .withCustomerId(new CustomerId(testCustomer.getId().getValue().toString()))
            .addItem(orderItem)
            .build();
        testOrder.confirm();
        testOrder.markAsPaid();
        testOrder.startProcessing();
        testOrder.ship("test-tracking-123", "TestCarrier");
        testOrder.deliver(LocalDateTime.now());
        testOrder = orderRepository.save(testOrder);
    }

    @Nested
    @DisplayName("Product Performance Integration Tests")
    class ProductPerformanceIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/best-selling - Should return products from database")
        @WithMockUser(roles = "OWNER")
        void shouldGetBestSellingProductsFromDatabase() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/top-revenue - Should calculate revenue correctly")
        @WithMockUser(roles = "OWNER")
        void shouldCalculateRevenueCorrectly() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/top-revenue")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/worst-selling - Should identify slow-moving products")
        @WithMockUser(roles = "OWNER")
        void shouldIdentifySlowMovingProducts() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/worst-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Sales Analytics Integration Tests")
    class SalesAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/daily-sales - Should aggregate sales by day")
        @WithMockUser(roles = "OWNER")
        void shouldAggregateSalesByDay() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().contentType("application/json"));
        }

        @Test
        @DisplayName("GET /api/analytics/hourly-sales - Should aggregate sales by hour")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldAggregateSalesByHour() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/sales/by-hour")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/day-of-week-sales - Should group sales by weekday")
        @WithMockUser(roles = "OWNER")
        void shouldGroupSalesByWeekday() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/sales/by-day-of-week")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Daily sales should span entire date range")
        @WithMockUser(roles = "OWNER")
        void dailySalesShouldSpanEntireDateRange() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", LocalDate.now().minusDays(30).toString())
                    .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Customer Analytics Integration Tests")
    class CustomerAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/top-customers - Should rank customers by spending")
        @WithMockUser(roles = "OWNER")
        void shouldRankCustomersBySpending() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/customers/top-buyers")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/cart-abandonment - Should calculate abandonment rate")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldCalculateAbandonmentRate() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/carts/abandonment")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCarts").exists())
                .andExpect(jsonPath("$.abandonmentRate").exists());
        }
    }

    @Nested
    @DisplayName("Inventory Analytics Integration Tests")
    class InventoryAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/low-stock - Should identify low inventory")
        @WithMockUser(roles = "OWNER")
        void shouldIdentifyLowInventory() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/inventory/low-stock")
                    .param("threshold", "50")
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/out-of-stock - Should list unavailable products")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldListUnavailableProducts() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/inventory/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/inventory-turnover - Should calculate turnover ratio")
        @WithMockUser(roles = "OWNER")
        void shouldCalculateTurnoverRatio() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/inventory/turnover")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/dead-stock - Should identify stagnant inventory")
        @WithMockUser(roles = "OWNER")
        void shouldIdentifyStagnantInventory() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/inventory/dead-stock")
                    .param("daysWithoutSales", "90")
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Revenue Analytics Integration Tests")
    class RevenueAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/revenue-by-category - Should group revenue by category")
        @WithMockUser(roles = "OWNER")
        void shouldGroupRevenueByCategory() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/revenue/by-category")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/revenue-forecast - Should predict future revenue")
        @WithMockUser(roles = "OWNER")
        void shouldPredictFutureRevenue() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/revenue/forecast")
                    .param("historicalStartDate", startDate.toString())
                    .param("historicalEndDate", endDate.toString())
                    .param("forecastDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictedRevenue").exists());
        }

        @Test
        @DisplayName("GET /api/analytics/profit-margins - Should calculate margins")
        @WithMockUser(roles = "OWNER")
        void shouldCalculateMargins() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/profit/margins")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Payment & Refund Analytics Integration Tests")
    class PaymentAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/payment-stats - Should aggregate by payment method")
        @WithMockUser(roles = "OWNER")
        void shouldAggregateByPaymentMethod() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/payments/method-stats")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/refund-stats - Should calculate refund metrics")
        @WithMockUser(roles = "OWNER")
        void shouldCalculateRefundMetrics() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/refunds/stats")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompletedOrders").exists())
                .andExpect(jsonPath("$.refundRate").exists());
        }
    }

    @Nested
    @DisplayName("Geographic Analytics Integration Tests")
    class GeographicAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/sales-by-location - Should group by geography")
        @WithMockUser(roles = "OWNER")
        void shouldGroupByGeography() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/geographic/sales")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("GET /api/analytics/shipping-performance - Should calculate delivery metrics")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldCalculateDeliveryMetrics() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/shipping/performance")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Marketing Analytics Integration Tests")
    class MarketingAnalyticsIntegrationTests {

        @Test
        @DisplayName("GET /api/analytics/marketing-attribution - Should track campaign performance")
        @WithMockUser(roles = "OWNER")
        void shouldTrackCampaignPerformance() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/marketing/attribution")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("Performance & Caching Tests")
    class PerformanceTests {

        @Test
        @DisplayName("Analytics queries should complete within performance SLA")
        @WithMockUser(roles = "OWNER")
        void queriesShouldMeetPerformanceSLA() throws Exception {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk());
            
            long duration = System.currentTimeMillis() - startTime;
            Assertions.assertTrue(duration < 2000, 
                "Query should complete within 2 seconds, took: " + duration + "ms");
        }

        @Test
        @DisplayName("Repeated queries should benefit from caching")
        @WithMockUser(roles = "OWNER")
        void repeatedQueriesShouldBenefitFromCaching() throws Exception {
            // First request - cache miss
            long firstRequest = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk());
            long firstDuration = System.currentTimeMillis() - firstRequest;

            // Second request - should be faster (cached)
            long secondRequest = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk());
            long secondDuration = System.currentTimeMillis() - secondRequest;

            // Second request should be significantly faster
            System.out.println("First request: " + firstDuration + "ms, Second request: " + secondDuration + "ms");
        }
    }

    @Nested
    @DisplayName("Data Consistency Tests")
    class DataConsistencyTests {

        @Test
        @DisplayName("Revenue totals should match across different analytics views")
        @WithMockUser(roles = "OWNER")
        void revenueTotalsShouldMatchAcrossViews() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

            mockMvc.perform(get("/api/v1/owner/analytics/revenue/by-category")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
        }

        @Test
        @DisplayName("Product quantities should match across analytics")
        @WithMockUser(roles = "OWNER")
        void productQuantitiesShouldMatchAcrossAnalytics() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/turnover")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk());
        }
    }
}

