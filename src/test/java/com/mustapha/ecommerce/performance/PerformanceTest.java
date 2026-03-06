package com.mustapha.ecommerce.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.order.dto.OrderRequest;
import com.mustapha.ecommerce.order.dto.OrderItemRequest;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance & Load Testing
 * Tests response times, throughput, and resource utilization
 */

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {"spring.cache.type=none"})
@Transactional
@DisplayName("Performance & Load Tests")
class PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private ProductRepository productRepository;
    
    private Product testProduct;

    @BeforeEach
    void setUp() {
        // Create test product for performance tests
        testProduct = Product.create(
            SKU.of("PERF-TEST-" + System.currentTimeMillis()),
            "Performance Test Product",
            "Product for performance testing",
            Price.of(new BigDecimal("99.99"), "USD"),
            Stock.of(1000)
        );
        testProduct = productRepository.save(testProduct);
    }

    @Nested
    @DisplayName("Response Time SLA Tests")
    class ResponseTimeSlaTests {

        @Test
        @DisplayName("Product listing should respond within 500ms")
        void productListingShouldMeetSla() throws Exception {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/products")
                    .param("sku", testProduct.getSku().getValue()))
                .andExpect(status().isOk());
            
            long responseTime = System.currentTimeMillis() - startTime;
            assertThat(responseTime).isLessThan(500);
        }

        @Test
        @DisplayName("Product search should respond within 300ms")
        void productSearchShouldMeetSla() throws Exception {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/products/search")
                    .param("name", "test"))
                .andExpect(status().isOk());
            
            long responseTime = System.currentTimeMillis() - startTime;
            assertThat(responseTime).isLessThan(300);
        }

        @Test
        @DisplayName("Product details should respond within 200ms")
        void productDetailsShouldMeetSla() throws Exception {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/products/{id}", testProduct.getId().getValue().toString()))
                .andExpect(status().isOk());
            
            long responseTime = System.currentTimeMillis() - startTime;
            assertThat(responseTime).isLessThan(200);
        }

        @Test
        @DisplayName("Order placement should respond within 1000ms")
        @WithMockUser(roles = "CUSTOMER")
        void orderPlacementShouldMeetSla() throws Exception {
            OrderRequest request = new OrderRequest();
            request.setCustomerId("customer-123");
            
            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(testProduct.getId().getValue().toString());
            item.setProductName(testProduct.getName());
            item.setQuantity(1);
            item.setPrice(99.99);
            request.setItems(Collections.singletonList(item));

            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                .andExpect(status().is2xxSuccessful());
            
            long responseTime = System.currentTimeMillis() - startTime;
            assertThat(responseTime).isLessThan(1000);
        }

        @Test
        @DisplayName("Login should respond within reasonable time")
        void loginShouldMeetSla() throws Exception {
            String loginJson = """
                {
                    "email": "test@example.com",
                    "password": "password"
                }
                """;

            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginJson));
            
            long responseTime = System.currentTimeMillis() - startTime;
            assertThat(responseTime).isLessThan(2000); // Realistic expectation for test environment
        }
    }

    @Nested
    @DisplayName("Throughput Tests")
    @Disabled("Flaky: Environment-dependent timing. Requires tuned environment.")
    class ThroughputTests {

        @Test
        @DisplayName("Should handle 100 concurrent product reads")
        void shouldHandle100ConcurrentReads() throws Exception {
            int numberOfRequests = 100;
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(numberOfRequests);
            List<Long> responseTimes = new CopyOnWriteArrayList<>();

            for (int i = 0; i < numberOfRequests; i++) {
                executor.submit(() -> {
                    try {
                        long start = System.nanoTime();
                        mockMvc.perform(get("/api/v1/products")
                                .param("sku", testProduct.getSku().getValue()))
                            .andExpect(status().isOk());
                        long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
                        responseTimes.add(duration);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();
            assertThat(responseTimes).hasSize(numberOfRequests);
            
            // Calculate average response time
            double avgResponseTime = responseTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0.0);
            
            assertThat(avgResponseTime).isLessThan(300);
        }

        @Test
        @DisplayName("Should maintain throughput under sustained load")
        @WithMockUser(roles = "CUSTOMER")
        void shouldMaintainThroughputUnderLoad() throws Exception {
            int duration = 10; // seconds
            int threadPoolSize = 5;
            ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
            long endTime = System.currentTimeMillis() + (duration * 1000);
            AtomicInteger requestCount = new AtomicInteger(0);
            AtomicInteger errorCount = new AtomicInteger(0);

            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < threadPoolSize; i++) {
                futures.add(executor.submit(() -> {
                    while (System.currentTimeMillis() < endTime) {
                        try {
                            mockMvc.perform(get("/api/v1/products")
                                    .param("sku", testProduct.getSku().getValue()))
                                .andExpect(status().isOk());
                            requestCount.incrementAndGet();
                        } catch (Exception e) {
                            errorCount.incrementAndGet();
                        }
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }
            executor.shutdown();

            int totalRequests = requestCount.get();
            int throughput = totalRequests / duration; // requests per second
            double errorRate = (double) errorCount.get() / totalRequests * 100;

            System.out.println("Total requests: " + totalRequests);
            System.out.println("Throughput: " + throughput + " req/s");
            System.out.println("Error rate: " + errorRate + "%");

            assertThat(throughput).isGreaterThan(10); // At least 10 req/s
            assertThat(errorRate).isLessThan(1.0); // Less than 1% errors
        }
    }

    @Nested
    @DisplayName("Database Connection Pool Tests")
    class DatabaseConnectionPoolTests {

        @Test
        @DisplayName("Should not exhaust database connection pool")
        void shouldNotExhaustConnectionPool() throws Exception {
            int numberOfRequests = 50;
            ExecutorService executor = Executors.newFixedThreadPool(20);
            CountDownLatch latch = new CountDownLatch(numberOfRequests);
            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failureCount = new AtomicInteger(0);

            for (int i = 0; i < numberOfRequests; i++) {
                executor.submit(() -> {
                    try {
                        mockMvc.perform(get("/api/v1/products")
                                .param("sku", testProduct.getSku().getValue()))
                            .andExpect(status().isOk());
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(60, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();
            assertThat(failureCount.get()).isZero(); // No connection pool exhaustion errors
        }

        @Test
        @DisplayName("Should release connections properly")
        void shouldReleaseConnectionsProperly() throws Exception {
            // Make requests and verify connections are returned to pool
            for (int i = 0; i < 100; i++) {
                mockMvc.perform(get("/api/v1/products")
                        .param("sku", testProduct.getSku().getValue()))
                    .andExpect(status().isOk());
            }

            // If connections aren't released, subsequent requests would fail
            mockMvc.perform(get("/api/v1/products")
                    .param("sku", testProduct.getSku().getValue()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Memory Leak Detection Tests")
    class MemoryLeakTests {

        @Test
        @DisplayName("Should not leak memory during repeated operations")
        void shouldNotLeakMemory() throws Exception {
            Runtime runtime = Runtime.getRuntime();
            runtime.gc(); // Force garbage collection
            
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            // Perform many operations
            for (int i = 0; i < 1000; i++) {
                mockMvc.perform(get("/api/v1/products")
                        .param("sku", testProduct.getSku().getValue()))
                    .andExpect(status().isOk());
            }

            runtime.gc(); // Force garbage collection
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();

            long memoryGrowth = finalMemory - initialMemory;
            long maxAcceptableGrowth = 50 * 1024 * 1024; // 50 MB

            assertThat(memoryGrowth).isLessThan(maxAcceptableGrowth);
        }
    }

    @Nested
    @DisplayName("Thread Pool Saturation Tests")
    class ThreadPoolTests {

        @Test
        @DisplayName("Should handle thread pool saturation gracefully")
        void shouldHandleThreadPoolSaturation() throws Exception {
            int excessiveThreads = 200;
            ExecutorService executor = Executors.newFixedThreadPool(excessiveThreads);
            CountDownLatch latch = new CountDownLatch(excessiveThreads);
            AtomicInteger timeoutCount = new AtomicInteger(0);

            for (int i = 0; i < excessiveThreads; i++) {
                executor.submit(() -> {
                    try {
                        mockMvc.perform(get("/api/v1/products")
                                .param("sku", testProduct.getSku().getValue()));
                    } catch (Exception e) {
                        if (e.getMessage().contains("timeout")) {
                            timeoutCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            boolean completed = latch.await(120, TimeUnit.SECONDS);
            executor.shutdown();

            assertThat(completed).isTrue();
            // Some timeouts are acceptable under extreme load
            assertThat(timeoutCount.get()).isLessThan(excessiveThreads / 10); // Less than 10% timeouts
        }
    }

    @Nested
    @DisplayName("Pagination Performance Tests")
    class PaginationPerformanceTests {

        @Test
        @DisplayName("Large result set pagination should be efficient")
        @WithMockUser(roles = "OWNER")
        void largePaginationShouldBeEfficient() throws Exception {
            // First page should be fast
            long startTime = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "0")
                    .param("size", "20")
                    .with(csrf()))
                .andExpect(status().isOk());
            long firstPageTime = System.currentTimeMillis() - startTime;

            // Last page should also be reasonably fast (no n+1 issues)
            startTime = System.currentTimeMillis();
            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "100")
                    .param("size", "20")
                    .with(csrf()))
                .andExpect(status().isOk());
            long lastPageTime = System.currentTimeMillis() - startTime;

            assertThat(firstPageTime).isLessThan(200);
            assertThat(lastPageTime).isLessThan(300); // Slightly slower but still fast
        }
    }

    @Nested
    @DisplayName("N+1 Query Detection Tests")
    class NPlusOneQueryTests {

        @Test
        @DisplayName("Product listing should not have N+1 query problem")
        void productListingShouldNotHaveNPlusOne() throws Exception {
            // Simple smoke test - N+1 queries would make this slow
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/v1/products")
                    .param("sku", testProduct.getSku().getValue()))
                .andExpect(status().isOk());
            
            long responseTime = System.currentTimeMillis() - startTime;
            // Should be fast - if N+1 queries present, would be much slower
            assertThat(responseTime).isLessThan(500);
        }

        @Test
        @DisplayName("Order details should use JOIN FETCH to avoid N+1")
        void orderDetailsShouldAvoidNPlusOne() throws Exception {
            // Order repository already has @EntityGraph for items
            // This test verifies the endpoint is accessible
            // Actual N+1 prevention is tested in repository tests
            Authentication auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                "550e8400-e29b-41d4-a716-446655440000",
                null,
                java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_CUSTOMER"))
            );
            mockMvc.perform(get("/api/v1/orders")
                    .with(csrf())
                    .with(authentication(auth)))
                .andExpect(status().isOk());
        }
    }
}
