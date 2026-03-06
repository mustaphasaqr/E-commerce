package com.mustapha.ecommerce.analytics.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.analytics.application.facade.AnalyticsFacade;
import com.mustapha.ecommerce.analytics.api.dto.*;
import com.mustapha.ecommerce.analytics.api.rest.OwnerAnalyticsController;
import com.mustapha.ecommerce.config.WebMvcTestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OwnerAnalyticsController Unit Tests
 * Tests all 20 analytics endpoints with mock facade
 */
@WebMvcTest(OwnerAnalyticsController.class)
@Import(WebMvcTestConfig.class)
@DisplayName("OwnerAnalyticsController Unit Tests")
class OwnerAnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnalyticsFacade analyticsFacade;

    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.now().minusDays(30);
        endDate = LocalDate.now();
    }

    @Nested
    @DisplayName("Product Performance Analytics")
    class ProductPerformanceTests {

        @Test
        @DisplayName("GET /api/analytics/best-selling - Should return best selling products")
        @WithMockUser(roles = "OWNER")
        void shouldGetBestSellingProducts() throws Exception {
            ProductPerformanceDTO product = new ProductPerformanceDTO(
                "product-id-123",
                "Best Seller",
                100L,
                new BigDecimal("9999.99"),
                10L
            );
            when(analyticsFacade.getBestSellingProducts(anyInt(), any(), any()))
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].productId").value("product-id-123"))
                .andExpect(jsonPath("$[0].productName").value("Best Seller"))
                .andExpect(jsonPath("$[0].unitsSold").value(100));

            verify(analyticsFacade).getBestSellingProducts(eq(10), any(), any());
        }

        @Test
        @DisplayName("GET /api/analytics/worst-selling - Should return worst selling products")
        @WithMockUser(roles = "OWNER")
        void shouldGetWorstSellingProducts() throws Exception {
            ProductPerformanceDTO product = new ProductPerformanceDTO(
                "product-id-456",
                "Slow Mover",
                5L,
                new BigDecimal("99.99"),
                2L
            );
            when(analyticsFacade.getWorstSellingProducts(anyInt(), any(), any()))
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/products/worst-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].unitsSold").value(5));

            verify(analyticsFacade).getWorstSellingProducts(eq(10), any(), any());
        }

        @Test
        @DisplayName("GET /api/analytics/top-revenue - Should return top revenue products")
        @WithMockUser(roles = "OWNER")
        void shouldGetTopRevenueProducts() throws Exception {
            ProductPerformanceDTO product = new ProductPerformanceDTO(
                "product-id-789",
                "Big Revenue",
                50L,
                new BigDecimal("50000.00"),
                25L
            );
            when(analyticsFacade.getTopRevenueProducts(anyInt(), any(), any()))
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/products/top-revenue")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalRevenue").value(50000.00));
        }
    }

    @Nested
    @DisplayName("Sales Analytics")
    class SalesAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/daily-sales - Should return daily sales")
        @WithMockUser(roles = "OWNER")
        void shouldGetDailySales() throws Exception {
            DailySalesDTO sales = new DailySalesDTO(
                LocalDate.now(),
                150,
                new BigDecimal("15000.00"),
                new BigDecimal("14000.00")
            );
            when(analyticsFacade.getDailySales(any(), any()))
                .thenReturn(List.of(sales));

            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderCount").value(150))
                .andExpect(jsonPath("$[0].revenue").value(15000.00));
        }

        @Test
        @DisplayName("GET /api/analytics/hourly-sales - Should return hourly sales")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldGetHourlySales() throws Exception {
            HourlySalesDTO sales = new HourlySalesDTO(
                14,
                25L,
                new BigDecimal("3500.00")
            );
            when(analyticsFacade.getSalesByHour(any(), any()))
                .thenReturn(List.of(sales));

            mockMvc.perform(get("/api/v1/owner/analytics/sales/by-hour")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].hour").value(14))
                .andExpect(jsonPath("$[0].orderCount").value(25));
        }

        @Test
        @DisplayName("GET /api/analytics/day-of-week-sales - Should return sales by day of week")
        @WithMockUser(roles = "OWNER")
        void shouldGetSalesByDayOfWeek() throws Exception {
            DailySalesDTO sales = new DailySalesDTO(
                LocalDate.now(),
                200,
                new BigDecimal("25000.00"),
                new BigDecimal("125.00")
            );
            when(analyticsFacade.getSalesByDayOfWeek(any(), any()))
                .thenReturn(List.of(sales));

            mockMvc.perform(get("/api/v1/owner/analytics/sales/by-day-of-week")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].orderCount").value(200))
                .andExpect(jsonPath("$[0].revenue").value(25000.00));
        }
    }

    @Nested
    @DisplayName("Customer Analytics")
    class CustomerAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/top-customers - Should return top customers")
        @WithMockUser(roles = "OWNER")
        void shouldGetTopCustomers() throws Exception {
            TopCustomerDTO customer = new TopCustomerDTO(
                "123",
                "John Doe",
                "john@example.com",
                50L,
                new BigDecimal("10000.00"),
                new BigDecimal("200.00")
            );
            when(analyticsFacade.getTopCustomers(anyInt(), any(), any()))
                .thenReturn(List.of(customer));

            mockMvc.perform(get("/api/v1/owner/analytics/customers/top-buyers")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(123))
                .andExpect(jsonPath("$[0].totalOrders").value(50))
                .andExpect(jsonPath("$[0].totalSpent").value(10000.00));
        }

        @Test
        @DisplayName("GET /api/analytics/cart-abandonment - Should return cart abandonment stats")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldGetCartAbandonmentStats() throws Exception {
            CartAbandonmentDTO stats = new CartAbandonmentDTO(
                100L,
                20L,
                55L,
                25L,
                new BigDecimal("5000.00"),
                new BigDecimal("200.00"),
                25.0,
                55.0,
                false,
                new BigDecimal("5000.00")
            );
            when(analyticsFacade.getCartAbandonmentStats(any(), any()))
                .thenReturn(stats);

            mockMvc.perform(get("/api/v1/owner/analytics/carts/abandonment")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCarts").value(100))
                .andExpect(jsonPath("$.abandonedCarts").value(25))
                .andExpect(jsonPath("$.abandonmentRate").value(25.0));
        }
    }

    @Nested
    @DisplayName("Inventory Analytics")
    class InventoryAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/low-stock - Should return low stock products")
        @WithMockUser(roles = "OWNER")
        void shouldGetLowStockProducts() throws Exception {
            LowStockProductDTO product = new LowStockProductDTO(
                "123",
                "Low Stock Item",
                5,
                20,
                100L,
                true
            );
            when(analyticsFacade.getLowStockProducts(anyInt()))
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/low-stock")
                    .param("threshold", "10")
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentStock").value(5))
                .andExpect(jsonPath("$[0].stockThreshold").value(20));
        }

        @Test
        @DisplayName("GET /api/analytics/out-of-stock - Should return out of stock products")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldGetOutOfStockProducts() throws Exception {
            LowStockProductDTO product = new LowStockProductDTO(
                "456",
                "Out of Stock",
                0,
                10,
                50L,
                true
            );
            when(analyticsFacade.getOutOfStockProducts())
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/out-of-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentStock").value(0));
        }

        @Test
        @DisplayName("GET /api/analytics/inventory-turnover - Should return inventory turnover")
        @WithMockUser(roles = "OWNER")
        void shouldGetInventoryTurnover() throws Exception {
            InventoryTurnoverDTO turnover = new InventoryTurnoverDTO(
                "123",
                "Fast Mover",
                500L,
                100,
                5.0,
                20
            );
            when(analyticsFacade.getInventoryTurnover(any(), any()))
                .thenReturn(List.of(turnover));

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/turnover")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].turnoverRate").value(5.0));
        }

        @Test
        @DisplayName("GET /api/analytics/dead-stock - Should return dead stock products")
        @WithMockUser(roles = "OWNER")
        void shouldGetDeadStock() throws Exception {
            LowStockProductDTO product = new LowStockProductDTO(
                "789",
                "Dead Stock",
                100,
                50,
                0L,
                false
            );
            when(analyticsFacade.getDeadStockProducts())
                .thenReturn(List.of(product));

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/dead-stock")
                    .param("daysWithoutSales", "90")
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currentStock").value(100));
        }
    }

    @Nested
    @DisplayName("Revenue Analytics")
    class RevenueAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/revenue-by-category - Should return revenue by category")
        @WithMockUser(roles = "OWNER")
        void shouldGetRevenueByCategory() throws Exception {
            CategoryRevenueDTO revenue = new CategoryRevenueDTO(
                "Electronics",
                50L,
                250L,
                new BigDecimal("50000.00"),
                new BigDecimal("1000.00")
            );
            when(analyticsFacade.getRevenueByCategory(any(), any()))
                .thenReturn(List.of(revenue));

            mockMvc.perform(get("/api/v1/owner/analytics/revenue/by-category")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].category").value("Electronics"))
                .andExpect(jsonPath("$[0].totalRevenue").value(50000.00));
        }

        @Test
        @DisplayName("GET /api/analytics/revenue-forecast - Should return revenue forecast")
        @WithMockUser(roles = "OWNER")
        void shouldGetRevenueForecast() throws Exception {
            RevenueForecastDTO forecast = new RevenueForecastDTO(
                LocalDate.now(),
                LocalDate.now().plusDays(30),
                30,
                new BigDecimal("5000.00"),
                new BigDecimal("4500.00"),
                new BigDecimal("5500.00"),
                new BigDecimal("166.67"),
                new BigDecimal("0.02"),
                "GROWING",
                new BigDecimal("0.85"),
                90,
                List.of()
            );
            when(analyticsFacade.getRevenueForecast(any(), any(), anyInt()))
                .thenReturn(forecast);

            mockMvc.perform(get("/api/v1/owner/analytics/revenue/forecast")
                    .param("historicalStartDate", startDate.toString())
                    .param("historicalEndDate", endDate.toString())
                    .param("forecastDays", "30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.predictedRevenue").value(5000.00));
        }

        @Test
        @DisplayName("GET /api/analytics/profit-margins - Should return profit margins")
        @WithMockUser(roles = "OWNER")
        void shouldGetProfitMargins() throws Exception {
            ProfitMarginDTO margin = new ProfitMarginDTO(
                "product-id-123",
                "High Margin Product",
                100L,
                new BigDecimal("10000.00"),
                new BigDecimal("6000.00"),
                new BigDecimal("4000.00"),
                new BigDecimal("40.00"),
                true
            );
            when(analyticsFacade.getProfitMargins(anyInt(), any(), any(), anyBoolean()))
                .thenReturn(List.of(margin));

            mockMvc.perform(get("/api/v1/owner/analytics/profit/margins")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].profitMarginPercent").value(40.00));
        }
    }

    @Nested
    @DisplayName("Payment & Refund Analytics")
    class PaymentAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/payment-stats - Should return payment method statistics")
        @WithMockUser(roles = "OWNER")
        void shouldGetPaymentMethodStats() throws Exception {
            PaymentMethodStatsDTO stats = new PaymentMethodStatsDTO(
                "CREDIT_CARD",
                500L,
                new BigDecimal("75000.00"),
                475L,
                25L,
                95.0,
                5.0
            );
            when(analyticsFacade.getPaymentMethodStats(any(), any()))
                .thenReturn(List.of(stats));

            mockMvc.perform(get("/api/v1/owner/analytics/payments/method-stats")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].paymentMethod").value("CREDIT_CARD"))
                .andExpect(jsonPath("$[0].transactionCount").value(500));
        }

        @Test
        @DisplayName("GET /api/analytics/refund-stats - Should return refund statistics")
        @WithMockUser(roles = "OWNER")
        void shouldGetRefundStats() throws Exception {
            RefundStatsDTO stats = new RefundStatsDTO(
                1000L,
                60L,
                50L,
                50L,
                10L,
                new BigDecimal("5000.00"),
                new BigDecimal("5.00"),
                new BigDecimal("100.00"),
                false
            );
            when(analyticsFacade.getRefundStats(any(), any()))
                .thenReturn(stats);

            mockMvc.perform(get("/api/v1/owner/analytics/refunds/stats")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCompletedOrders").value(1000))
                .andExpect(jsonPath("$.refundCompletedCount").value(50))
                .andExpect(jsonPath("$.refundRate").value(5.00));
        }
    }

    @Nested
    @DisplayName("Geographic & Shipping Analytics")
    class GeographicAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/sales-by-location - Should return sales by location")
        @WithMockUser(roles = "OWNER")
        void shouldGetSalesByLocation() throws Exception {
            GeographicSalesDTO sales = new GeographicSalesDTO(
                "Cairo",
                "Cairo Governorate",
                "Egypt",
                350L,
                new BigDecimal("45000.00"),
                new BigDecimal("128.57"),
                "Egypt-Cairo"
            );
            when(analyticsFacade.getSalesByLocation(any(), any(), anyBoolean()))
                .thenReturn(List.of(sales));

            mockMvc.perform(get("/api/v1/owner/analytics/geographic/sales")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].country").value("Egypt"))
                .andExpect(jsonPath("$[0].city").value("Cairo"));
        }

        @Test
        @DisplayName("GET /api/analytics/shipping-performance - Should return shipping performance")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldGetShippingPerformance() throws Exception {
            ShippingPerformanceDTO performance = new ShippingPerformanceDTO(
                "Standard Carrier",
                500L,
                475L,
                24.0,
                72.0,
                95.0,
                true
            );
            when(analyticsFacade.getShippingPerformance(any(), any()))
                .thenReturn(List.of(performance));

            mockMvc.perform(get("/api/v1/owner/analytics/shipping/performance")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].carrier").value("Standard Carrier"))
                .andExpect(jsonPath("$[0].deliverySuccessRate").value(95.0));
        }
    }

    @Nested
    @DisplayName("Marketing Analytics")
    class MarketingAnalyticsTests {

        @Test
        @DisplayName("GET /api/analytics/marketing-attribution - Should return marketing attribution")
        @WithMockUser(roles = "OWNER")
        void shouldGetMarketingAttribution() throws Exception {
            MarketingAttributionDTO attribution = new MarketingAttributionDTO(
                "Google Ads",
                "Summer Campaign",
                200L,
                new BigDecimal("25000.00"),
                new BigDecimal("125.00"),
                150L,
                5.0,
                "Google Ads-Summer Campaign"
            );
            when(analyticsFacade.getMarketingAttribution(any(), any()))
                .thenReturn(List.of(attribution));

            mockMvc.perform(get("/api/v1/owner/analytics/marketing/attribution")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].source").value("Google Ads"))
                .andExpect(jsonPath("$[0].conversionRate").value(5.0));
        }
    }

    @Nested
    @DisplayName("Authorization Tests")
    class AuthorizationTests {

        @Test
        @DisplayName("Should deny access without authentication")
        void shouldDenyAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should deny access for CUSTOMER role")
        @WithMockUser(roles = "CUSTOMER")
        void shouldDenyAccessForCustomer() throws Exception {
            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow access for EMPLOYEE role")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldAllowAccessForEmployee() throws Exception {
            when(analyticsFacade.getBestSellingProducts(anyInt(), any(), any()))
                .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow access for OWNER role")
        @WithMockUser(roles = "OWNER")
        void shouldAllowAccessForOwner() throws Exception {
            when(analyticsFacade.getBestSellingProducts(anyInt(), any(), any()))
                .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/owner/analytics/products/best-selling")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "10"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Tests")
    class HttpStatusCodeTests {

        @Test
        @DisplayName("Successful requests should return explicit 200 OK")
        @WithMockUser(roles = "OWNER")
        void successfulRequestsShouldReturn200() throws Exception {
            when(analyticsFacade.getDailySales(any(), any()))
                .thenReturn(List.of());

            mockMvc.perform(get("/api/v1/owner/analytics/sales/daily")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(status().reason(org.hamcrest.Matchers.nullValue()));
        }

        @Test
        @DisplayName("All analytics endpoints should use explicit status codes")
        @WithMockUser(roles = "OWNER")
        void allEndpointsShouldUseExplicitStatusCodes() throws Exception {
            // Test multiple endpoints
            when(analyticsFacade.getTopCustomers(anyInt(), any(), any())).thenReturn(List.of());
            when(analyticsFacade.getLowStockProducts(anyInt())).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/owner/analytics/customers/top-buyers")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString())
                    .param("limit", "20"))
                .andExpect(status().isOk());

            mockMvc.perform(get("/api/v1/owner/analytics/inventory/low-stock")
                    .param("threshold", "10")
                    .param("limit", "50"))
                .andExpect(status().isOk());
        }
    }
}

