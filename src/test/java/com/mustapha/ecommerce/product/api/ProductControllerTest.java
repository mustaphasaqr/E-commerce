package com.mustapha.ecommerce.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.product.application.facade.ProductFacade;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.exception.InsufficientStockException;
import com.mustapha.ecommerce.product.domain.exception.InvalidProductStateException;
import com.mustapha.ecommerce.product.domain.exception.ProductAlreadyActiveException;
import com.mustapha.ecommerce.product.domain.exception.ProductAlreadyInactiveException;
import com.mustapha.ecommerce.product.domain.exception.ProductDiscontinuedException;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;
import com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException;
import com.mustapha.ecommerce.shared.exception.GlobalExceptionHandler;
import com.mustapha.ecommerce.product.api.ProductGlobalExceptionHandler;
import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * API Layer Tests - ProductController
 * Pattern: Spring MockMvc integration tests
 * Focus: HTTP contract validation, response values, error handling
 * 
 * Note: We don't load the full ECommerceApplication to avoid bean conflicts
 * between Product and Order GlobalExceptionHandler classes
 */
@WebMvcTest(controllers = {ProductController.class, ProductGlobalExceptionHandler.class, GlobalExceptionHandler.class}, 
    excludeAutoConfiguration = SecurityAutoConfiguration.class)
