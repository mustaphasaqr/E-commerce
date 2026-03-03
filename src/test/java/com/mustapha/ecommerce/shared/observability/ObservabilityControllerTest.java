package com.mustapha.ecommerce.shared.observability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Observability Controller Tests
 * Tests all observability endpoints for monitoring and metrics
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Observability Controller Tests")
class ObservabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Business Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class BusinessMetricsTests {

        @Test
        @DisplayName("Should return business metrics summary")
        void shouldReturnBusinessMetricsSummary() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders").exists())
                .andExpect(jsonPath("$.payments").exists())
                .andExpect(jsonPath("$.shipping").exists())
                .andExpect(jsonPath("$.inventory").exists())
                .andExpect(jsonPath("$.products").exists())
                .andExpect(jsonPath("$.reviews").exists())
                .andExpect(jsonPath("$.fraud").exists());
        }

        @Test
        @DisplayName("Should include order metrics")
        void shouldIncludeOrderMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orders.created").isNumber())
                .andExpect(jsonPath("$.orders.completed").isNumber())
                .andExpect(jsonPath("$.orders.cancelled").isNumber())
                .andExpect(jsonPath("$.orders.failed").isNumber())
                .andExpect(jsonPath("$.orders.averageProcessingTime").exists());
        }

        @Test
        @DisplayName("Should include payment metrics")
        void shouldIncludePaymentMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payments.successful").isNumber())
                .andExpect(jsonPath("$.payments.failed").isNumber())
                .andExpect(jsonPath("$.payments.timeout").isNumber())
                .andExpect(jsonPath("$.payments.averageProcessingTime").exists());
        }

        @Test
        @DisplayName("Should include product metrics")
        void shouldIncludeProductMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/business"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products.searches").isNumber())
                .andExpect(jsonPath("$.products.viewed").isNumber())
                .andExpect(jsonPath("$.products.addedToCart").isNumber());
        }
    }

    @Nested
    @DisplayName("System Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class SystemMetricsTests {

        @Test
        @DisplayName("Should return system metrics")
        void shouldReturnSystemMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.threads").exists())
                .andExpect(jsonPath("$.cpu").exists())
                .andExpect(jsonPath("$.uptimeMillis").exists());
        }

        @Test
        @DisplayName("Should include memory metrics")
        void shouldIncludeMemoryMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memory.totalMemoryMB").isNumber())
                .andExpect(jsonPath("$.memory.freeMemoryMB").isNumber())
                .andExpect(jsonPath("$.memory.usedMemoryMB").isNumber())
                .andExpect(jsonPath("$.memory.maxMemoryMB").isNumber())
                .andExpect(jsonPath("$.memory.memoryUsagePercent").isNumber());
        }

        @Test
        @DisplayName("Should include thread metrics")
        void shouldIncludeThreadMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.threads.activeCount").isNumber())
                .andExpect(jsonPath("$.threads.activeCount").value(greaterThan(0)));
        }

        @Test
        @DisplayName("Should include CPU metrics")
        void shouldIncludeCpuMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cpu.availableProcessors").isNumber())
                .andExpect(jsonPath("$.cpu.availableProcessors").value(greaterThan(0)));
        }
    }

    @Nested
    @DisplayName("HTTP Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class HttpMetricsTests {

        @Test
        @DisplayName("Should return HTTP metrics")
        void shouldReturnHttpMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/http"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.requests").isArray());
        }
    }

    @Nested
    @DisplayName("Database Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class DatabaseMetricsTests {

        @Test
        @DisplayName("Should return database metrics")
        void shouldReturnDatabaseMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthy").isBoolean());
        }

        @Test
        @DisplayName("Database should be healthy")
        void databaseShouldBeHealthy() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/database"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthy").value(true));
        }
    }

    @Nested
    @DisplayName("Cache Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class CacheMetricsTests {

        @Test
        @DisplayName("Should return cache metrics")
        void shouldReturnCacheMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redisHealthy").isBoolean())
                .andExpect(jsonPath("$.cacheStatistics").isArray());
        }

        @Test
        @DisplayName("Redis should be healthy")
        void redisShouldBeHealthy() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/cache"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.redisHealthy").value(true));
        }
    }

    @Nested
    @DisplayName("Health Summary Tests")
    @WithMockUser(roles = "OWNER")
    class HealthSummaryTests {

        @Test
        @DisplayName("Should return health summary")
        void shouldReturnHealthSummary() throws Exception {
            mockMvc.perform(get("/api/observability/health/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andExpect(jsonPath("$.components").exists())
                .andExpect(jsonPath("$.uptimeSeconds").isNumber());
        }

        @Test
        @DisplayName("Should include component health status")
        void shouldIncludeComponentHealthStatus() throws Exception {
            mockMvc.perform(get("/api/observability/health/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.database").exists())
                .andExpect(jsonPath("$.components.redis").exists())
                .andExpect(jsonPath("$.components.diskSpace").exists())
                .andExpect(jsonPath("$.components.jvm").exists());
        }

        @Test
        @DisplayName("Application should be UP")
        void applicationShouldBeUp() throws Exception {
            mockMvc.perform(get("/api/observability/health/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", anyOf(is("UP"), is("DEGRADED"))));
        }
    }

    @Nested
    @DisplayName("Performance Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class PerformanceMetricsTests {

        @Test
        @DisplayName("Should return performance metrics")
        void shouldReturnPerformanceMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRequests").isNumber())
                .andExpect(jsonPath("$.requestsPerSecond").isNumber())
                .andExpect(jsonPath("$.garbageCollection").exists());
        }

        @Test
        @DisplayName("Should include response time metrics")
        void shouldIncludeResponseTimeMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageResponseTimeMs").exists())
                .andExpect(jsonPath("$.orderProcessingTimeMs").exists())
                .andExpect(jsonPath("$.paymentProcessingTimeMs").exists());
        }

        @Test
        @DisplayName("Should include garbage collection metrics")
        void shouldIncludeGarbageCollectionMetrics() throws Exception {
            mockMvc.perform(get("/api/observability/metrics/performance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.garbageCollection.pauseTimeMs").exists());
        }
    }
}
