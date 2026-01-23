package com.mustapha.ecommerce.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.application.exception.ProductNotFoundException;
import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentResult;
import com.mustapha.ecommerce.order.application.port.ProductPort;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.product.application.facade.ProductFacade;

/**
 * Chaos Engineering Tests - System Resilience
 * 
 * Tests system behavior when:
 * - Product module is temporarily unavailable
 * - Network timeouts occur
 * - Partial failures (some products available, some not)
 * - Cascading failures
 * - Recovery after failure
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
@Transactional
@DisplayName("Chaos Tests - Order ↔ Product Resilience")
class OrderProductChaosTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentPort paymentPort;

    @MockBean
    private InventoryPort inventoryPort;

    @MockBean
    private NotificationPort notificationPort;

    @SpyBean
    private ProductPort productPort;

    @SpyBean
    private ProductFacade productFacade;

    @BeforeEach
    void setUp() {
        when(paymentPort.processPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentResult(true, "txn_success", "Payment successful"));
        
        when(inventoryPort.checkAvailability(any(), anyInt()))
            .thenReturn(true);
    }

    @Test
    @DisplayName("Chaos: Product module temporarily unavailable - Should fail gracefully")
    void shouldHandleProductModuleUnavailable() throws Exception {
        // Arrange - Simulate Product module down (throws exception)
        doThrow(new RuntimeException("Product module unavailable"))
            .when(productPort).productExists(any(ProductId.class));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-001");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-001", "Test Product", 1, 99.99)
        ));

        // Act & Assert - Should fail with appropriate error
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().is5xxServerError());

        // Verify payment was NOT processed (circuit breaker behavior)
        verify(paymentPort, never()).processPayment(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Chaos: Intermittent Product failures - Retry mechanism test")
    void shouldHandleIntermittentProductFailures() throws Exception {
        // Arrange - Simulate intermittent failures (fail twice, then succeed)
        AtomicInteger attemptCount = new AtomicInteger(0);
        
        doAnswer(invocation -> {
            int attempt = attemptCount.incrementAndGet();
            if (attempt <= 2) {
                throw new RuntimeException("Temporary network glitch");
            }
            return true; // Succeed on 3rd attempt
        }).when(productPort).productExists(any(ProductId.class));

        doReturn(new Money(new BigDecimal("99.99")))
            .when(productPort).getProductPrice(any(ProductId.class));
        doReturn(100).when(productPort).getAvailableStock(any(ProductId.class));
        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-002");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-002", "Intermittent Product", 1, 99.99)
        ));

        // Act - First 2 attempts should fail, could implement retry logic
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().is5xxServerError());

        // Note: In real-world, implement retry mechanism in ProductServiceAdapter
        // For now, we verify the failure is detected
        assertThat(attemptCount.get()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Chaos: Partial Product availability - Some products down, others working")
    void shouldHandlePartialProductAvailability() throws Exception {
        // Arrange - Product PROD-CHAOS-003 is available, PROD-CHAOS-004 is down
        doAnswer(invocation -> {
            ProductId productId = invocation.getArgument(0);
            if ("PROD-CHAOS-004".equals(productId.getValue())) {
                throw new ProductNotFoundException(productId);
            }
            return true;
        }).when(productPort).productExists(any(ProductId.class));

        doAnswer(invocation -> {
            ProductId productId = invocation.getArgument(0);
            if ("PROD-CHAOS-004".equals(productId.getValue())) {
                throw new ProductNotFoundException(productId);
            }
            return new Money(new BigDecimal("50.00"));
        }).when(productPort).getProductPrice(any(ProductId.class));

        doReturn(100).when(productPort).getAvailableStock(any(ProductId.class));
        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));

        // Act - Order with unavailable product should fail
        OrderRequest failedOrderRequest = new OrderRequest();
        failedOrderRequest.setCustomerId("CUST-CHAOS-003");
        failedOrderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-003", "Available Product", 1, 50.00),
            new OrderItemRequest("PROD-CHAOS-004", "Unavailable Product", 1, 50.00)
        ));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(failedOrderRequest)))
            .andExpect(status().is4xxClientError());

        // Act - Order with only available products should succeed
        OrderRequest successOrderRequest = new OrderRequest();
        successOrderRequest.setCustomerId("CUST-CHAOS-004");
        successOrderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-003", "Available Product", 2, 50.00)
        ));

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(successOrderRequest)))
            .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Chaos: Price service degradation - Stale prices vs current prices")
    void shouldDetectPriceMismatch() throws Exception {
        // Arrange - ProductPort returns different price than expected (stale cache scenario)
        doReturn(true).when(productPort).productExists(any(ProductId.class));
        doReturn(new Money(new BigDecimal("120.00"))) // Actual price is 120
            .when(productPort).getProductPrice(any(ProductId.class));
        doReturn(100).when(productPort).getAvailableStock(any(ProductId.class));
        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-005");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-005", "Price Mismatch Product", 1, 99.99) // Old price
        ));

        // Act & Assert - Should detect price mismatch and reject
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isBadRequest());

        // Payment should not be processed due to price validation failure
        verify(paymentPort, never()).processPayment(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Chaos: Stock reservation failure after order creation")
    void shouldHandleStockReservationFailure() throws Exception {
        // Arrange - Stock reservation fails after order is saved
        doReturn(true).when(productPort).productExists(any(ProductId.class));
        doReturn(new Money(new BigDecimal("75.00")))
            .when(productPort).getProductPrice(any(ProductId.class));
        doReturn(50).when(productPort).getAvailableStock(any(ProductId.class));
        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));
        
        // Stock reservation will fail
        doThrow(new RuntimeException("Stock reservation system unavailable"))
            .when(productPort).reserveStock(any(ProductId.class), anyString(), anyInt());

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-006");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-006", "Reservation Fail Product", 2, 75.00)
        ));

        // Act & Assert - Should fail with 500 error (transaction should rollback)
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().is5xxServerError());

        // Note: In production with @Transactional, order should NOT be saved
        // Stock reservation failure should trigger rollback
    }

    @Test
    @DisplayName("Chaos: Recovery after Product module comes back online")
    void shouldRecoverAfterProductModuleRecovers() throws Exception {
        // Arrange - First call fails, subsequent calls succeed (module recovery)
        AtomicInteger callCount = new AtomicInteger(0);
        
        doAnswer(invocation -> {
            if (callCount.incrementAndGet() == 1) {
                throw new RuntimeException("Service temporarily unavailable");
            }
            return true;
        }).when(productPort).productExists(any(ProductId.class));

        doReturn(new Money(new BigDecimal("60.00")))
            .when(productPort).getProductPrice(any(ProductId.class));
        doReturn(100).when(productPort).getAvailableStock(any(ProductId.class));
        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-007");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-007", "Recovery Test Product", 1, 60.00)
        ));

        // Act - First attempt fails
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().is5xxServerError());

        // Second attempt succeeds (module recovered)
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated());

        // Assert - System recovered successfully
        assertThat(callCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Chaos: Timeout simulation - Slow Product module response")
    void shouldHandleSlowProductResponses() throws Exception {
        // Arrange - Simulate slow Product module (delay in response)
        doAnswer(invocation -> {
            Thread.sleep(100); // Simulate 100ms delay
            return true;
        }).when(productPort).productExists(any(ProductId.class));

        doAnswer(invocation -> {
            Thread.sleep(100);
            return new Money(new BigDecimal("45.00"));
        }).when(productPort).getProductPrice(any(ProductId.class));

        doAnswer(invocation -> {
            Thread.sleep(100);
            return 100;
        }).when(productPort).getAvailableStock(any(ProductId.class));

        doReturn(false).when(productPort).isDiscontinued(any(ProductId.class));

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId("CUST-CHAOS-008");
        orderRequest.setItems(Arrays.asList(
            new OrderItemRequest("PROD-CHAOS-008", "Slow Product", 1, 45.00)
        ));

        // Act - Measure response time
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(orderRequest)))
            .andExpect(status().isCreated());

        long duration = System.currentTimeMillis() - startTime;

        // Assert - Should complete despite slowness (but might want timeout in production)
        System.out.println("Slow response handled in " + duration + "ms");
        assertThat(duration).isGreaterThan(300); // At least 3 calls * 100ms delay
        
        // Note: In production, consider implementing timeout circuit breakers
    }
}