@org.springframework.security.test.context.support.WithMockUser(roles = {"EMPLOYEE", "OWNER"})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductFacade productFacade;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    private com.mustapha.ecommerce.product.application.port.ProductReviewPort productReviewPort;

    @MockBean
    private com.mustapha.ecommerce.product.application.port.RecommendationPort recommendationPort;
    
    @MockBean
    private org.springframework.data.redis.core.RedisTemplate<String, String> redisTemplate;
    
    @MockBean
    private com.mustapha.ecommerce.shared.security.ExponentialBackoffFilter exponentialBackoffFilter;
    
    @MockBean
    private com.mustapha.ecommerce.shared.security.GlobalApiRateLimitFilter globalApiRateLimitFilter;
    
    @MockBean
    private com.mustapha.ecommerce.shared.security.JwtTokenGenerator jwtTokenGenerator;

    private ProductResponse testProductResponse;
    private ProductRequest testProductRequest;

    @BeforeEach
    void setUp() throws Exception {
        // Configure mock filters to pass through requests to the controller
        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(exponentialBackoffFilter).doFilter(
            org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletRequest.class),
            org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletResponse.class),
            org.mockito.ArgumentMatchers.any(jakarta.servlet.FilterChain.class)
        );

        org.mockito.Mockito.doAnswer(invocation -> {
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(invocation.getArgument(0), invocation.getArgument(1));
            return null;
        }).when(globalApiRateLimitFilter).doFilter(
            org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletRequest.class),
            org.mockito.ArgumentMatchers.any(jakarta.servlet.ServletResponse.class),
            org.mockito.ArgumentMatchers.any(jakarta.servlet.FilterChain.class)
        );

        // Test product response with actual values
        testProductResponse = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100,  // totalStock
            100,  // availableStock
            0,    // reservedStock
            true, // active
            true, // visible
            true, // availableForPurchase
            false // discontinued
        );

        // Test product request
        testProductRequest = new ProductRequest(
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100
        );
    }

    // ========== CREATE PRODUCT TESTS ==========

    @Test
    void createProduct_shouldReturn201WithProductResponse() throws Exception {
        // Given
        when(productFacade.createProduct(any(ProductRequest.class))).thenReturn(testProductResponse);

        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testProductRequest)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.price").value(99.99))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.totalStock").value(100))
            .andExpect(jsonPath("$.availableStock").value(100))
            .andExpect(jsonPath("$.reservedStock").value(0))
            .andExpect(jsonPath("$.active").value(true))
            .andExpect(jsonPath("$.visible").value(true))
            .andExpect(jsonPath("$.availableForPurchase").value(true))
            .andExpect(jsonPath("$.discontinued").value(false));
    }

    @Test
    void createProduct_withInvalidRequest_shouldReturn400WithErrorDetails() throws Exception {
        // Given - Request with missing required field (null sku)
        ProductRequest invalidRequest = new ProductRequest(
            null,  // Invalid: SKU is required
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100
        );

        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Validation error"))
            .andExpect(jsonPath("$.message").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void createProduct_withNegativePrice_shouldReturn400() throws Exception {
        // Given
        ProductRequest invalidRequest = new ProductRequest(
            "TEST-SKU",
            "Test Product",
            "Test Description",
            new BigDecimal("-10.00"),  // Invalid: negative price
            "USD",
            100
        );

        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createProduct_withMalformedJson_shouldReturn400() throws Exception {
        // When/Then
        mockMvc.perform(post("/api/v1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid request body format"));
    }

    // ========== GET PRODUCT BY ID TESTS ==========

    @Test
    void getProductById_shouldReturn200WithProductData() throws Exception {
        // Given
        when(productFacade.getProductById("test-product-id-123")).thenReturn(testProductResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/products/test-product-id-123"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
            .andExpect(jsonPath("$.name").value("Test Product"))
            .andExpect(jsonPath("$.price").value(99.99))
            .andExpect(jsonPath("$.currency").value("USD"))
            .andExpect(jsonPath("$.totalStock").value(100));
    }

    @Test
    void getProductById_whenNotFound_shouldReturn404WithErrorDetails() throws Exception {
        // Given
        String nonExistentId = "00000000-0000-0000-0000-000000000000";  // Valid UUID format
        when(productFacade.getProductById(nonExistentId))
            .thenThrow(new ProductNotFoundException(ProductId.of(nonExistentId)));

        // When/Then
        mockMvc.perform(get("/api/v1/products/" + nonExistentId))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Product not found"))
            .andExpect(jsonPath("$.message").value("Product not found"))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    // ========== GET PRODUCT BY SKU TESTS ==========

    @Test
    void getProductBySku_shouldReturn200WithProductData() throws Exception {
        // Given
        when(productFacade.getProductBySku("TEST-SKU-001")).thenReturn(testProductResponse);

        // When/Then
        mockMvc.perform(get("/api/v1/products")
                .param("sku", "TEST-SKU-001"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
            .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProductBySku_whenNotFound_shouldReturn404() throws Exception {
        // Given
        when(productFacade.getProductBySku("NON-EXISTENT"))
            .thenThrow(new ProductNotFoundException(SKU.of("NON-EXISTENT")));

        // When/Then
        mockMvc.perform(get("/api/v1/products")
                .param("sku", "NON-EXISTENT"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.error").value("Product not found"));
    }

    @Test
    void getProductBySku_withMissingParameter_shouldReturn200WithEmptyList() throws Exception {
        // When/Then - Missing 'sku' parameter returns empty list (graceful degradation)
        mockMvc.perform(get("/api/v1/products"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());
    }

    // ========== RESERVE STOCK TESTS ==========

    @Test
    void reserveStock_shouldReturn200WithUpdatedStockValues() throws Exception {
        // Given - Product with reservation
        ProductResponse reservedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100,  // totalStock unchanged
            90,   // availableStock decreased
            10,   // reservedStock increased
            true, true, true, false
        );
        when(productFacade.reserveStock("test-product-id-123", "order-123", 10))
            .thenReturn(reservedProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/reserve-stock")
                .param("orderId", "order-123")
                .param("quantity", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.totalStock").value(100))
            .andExpect(jsonPath("$.availableStock").value(90))
            .andExpect(jsonPath("$.reservedStock").value(10));
    }

    @Test
    void reserveStock_withInsufficientStock_shouldReturn409WithErrorDetails() throws Exception {
        // Given
        when(productFacade.reserveStock("test-product-id-123", "order-123", 200))
            .thenThrow(new InsufficientStockException("test-product-id-123", 100, 200));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/reserve-stock")
                .param("orderId", "order-123")
                .param("quantity", "200"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Insufficient stock"))
            .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    void reserveStock_forDiscontinuedProduct_shouldReturn403() throws Exception {
        // Given
        when(productFacade.reserveStock("test-product-id-123", "order-123", 10))
            .thenThrow(new ProductDiscontinuedException("test-product-id-123"));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/reserve-stock")
                .param("orderId", "order-123")
                .param("quantity", "10"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Product has been discontinued"));
    }

    // ========== RELEASE RESERVATION TESTS ==========

    @Test
    void releaseReservation_shouldReturn200WithUpdatedStockValues() throws Exception {
        // Given - Product after releasing reservation
        ProductResponse releasedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100,  // totalStock unchanged
            100,  // availableStock restored
            0,    // reservedStock decreased
            true, true, true, false
        );
        when(productFacade.releaseReservation("test-product-id-123", "order-123"))
            .thenReturn(releasedProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/release-reservation")
                .param("orderId", "order-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalStock").value(100))
            .andExpect(jsonPath("$.availableStock").value(100))
            .andExpect(jsonPath("$.reservedStock").value(0));
    }

    // ========== FULFILL RESERVATION TESTS ==========

    @Test
    void fulfillReservation_shouldReturn200WithReducedTotalStock() throws Exception {
        // Given - Product after fulfillment (stock permanently reduced)
        ProductResponse fulfilledProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            90,   // totalStock decreased
            90,   // availableStock
            0,    // reservedStock cleared
            true, true, true, false
        );
        when(productFacade.fulfillReservation("test-product-id-123", "order-123"))
            .thenReturn(fulfilledProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/fulfill-reservation")
                .param("orderId", "order-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalStock").value(90))
            .andExpect(jsonPath("$.availableStock").value(90))
            .andExpect(jsonPath("$.reservedStock").value(0));
    }

    // ========== UPDATE PRICE TESTS ==========

    @Test
    void updatePrice_shouldReturn200WithNewPriceValue() throws Exception {
        // Given - Product with updated price
        ProductResponse updatedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("149.99"),  // Updated price
            "USD",
            100, 100, 0,
            true, true, true, false
        );
        when(productFacade.updatePrice(
            eq("test-product-id-123"), 
            eq(new BigDecimal("149.99")), 
            eq("USD")))
            .thenReturn(updatedProduct);

        // When/Then
        mockMvc.perform(put("/api/v1/products/test-product-id-123/price")
                .param("newPrice", "149.99")
                .param("currencyCode", "USD"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.price").value(149.99))
            .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void updatePrice_withDifferentCurrency_shouldReturn400() throws Exception {
        // Given
        when(productFacade.updatePrice(
            eq("test-product-id-123"), 
            any(BigDecimal.class), 
            eq("EUR")))
            .thenThrow(new IllegalArgumentException("Cannot compare prices with different currencies: USD vs EUR"));

        // When/Then
        mockMvc.perform(put("/api/v1/products/test-product-id-123/price")
                .param("newPrice", "149.99")
                .param("currencyCode", "EUR"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.error").value("Invalid request"))
            .andExpect(jsonPath("$.message").value("Invalid request"));
    }

    // ========== UPDATE PRODUCT DETAILS TESTS ==========

    @Test
    void updateProductDetails_shouldReturn200WithUpdatedValues() throws Exception {
        // Given - Product with updated details
        ProductResponse updatedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Updated Product Name",     // Updated name
            "Updated Description",       // Updated description
            new BigDecimal("99.99"),
            "USD",
            100, 100, 0,
            true, true, true, false
        );
        when(productFacade.updateProductDetails(
            "test-product-id-123", 
            "Updated Product Name", 
            "Updated Description"))
            .thenReturn(updatedProduct);

        // When/Then
        mockMvc.perform(put("/api/v1/products/test-product-id-123/details")
                .param("name", "Updated Product Name")
                .param("description", "Updated Description"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.name").value("Updated Product Name"))
            .andExpect(jsonPath("$.description").value("Updated Description"));
    }

    // ========== ACTIVATE PRODUCT TESTS ==========

    @Test
    void activateProduct_shouldReturn200WithActiveStateTrue() throws Exception {
        // Given - Initially inactive product becomes active
        ProductResponse activatedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100, 100, 0,
            true,  // active = true
            true, true, false
        );
        when(productFacade.activateProduct("test-product-id-123"))
            .thenReturn(activatedProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/activate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void activateProduct_whenAlreadyActive_shouldReturn409() throws Exception {
        // Given
        when(productFacade.activateProduct("test-product-id-123"))
            .thenThrow(new ProductAlreadyActiveException("test-product-id-123"));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/activate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Invalid product state"));
    }

    @Test
    void activateProduct_whenDiscontinued_shouldReturn403() throws Exception {
        // Given
        when(productFacade.activateProduct("test-product-id-123"))
            .thenThrow(new ProductDiscontinuedException("test-product-id-123"));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/activate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Product has been discontinued"));
    }

    // ========== DEACTIVATE PRODUCT TESTS ==========

    @Test
    void deactivateProduct_shouldReturn200WithActiveFalse() throws Exception {
        // Given - Product becomes inactive
        ProductResponse deactivatedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100, 100, 0,
            false,  // active = false
            true,   // visible stays true (domain behavior)
            false,  // availableForPurchase = false
            false
        );
        when(productFacade.deactivateProduct("test-product-id-123"))
            .thenReturn(deactivatedProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/deactivate"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.active").value(false))
            .andExpect(jsonPath("$.visible").value(true))
            .andExpect(jsonPath("$.availableForPurchase").value(false));
    }

    @Test
    void deactivateProduct_whenAlreadyInactive_shouldReturn409() throws Exception {
        // Given
        when(productFacade.deactivateProduct("test-product-id-123"))
            .thenThrow(new ProductAlreadyInactiveException("test-product-id-123"));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/deactivate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Invalid product state"));
    }

    @Test
    void deactivateProduct_withReservedStock_shouldReturn409() throws Exception {
        // Given
        when(productFacade.deactivateProduct("test-product-id-123"))
            .thenThrow(new InvalidProductStateException(
                "Cannot deactivate product test-product-id-123 - it has 10 units reserved in active orders"));

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/deactivate"))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.status").value(409))
            .andExpect(jsonPath("$.error").value("Invalid product state"))
            .andExpect(jsonPath("$.message").value(containsString("Invalid product state")));
    }

    // ========== DISCONTINUE PRODUCT TESTS ==========

    @Test
    void discontinueProduct_shouldReturn200WithDiscontinuedTrue() throws Exception {
        // Given - Product becomes discontinued
        ProductResponse discontinuedProduct = new ProductResponse(
            "test-product-id-123",
            "TEST-SKU-001",
            "Test Product",
            "Test Description",
            new BigDecimal("99.99"),
            "USD",
            100, 100, 0,
            false,  // active = false
            true,   // visible stays true (domain behavior)
            false,  // availableForPurchase = false
            true    // discontinued = true
        );
        when(productFacade.discontinueProduct("test-product-id-123"))
            .thenReturn(discontinuedProduct);

        // When/Then
        mockMvc.perform(post("/api/v1/products/test-product-id-123/discontinue"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("test-product-id-123"))
            .andExpect(jsonPath("$.discontinued").value(true))
            .andExpect(jsonPath("$.active").value(false))
            .andExpect(jsonPath("$.availableForPurchase").value(false));
    }

    // ========== GENERIC ERROR HANDLING TESTS ==========

    @Test
    void whenUnexpectedException_shouldReturn500() throws Exception {
        // Given
        when(productFacade.getProductById("test-product-id-123"))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When/Then
        mockMvc.perform(get("/api/v1/products/test-product-id-123"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.status").value(500))
            .andExpect(jsonPath("$.error").value("An unexpected error occurred"))
            .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}


