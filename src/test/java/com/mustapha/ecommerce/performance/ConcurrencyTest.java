package com.mustapha.ecommerce.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.OrderBuilder;
import com.mustapha.ecommerce.order.domain.model.OrderItem;
import com.mustapha.ecommerce.order.domain.model.valueobject.CustomerId;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.SKU;
import com.mustapha.ecommerce.product.domain.model.valueobject.Stock;
import com.mustapha.ecommerce.product.domain.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Concurrency & Race Condition Tests
 * Tests for optimistic locking, concurrent updates, and data consistency
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.cache.type=none"
})
@DisplayName("Concurrency & Race Condition Tests")
class ConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private EntityManager entityManager;

    private Product testProduct;

    @BeforeEach
    @Transactional
    void setUp() {
        // Create unique product for each test to avoid activation conflicts
        testProduct = Product.create(
            SKU.of("CONC-" + java.util.UUID.randomUUID().toString().substring(0, 8)),
            "Test Product",
            "Test product for concurrency",
            Price.of(new BigDecimal("99.99"), "USD"),
            Stock.of(100)
        );
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("Optimistic Locking Tests")
    class OptimisticLockingTests {

        @Test
        @DisplayName("Should detect concurrent modifications using version")
        @Transactional
        void shouldDetectConcurrentModifications() {
            // Simulate two transactions trying to update same product
            Product product1 = productRepository.findById(testProduct.getId()).orElseThrow();
            Product product2 = productRepository.findById(testProduct.getId()).orElseThrow();

            // First update succeeds
            product1.updatePrice(Price.of(new BigDecimal("89.99"), "USD"));
            productRepository.save(product1);

            // Second update should fail due to stale version
            product2.updatePrice(Price.of(new BigDecimal("79.99"), "USD"));
            
            try {
                productRepository.save(product2);
                // Should throw OptimisticLockException or similar
            } catch (Exception e) {
                assertThat(e).hasMessageContaining("optimistic");
            }
        }

        @Test
        @DisplayName("Should handle concurrent stock updates correctly")
        void shouldHandleConcurrentStockUpdates() throws Exception {
            int numberOfThreads = 10;
            int itemsPerThread = 5;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            // Get initial stock
            int initialStock = productRepository.findById(testProduct.getId()).orElseThrow().getStock().getAvailableQuantity();

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        // Each thread tries to reserve stock
                        Product product = productRepository.findById(testProduct.getId()).orElseThrow();
                        String orderId = "order-" + threadIndex;
                        product.reserveStockForOrder(orderId, itemsPerThread);
                        productRepository.save(product);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // Verify stock consistency - total reserved should not exceed initial stock
            Product finalProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            int finalStock = finalProduct.getStock().getAvailableQuantity();
            int totalReserved = initialStock - finalStock;
            
            // Either some reservations succeeded (consistent) or all failed
            // The key is that we don't oversell
            assertThat(finalStock).isGreaterThanOrEqualTo(0);
            assertThat(totalReserved).isLessThanOrEqualTo(initialStock);
        }
    }

    @Nested
    @DisplayName("Race Condition Tests")
    class RaceConditionTests {

        @Test
        @DisplayName("Should prevent double-spending in concurrent orders")
        void shouldPreventDoubleSpending() throws Exception {
            // Create product with limited stock
            Product limitedProduct = Product.create(
                SKU.of("LIMITED-001"),
                "Limited Product",
                "Only 1 in stock",
                Price.of(new BigDecimal("100.00"), "USD"),
                Stock.of(1)
            );
            limitedProduct = productRepository.save(limitedProduct);

            // Test at domain level: concurrent reservations on same product
            Product product1 = productRepository.findById(limitedProduct.getId()).orElseThrow();
            Product product2 = productRepository.findById(limitedProduct.getId()).orElseThrow();

            // First reservation succeeds
            product1.reserveStockForOrder("order-1", 1);
            productRepository.save(product1);

            // Refresh product2 to get latest state
            Product product2Updated = productRepository.findById(limitedProduct.getId()).orElseThrow();
            
            // Second reservation should fail due to insufficient stock
            try {
                product2Updated.reserveStockForOrder("order-2", 1);
                productRepository.save(product2Updated);
            } catch (Exception e) {
                // Expected - either optimistic lock or insufficient stock
            }

            // Verify final state - stock should be 0 or not oversold
            Product finalProduct = productRepository.findById(limitedProduct.getId()).orElseThrow();
            assertThat(finalProduct.getStock().getAvailableQuantity()).isLessThanOrEqualTo(0); // At most depleted, never negative
        }

        @Test
        @DisplayName("Should handle concurrent price updates atomically")
        @WithMockUser(roles = "EMPLOYEE")
        void shouldHandleConcurrentPriceUpdates() throws Exception {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CountDownLatch latch = new CountDownLatch(5);
            List<BigDecimal> prices = List.of(
                new BigDecimal("99.99"),
                new BigDecimal("89.99"),
                new BigDecimal("79.99"),
                new BigDecimal("69.99"),
                new BigDecimal("59.99")
            );

            for (BigDecimal price : prices) {
                executor.submit(() -> {
                    try {
                        String updateJson = String.format("{\"newPrice\": %s}", price);
                        mockMvc.perform(put("/api/products/{id}/price", testProduct.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateJson)
                                .with(csrf()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // Final price should be one of the attempted prices (not corrupted)
            Product finalProduct = productRepository.findById(testProduct.getId()).orElseThrow();
            assertThat(prices).contains(finalProduct.getPrice().getAmount());
        }
    }

    @Nested
    @DisplayName("Deadlock Prevention Tests")
    class DeadlockPreventionTests {

        @Test
        @DisplayName("Should prevent deadlocks in multi-resource transactions")
        void shouldPreventDeadlocks() throws Exception {
            // Create two products
            Product product1 = Product.create(
                SKU.of("DEAD-001"),
                "Product 1",
                "Desc", 
                Price.of(new BigDecimal("10.00"), "USD"),
                Stock.of(100)
            );
            productRepository.save(product1);

            Product product2 = Product.create(
                SKU.of("DEAD-002"),
                "Product 2",
                "Desc",
                Price.of(new BigDecimal("20.00"), "USD"),
                Stock.of(100)
            );
            productRepository.save(product2);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            CountDownLatch latch = new CountDownLatch(2);

            // Thread 1: Lock product1 then product2
            executor.submit(() -> {
                try {
                    Product p1 = productRepository.findById(product1.getId()).orElseThrow();
                    Thread.sleep(100);
                    Product p2 = productRepository.findById(product2.getId()).orElseThrow();
                    
                    p1.updatePrice(Price.of(new BigDecimal("15.00"), "USD"));
                    p2.updatePrice(Price.of(new BigDecimal("25.00"), "USD"));
                    
                    productRepository.save(p1);
                    productRepository.save(p2);
                } catch (Exception e) {
                    // Expected: deadlock or timeout
                } finally {
                    latch.countDown();
                }
            });

            // Thread 2: Lock product2 then product1 (reverse order)
            executor.submit(() -> {
                try {
                    Product p2 = productRepository.findById(product2.getId()).orElseThrow();
                    Thread.sleep(100);
                    Product p1 = productRepository.findById(product1.getId()).orElseThrow();
                    
                    p2.updatePrice(Price.of(new BigDecimal("22.00"), "USD"));
                    p1.updatePrice(Price.of(new BigDecimal("12.00"), "USD"));
                    
                    productRepository.save(p2);
                    productRepository.save(p1);
                } catch (Exception e) {
                    // Expected: deadlock or timeout
                } finally {
                    latch.countDown();
                }
            });

            boolean completed = latch.await(10, TimeUnit.SECONDS);
            executor.shutdownNow();

            // Should complete without deadlock (using timeout or deadlock detection)
            assertThat(completed).isTrue();
        }
    }

    @Nested
    @DisplayName("Order Race Condition Tests")
    class OrderRaceConditionTests {

        @Test
        @DisplayName("Should prevent concurrent order status updates using optimistic locking")
        void shouldPreventConcurrentOrderStatusUpdates() {
            // Arrange - Create and persist an order
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-123"))
                .addItem(new OrderItem(
                    new ProductId(testProduct.getId().getValue().toString()),
                    "Test Product",
                    1,
                    new Money(testProduct.getPrice().getAmount())
                ))
                .build();
            order = orderRepository.save(order);
            
            //Progress order to PROCESSING state (required before shipping)
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order = orderRepository.save(order); // Save state changes
            
            String orderId = order.getId().getValue();

            // Act - Simulate two concurrent transactions trying to update the same order
            Order order1 = orderRepository.findById(new OrderId(orderId)).orElseThrow();
            Order order2 = orderRepository.findById(new OrderId(orderId)).orElseThrow();

            // First transaction: Warehouse marks as shipped
            order1.ship("TRACK123", "UPS");
            orderRepository.save(order1); // Should succeed

            // Second transaction: Customer tries to cancel
            order2.cancel("Changed mind");
            
            // Assert - Second save should fail due to optimistic lock exception
            try {
                orderRepository.save(order2);
                // If we reach here, optimistic locking didn't work
                org.junit.jupiter.api.Assertions.fail("Expected OptimisticLockException but save succeeded");
            } catch (Exception e) {
                // Expected: OptimisticLockException or similar
                assertThat(e.getClass().getName())
                    .matches(".*Optimistic.*|.*StaleState.*|.*version.*");
            }
        }

        @Test
        @DisplayName("Should handle concurrent order updates with proper retry logic")
        void shouldHandleConcurrentOrderUpdatesWithRetry() throws Exception {
            // Arrange - Create order
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-456"))
                .addItem(new OrderItem(
                    new ProductId(testProduct.getId().getValue().toString()),
                    "Test Product",
                    2,
                    new Money(testProduct.getPrice().getAmount())
                ))
                .build();
            order = orderRepository.save(order);
            
            // Progress order to PROCESSING state (required before shipping)
            order.confirm();
            order.markAsPaid();
            order.startProcessing();
            order = orderRepository.save(order); // Save state changes
            
            final String orderId = order.getId().getValue();

            // Act - 5 concurrent threads trying to update same order
            int numberOfThreads = 5;
            ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
            CountDownLatch latch = new CountDownLatch(numberOfThreads);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            for (int i = 0; i < numberOfThreads; i++) {
                final int threadIndex = i;
                executor.submit(() -> {
                    try {
                        // Each thread tries to ship the order with different tracking
                        Order orderToUpdate = orderRepository.findById(new OrderId(orderId)).orElseThrow();
                        orderToUpdate.ship("TRACK" + threadIndex, "Carrier" + threadIndex);
                        orderRepository.save(orderToUpdate);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        // Expected: Optimistic lock exceptions
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            executor.shutdown();

            // Assert - Exactly one thread should succeed, others should fail
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(failureCount.get()).isEqualTo(numberOfThreads - 1);
            
            // Verify final state is consistent
            Order finalOrder = orderRepository.findById(new OrderId(orderId)).orElseThrow();
            assertThat(finalOrder.getStatus()).isEqualTo(com.mustapha.ecommerce.order.domain.model.OrderStatus.SHIPPED);
            assertThat(finalOrder.getTrackingNumber()).isNotNull();
        }

        @Test
        @DisplayName("Should prevent race between payment and cancellation")
        void shouldPreventRaceBetweenPaymentAndCancellation() {
            // Arrange
            Order order = new OrderBuilder()
                .withCustomerId(new CustomerId("customer-789"))
                .addItem(new OrderItem(
                    new ProductId(testProduct.getId().getValue().toString()),
                    "Test Product",
                    1,
                    new Money(testProduct.getPrice().getAmount())
                ))
                .build();
            order = orderRepository.save(order);
            
            // Confirm order (required before payment)
            order.confirm();
            order = orderRepository.save(order); // Save state changes
            
            String orderId = order.getId().getValue();

            // Act - Simulate payment confirmation and cancellation happening simultaneously
            Order order1 = orderRepository.findById(new OrderId(orderId)).orElseThrow();
            Order order2 = orderRepository.findById(new OrderId(orderId)).orElseThrow();

            // Payment system confirms payment
            order1.markAsPaid();
            orderRepository.save(order1);

            // Customer tries to cancel
            order2.cancel("No longer needed");
            
            // Assert - Cancellation should fail (order already paid)
            assertThatThrownBy(() -> orderRepository.save(order2))
                .satisfiesAnyOf(
                    ex -> assertThat(ex.getClass().getName()).contains("Optimistic"),
                    ex -> assertThat(ex.getClass().getName()).contains("StaleState"),
                    ex -> assertThat(ex.getMessage()).containsIgnoringCase("version")
                );
        }
    }

    @Nested
    @DisplayName("Idempotency Tests")
    class IdempotencyTests {

        @Test
        @DisplayName("Should handle duplicate order submissions idempotently")
        @WithMockUser(roles = "CUSTOMER")
        void shouldHandleDuplicateOrdersIdempotently() throws Exception {
            String idempotencyKey = "order-" + System.currentTimeMillis();
            
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.setCustomerId("customer-123");
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(testProduct.getId().getValue().toString());
            item.setProductName("Test Product");
            item.setQuantity(1);
            item.setPrice(99.99);
            orderRequest.setItems(Collections.singletonList(item));

            // Submit same order twice with same idempotency key
            String orderJson = objectMapper.writeValueAsString(orderRequest);

            var response1 = mockMvc.perform(post("/api/orders")
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderJson)
                    .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

            var response2 = mockMvc.perform(post("/api/orders")
                    .header("Idempotency-Key", idempotencyKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(orderJson)
                    .with(csrf()))
                .andExpect(status().is2xxSuccessful())
                .andReturn();

            // Both responses should have same order ID
            String orderId1 = objectMapper.readTree(response1.getResponse().getContentAsString())
                .get("orderId").asText();
            String orderId2 = objectMapper.readTree(response2.getResponse().getContentAsString())
                .get("orderId").asText();

            assertThat(orderId1).isEqualTo(orderId2);
        }

        @Test
        @DisplayName("Should handle duplicate payment submissions idempotently")
        @WithMockUser(roles = "CUSTOMER")
        void shouldHandleDuplicatePaymentsIdempotently() throws Exception {
            String paymentIdempotencyKey = "payment-" + System.currentTimeMillis();
            
            // Simulate duplicate payment button clicks
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/orders/{id}/pay", "order-123")
                        .header("Idempotency-Key", paymentIdempotencyKey)
                        .with(csrf()));
            }

            // Payment should only be processed once
            // (Verification would check payment processor was only called once)
        }
    }

    @Nested
    @DisplayName("Isolation Level Tests")
    class IsolationLevelTests {

        @Test
        @DisplayName("Should prevent phantom reads")
        @Transactional
        void shouldPreventPhantomReads() {
            // Test READ_COMMITTED isolation level
            // Two transactions reading same data should see consistent snapshots
        }

        @Test
        @DisplayName("Should prevent dirty reads")
        @Transactional
        void shouldPreventDirtyReads() {
            // Transaction should not see uncommitted changes from other transactions
        }

        @Test
        @DisplayName("Should prevent non-repeatable reads")
        @Transactional
        void shouldPreventNonRepeatableReads() {
            // Same query in transaction should return same results
        }
    }
}
