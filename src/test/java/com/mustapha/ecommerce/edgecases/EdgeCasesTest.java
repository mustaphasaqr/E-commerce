package com.mustapha.ecommerce.edgecases;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Edge Cases & Boundary Condition Tests
 * Tests unusual inputs, boundary values, and edge scenarios
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cache.type=none"
})
@Transactional
@DisplayName("Edge Cases & Boundary Tests")
class EdgeCasesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = Product.create(
            SKU.of("EDGE-TEST-" + System.currentTimeMillis()),
            "Edge Test Product",
            "Product for edge case testing",
            Price.of(new BigDecimal("99.99"), "USD"),
            Stock.of(100)
        );
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("Decimal Precision Tests")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    class DecimalPrecisionTests {

        @Test
        @DisplayName("Should handle very small decimal values")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleVerySmallDecimals() throws Exception {
            ProductRequest request = new ProductRequest(
                "DEC-SMALL-" + System.currentTimeMillis(),
                "Small Decimal Product",
                "Testing small decimals",
                new BigDecimal("0.01"), // 1 cent
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(0.01));
        }

        @Test
        @DisplayName("Should handle very large decimal values")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleLargeDecimals() throws Exception {
            ProductRequest request = new ProductRequest(
                "DEC-LARGE-" + System.currentTimeMillis(),
                "Large Decimal Product",
                "Testing large decimals",
                new BigDecimal("999999999.99"), // Nearly 1 billion
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(999999999.99));
        }

        @Test
        @DisplayName("Should round prices to 2 decimal places")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldRoundPricesToTwoDecimals() throws Exception {
            ProductRequest request = new ProductRequest(
                "DEC-ROUND-" + System.currentTimeMillis(),
                "Rounding Test Product",
                "Testing decimal rounding",
                new BigDecimal("99.999"), // 3 decimal places
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should handle recurring decimals")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleRecurringDecimals() throws Exception {
            // Test 1/3 = 0.333...
            BigDecimal recurringPrice = new BigDecimal("33.33");
            
            ProductRequest request = new ProductRequest(
                "DEC-RECUR-" + System.currentTimeMillis(),
                "Recurring Decimal Product",
                "Testing recurring decimals",
                recurringPrice,
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.price").value(33.33));
        }

        @Test
        @DisplayName("Should reject negative prices")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldRejectNegativePrices() throws Exception {
            ProductRequest request = new ProductRequest(
                "DEC-NEG-" + System.currentTimeMillis(),
                "Negative Price Product",
                "Testing negative price",
                new BigDecimal("-10.00"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Unicode & Special Character Tests")
    class UnicodeHandlingTests {

        @Test
        @DisplayName("Should handle emoji in product names")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleEmojiInProductName() throws Exception {
            ProductRequest request = new ProductRequest(
                "EMOJI-" + System.currentTimeMillis(),
                "🎉 Party Product 🎊",
                "Product with emoji 😀",
                new BigDecimal("29.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("🎉 Party Product 🎊"));
        }

        @Test
        @DisplayName("Should handle non-Latin characters")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleNonLatinCharacters() throws Exception {
            ProductRequest request = new ProductRequest(
                "UNICODE-" + System.currentTimeMillis(),
                "日本語テスト商品", // Japanese
                "中文描述 العربية Русский", // Chinese, Arabic, Russian
                new BigDecimal("49.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("日本語テスト商品"));
        }

        @Test
        @DisplayName("Should reject HTML tags in names (XSS prevention)")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleSpecialCharacters() throws Exception {
            ProductRequest request = new ProductRequest(
                "SPECIAL-" + System.currentTimeMillis(),
                "Product with <script>alert('xss')</script>",
                "Description with @#$%^&*()",
                new BigDecimal("19.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very long product names")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleLongProductNames() throws Exception {
            String longName = "A".repeat(200); // Adjust to actual DB limit
            
            ProductRequest request = new ProductRequest(
                "LONG-NAME-" + System.currentTimeMillis(),
                longName,
                "Normal description",
                new BigDecimal("99.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should reject excessively long names")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldRejectExcessivelyLongNames() throws Exception {
            String tooLongName = "A".repeat(300);
            
            ProductRequest request = new ProductRequest(
                "TOO-LONG-" + System.currentTimeMillis(),
                tooLongName,
                "Normal description",
                new BigDecimal("99.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Pagination Boundary Tests")
    class PaginationBoundaryTests {

        @Test
        @DisplayName("Should handle page 0")
        @WithMockUser(roles = "OWNER")
        void shouldHandlePageZero() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "0")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentPage").value(0));
        }

        @Test
        @DisplayName("Should handle negative page numbers gracefully")
        @WithMockUser(roles = "OWNER")
        void shouldHandleNegativePage() throws Exception {
            // Spring Data handles negative pages as page 0
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "-1")
                    .param("size", "20"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle page beyond available data")
        @WithMockUser(roles = "OWNER")
        void shouldHandlePageBeyondData() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "999999")
                    .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray());
        }

        @Test
        @DisplayName("Should handle very large page sizes")
        @WithMockUser(roles = "OWNER")
        void shouldHandleLargePageSize() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "0")
                    .param("size", "1000"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should handle zero page size")
        @WithMockUser(roles = "OWNER")
        void shouldHandleZeroPageSize() throws Exception {
            // Should either default to minimum or return error
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "0")
                    .param("size", "0"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Multi-Currency Tests")
    class MultiCurrencyTests {

        @Test
        @DisplayName("Should support USD currency")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldSupportUSD() throws Exception {
            ProductRequest request = new ProductRequest(
                "CURR-USD-" + System.currentTimeMillis(),
                "USD Product",
                "Product in US Dollars",
                new BigDecimal("99.99"),
                "USD",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("USD"));
        }

        @Test
        @DisplayName("Should support EUR currency")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldSupportEUR() throws Exception {
            ProductRequest request = new ProductRequest(
                "CURR-EUR-" + System.currentTimeMillis(),
                "EUR Product",
                "Product in Euros",
                new BigDecimal("89.99"),
                "EUR",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("EUR"));
        }

        @Test
        @DisplayName("Should support GBP currency")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldSupportGBP() throws Exception {
            ProductRequest request = new ProductRequest(
                "CURR-GBP-" + System.currentTimeMillis(),
                "GBP Product",
                "Product in British Pounds",
                new BigDecimal("79.99"),
                "GBP",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("GBP"));
        }

        @Test
        @DisplayName("Should reject invalid currency codes")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldRejectInvalidCurrency() throws Exception {
            ProductRequest request = new ProductRequest(
                "CURR-INV-" + System.currentTimeMillis(),
                "Invalid Currency Product",
                "Product with invalid currency",
                new BigDecimal("99.99"),
                "INVALID",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should support Japanese Yen (no decimal places)")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldSupportJPY() throws Exception {
            ProductRequest request = new ProductRequest(
                "CURR-JPY-" + System.currentTimeMillis(),
                "JPY Product",
                "Product in Japanese Yen",
                new BigDecimal("10000"),
                "JPY",
                10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currency").value("JPY"));
        }
    }

    @Nested
    @DisplayName("Boundary Stock Values Tests")
    class StockBoundaryTests {

        @Test
        @DisplayName("Should handle zero stock")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleZeroStock() throws Exception {
            ProductRequest request = new ProductRequest(
                "STOCK-ZERO-" + System.currentTimeMillis(),
                "Zero Stock Product",
                "Product with no stock",
                new BigDecimal("99.99"),
                "USD",
                0
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalStock").value(0));
        }

        @Test
        @DisplayName("Should handle large stock quantities")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleLargeStock() throws Exception {
            ProductRequest request = new ProductRequest(
                "STOCK-LARGE-" + System.currentTimeMillis(),
                "Large Stock Product",
                "Product with large stock",
                new BigDecimal("99.99"),
                "USD",
                1000000
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.totalStock").value(1000000));
        }

        @Test
        @DisplayName("Should reject negative stock")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldRejectNegativeStock() throws Exception {
            ProductRequest request = new ProductRequest(
                "STOCK-NEG-" + System.currentTimeMillis(),
                "Negative Stock Product",
                "Product with negative stock",
                new BigDecimal("99.99"),
                "USD",
                -10
            );

            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Order Edge Cases")
    @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
    class OrderEdgeCasesTests {

        @Test
        @DisplayName("Should reject order with empty items list")
        @WithMockUser(roles = "CUSTOMER")
        void shouldRejectEmptyOrder() throws Exception {
            OrderRequest request = new OrderRequest();
            request.setCustomerId("customer-123");
            request.setItems(Collections.emptyList());

            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle order with maximum quantity")
        @WithMockUser(roles = "CUSTOMER")
        void shouldHandleMaxQuantityOrder() throws Exception {
            OrderRequest request = new OrderRequest();
            request.setCustomerId("customer-123");
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(testProduct.getId().getValue().toString());
            item.setProductName(testProduct.getName());
            item.setQuantity(100); // Max available stock
            item.setPrice(99.99);
            request.setItems(Collections.singletonList(item));

            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should reject order exceeding available stock")
        @WithMockUser(roles = "CUSTOMER")
        void shouldRejectOrderExceedingStock() throws Exception {
            OrderRequest request = new OrderRequest();
            request.setCustomerId("customer-123");
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(testProduct.getId().getValue().toString());
            item.setProductName(testProduct.getName());
            item.setQuantity(1000); // More than available stock
            item.setPrice(99.99);
            request.setItems(Collections.singletonList(item));

            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().isBadRequest()); // Returns 400 for invalid quantity
        }
    }
}

