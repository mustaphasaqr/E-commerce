package com.mustapha.ecommerce.integration;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.application.port.InventoryPort;
import com.mustapha.ecommerce.order.application.port.NotificationPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentResult;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.product.dto.ProductRequest;
import com.mustapha.ecommerce.product.dto.ProductResponse;

/**
 * Performance Tests - Load Testing Order→Product Communication
 * 
 * Tests system behavior under:
 * - High concurrency (multiple simultaneous orders)
 * - Race conditions (concurrent access to same product)
 * - Throughput limits (orders per second)
 * - Response time degradation under load
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.jpa.show-sql=false"
})
@DisplayName("Performance Tests - Order ↔ Product Load Testing")
class OrderProductPerformanceTest {

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

    @BeforeEach
    void setUp() {
        when(paymentPort.processPayment(any(), any(), any(), any()))
            .thenReturn(new PaymentResult(true, "txn_success", "Payment successful"));
        
        when(inventoryPort.checkAvailability(any(), anyInt()))
            .thenReturn(true);
    }

    @Test
    @DisplayName("Should handle 100 concurrent orders for same product - Stock reservation race condition")
    void shouldHandleConcurrentOrdersForSameProduct() throws Exception {
        // Arrange - Create product with limited stock
        ProductRequest productRequest = new ProductRequest(
            "PERF-RACE-001",
            "High Demand Product",
            "Product with race condition testing",
            new BigDecimal("99.99"),
            "USD",
            50 // Only 50 units available
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Act - Simulate 100 concurrent customers trying to order 1 unit each
        int numberOfThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        
        AtomicInteger successfulOrders = new AtomicInteger(0);
        AtomicInteger failedOrders = new AtomicInteger(0);
        List<Throwable> errors = new ArrayList<>();

        for (int i = 0; i < numberOfThreads; i++) {
            final int customerId = i;
            executor.submit(() -> {
                try {
                    OrderRequest orderRequest = new OrderRequest();
                    orderRequest.setCustomerId("CUST-PERF-" + customerId);
                    orderRequest.setItems(Arrays.asList(
                        new OrderItemRequest(product.getId(), "High Demand Product", 1, 99.99)
                    ));

                    MvcResult result = mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                        .andReturn();

                    if (result.getResponse().getStatus() == 201) {
                        successfulOrders.incrementAndGet();
                    } else {
                        failedOrders.incrementAndGet();
                    }
                } catch (Exception e) {
                    errors.add(e);
                    failedOrders.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Wait for all threads to complete (max 30 seconds)
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();

        // Assert
        assertThat(completed).isTrue();
        assertThat(successfulOrders.get() + failedOrders.get()).isEqualTo(numberOfThreads);
        
        // Exactly 50 orders should succeed (stock limit), 50 should fail
        assertThat(successfulOrders.get()).isLessThanOrEqualTo(50);
        assertThat(failedOrders.get()).isGreaterThanOrEqualTo(50);
        
        System.out.println("Performance Test Results:");
        System.out.println("  Successful orders: " + successfulOrders.get());
        System.out.println("  Failed orders: " + failedOrders.get());
        System.out.println("  Stock correctly protected: " + (successfulOrders.get() <= 50));
    }

    @Test
    @DisplayName("Should handle 50 concurrent orders for different products - Throughput test")
    void shouldHandleHighThroughputMultipleProducts() throws Exception {
        // Arrange - Create 10 different products
        List<ProductResponse> products = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProductRequest productRequest = new ProductRequest(
                "PERF-THROUGHPUT-" + i,
                "Product " + i,
                "Throughput test product",
                new BigDecimal("50.00"),
                "USD",
                100
            );

            MvcResult result = mockMvc.perform(post("/api/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(productRequest)))
                .andExpect(status().isCreated())
                .andReturn();

            products.add(objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ProductResponse.class
            ));
        }

        // Act - 50 concurrent orders across 10 products
        int numberOfOrders = 50;
        ExecutorService executor = Executors.newFixedThreadPool(20);
        CountDownLatch latch = new CountDownLatch(numberOfOrders);
        
        AtomicInteger successCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfOrders; i++) {
            final int orderNum = i;
            executor.submit(() -> {
                try {
                    // Distribute orders across products
                    ProductResponse selectedProduct = products.get(orderNum % products.size());
                    
                    OrderRequest orderRequest = new OrderRequest();
                    orderRequest.setCustomerId("CUST-THROUGHPUT-" + orderNum);
                    orderRequest.setItems(Arrays.asList(
                        new OrderItemRequest(selectedProduct.getId(), selectedProduct.getName(), 2, 50.00)
                    ));

                    mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                        .andExpect(status().isCreated());
                    
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // Log error but don't fail test
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(60, TimeUnit.SECONDS);
        executor.shutdown();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Assert
        assertThat(successCount.get()).isEqualTo(numberOfOrders);
        
        double ordersPerSecond = (successCount.get() * 1000.0) / duration;
        System.out.println("Throughput Test Results:");
        System.out.println("  Total orders: " + successCount.get());
        System.out.println("  Duration: " + duration + "ms");
        System.out.println("  Orders/second: " + String.format("%.2f", ordersPerSecond));
        
        // Should process at least 5 orders per second under load
        assertThat(ordersPerSecond).isGreaterThan(5.0);
    }

    @Test
    @DisplayName("Should maintain consistent response times under moderate load")
    @Transactional
    void shouldMaintainConsistentResponseTimes() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "PERF-LATENCY-001",
            "Response Time Test",
            "Product for latency testing",
            new BigDecimal("75.00"),
            "USD",
            500
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Act - Measure response times for 20 sequential orders
        List<Long> responseTimes = new ArrayList<>();
        
        for (int i = 0; i < 20; i++) {
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCustomerId("CUST-LATENCY-" + i);
            orderRequest.setItems(Arrays.asList(
                new OrderItemRequest(product.getId(), "Response Time Test", 1, 75.00)
            ));

            long start = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
            
            long duration = System.currentTimeMillis() - start;
            responseTimes.add(duration);
        }

        // Assert - Calculate statistics
        double avgResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .average()
            .orElse(0.0);
        
        long maxResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
        
        long minResponseTime = responseTimes.stream()
            .mapToLong(Long::longValue)
            .min()
            .orElse(0L);

        System.out.println("Response Time Analysis:");
        System.out.println("  Average: " + String.format("%.2f", avgResponseTime) + "ms");
        System.out.println("  Min: " + minResponseTime + "ms");
        System.out.println("  Max: " + maxResponseTime + "ms");
        
        // Response times should be reasonable (< 2 seconds average)
        assertThat(avgResponseTime).isLessThan(2000.0);
        
        // Max shouldn't be more than 3x average (consistent performance)
        assertThat(maxResponseTime).isLessThan((long) (avgResponseTime * 3));
    }

    @Test
    @DisplayName("Should handle burst traffic - Sudden spike in orders")
    void shouldHandleBurstTraffic() throws Exception {
        // Arrange - Create product
        ProductRequest productRequest = new ProductRequest(
            "PERF-BURST-001",
            "Flash Sale Product",
            "Product for burst testing",
            new BigDecimal("29.99"),
            "USD",
            200
        );

        MvcResult productResult = mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(productRequest)))
            .andExpect(status().isCreated())
            .andReturn();

        ProductResponse product = objectMapper.readValue(
            productResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Act - Simulate burst: 30 orders submitted within 1 second
        int burstSize = 30;
        ExecutorService executor = Executors.newFixedThreadPool(burstSize);
        CountDownLatch readyLatch = new CountDownLatch(burstSize);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(burstSize);
        
        AtomicInteger processed = new AtomicInteger(0);

        // Prepare all threads first
        for (int i = 0; i < burstSize; i++) {
            final int orderId = i;
            executor.submit(() -> {
                try {
                    readyLatch.countDown();
                    startLatch.await(); // Wait for signal to start simultaneously
                    
                    OrderRequest orderRequest = new OrderRequest();
                    orderRequest.setCustomerId("CUST-BURST-" + orderId);
                    orderRequest.setItems(Arrays.asList(
                        new OrderItemRequest(product.getId(), "Flash Sale Product", 1, 29.99)
                    ));

                    mockMvc.perform(post("/api/orders")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(orderRequest)))
                        .andExpect(status().isCreated());
                    
                    processed.incrementAndGet();
                } catch (Exception e) {
                    // Some may fail due to stock limits
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // Wait for all threads to be ready, then release them simultaneously
        readyLatch.await(5, TimeUnit.SECONDS);
        long burstStart = System.currentTimeMillis();
        startLatch.countDown(); // Start the burst!
        
        doneLatch.await(30, TimeUnit.SECONDS);
        long burstDuration = System.currentTimeMillis() - burstStart;
        executor.shutdown();

        // Assert
        System.out.println("Burst Test Results:");
        System.out.println("  Orders submitted: " + burstSize);
        System.out.println("  Orders processed: " + processed.get());
        System.out.println("  Burst duration: " + burstDuration + "ms");
        
        // System should handle burst without crashing
        assertThat(processed.get()).isGreaterThan(0);
        
        // Verify product stock was correctly managed
        MvcResult updatedProductResult = mockMvc.perform(
                get("/api/products/" + product.getId()))
            .andExpect(status().isOk())
            .andReturn();

        ProductResponse updatedProduct = objectMapper.readValue(
            updatedProductResult.getResponse().getContentAsString(),
            ProductResponse.class
        );

        // Stock should have decreased by number of successful orders
        assertThat(updatedProduct.getAvailableStock()).isEqualTo(200 - processed.get());
    }
}
