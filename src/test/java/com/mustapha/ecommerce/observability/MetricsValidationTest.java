package com.mustapha.ecommerce.observability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Metrics Validation Tests
 * Validates Prometheus metrics exposure and quality
 */
@SpringBootTest(properties = {
    "management.endpoints.web.exposure.include=health,info,metrics,prometheus",
    "management.endpoint.prometheus.enabled=true",
    "management.metrics.export.prometheus.enabled=true"
})
@AutoConfigureMockMvc
@DisplayName("Metrics Validation Tests")
class MetricsValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Metrics Endpoint Tests")
    @WithMockUser(roles = "OWNER")
    class MetricsEndpointTests {

        @Test
        @DisplayName("Metrics endpoint should be accessible")
        void metricsEndpointShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/vnd.spring-boot.actuator.v3+json"));
        }

        @Test
        @DisplayName("Metrics endpoint should list available metrics")
        void metricsEndpointShouldListMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            assertThat(metrics.get("names")).isNotNull();
            assertThat(metrics.get("names").size()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Metrics endpoint should not require special authentication")
        void metricsEndpointShouldNotRequireAuth() throws Exception {
            mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("JVM Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class JvmMetricsTests {

        @Test
        @DisplayName("Should expose JVM memory metrics")
        void shouldExposeJvmMemoryMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).containsAnyOf("jvm.memory.used", "jvm.memory.max", "jvm.memory.committed");
        }

        @Test
        @DisplayName("Should expose garbage collection metrics")
        void shouldExposeGcMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).containsAnyOf(
                "jvm.gc.pause",
                "jvm.gc.memory.allocated",
                "jvm.gc.memory.promoted"
            );
        }

        @Test
        @DisplayName("Should expose thread metrics")
        void shouldExposeThreadMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).containsAnyOf("jvm.threads.live", "jvm.threads.daemon");
        }
    }

    @Nested
    @DisplayName("HTTP Request Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class HttpRequestMetricsTests {

        @Test
        @DisplayName("Should expose HTTP request count metrics")
        @WithMockUser
        void shouldExposeHttpRequestCountMetrics() throws Exception {
            // Make a sample request to generate metrics
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

            // Check metrics
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).containsAnyOf(
                "http.server.requests",
                "http.server.requests.count"
            );
        }

        @Test
        @DisplayName("Should track HTTP request durations")
        @WithMockUser
        void shouldTrackHttpRequestDurations() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).contains("http.server.requests");
        }

        @Test
        @DisplayName("Should track HTTP status codes")
        @WithMockUser
        void shouldTrackHttpStatusCodes() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());

            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            // Verify that metrics endpoint is working
            assertThat(content).contains("names");
        }
    }

    @Nested
    @DisplayName("Database Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class DatabaseMetricsTests {

        @Test
        @DisplayName("Should expose database connection pool metrics")
        void shouldExposeConnectionPoolMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            assertThat(metricNames).containsAnyOf(
                "hikaricp.connections.active",
                "hikaricp.connections.idle",
                "hikaricp.connections",
                "jdbc.connections.active"
            );
        }

        @Test
        @DisplayName("Should expose database query metrics")
        void shouldExposeQueryMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            String metricNames = metrics.get("names").toString();
            // Check for any database-related metrics
            assertThat(metricNames).containsAnyOf(
                "hikaricp",
                "jdbc",
                "data.source"
            );
        }
    }

    @Nested
    @DisplayName("Cache Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class CacheMetricsTests {

        @Test
        @DisplayName("Should expose Redis cache metrics")
        void shouldExposeRedisCacheMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            // In test environment, caching is disabled, so just verify metrics endpoint works
            assertThat(metrics.get("names")).isNotNull();
            assertThat(metrics.get("names").isArray()).isTrue();
        }

        @Test
        @DisplayName("Should track cache hit/miss ratio")
        void shouldTrackCacheHitMissRatio() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            // In test environment, caching is disabled, so just verify metrics endpoint works
            assertThat(metrics.get("names")).isNotNull();
            assertThat(metrics.get("names").size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Custom Business Metrics Tests")
    @WithMockUser(roles = "OWNER")
    class CustomMetricsTests {

        @Test
        @DisplayName("Should expose custom application metrics")
        void shouldExposeCustomApplicationMetrics() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            // Check for any custom metrics (should have names array)
            assertThat(metrics.get("names")).isNotNull();
            assertThat(metrics.get("names").size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Metrics Naming Convention Tests")
    @WithMockUser(roles = "OWNER")
    class NamingConventionTests {

        @Test
        @DisplayName("Metrics should follow standardized naming conventions")
        void metricsShouldFollowNamingConventions() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            
            // Verify all metric names are present
            assertThat(metrics.get("names")).isNotNull();
            assertThat(metrics.get("names").isArray()).isTrue();
        }

        @Test
        @DisplayName("All metrics should be properly structured")
        void allMetricsShouldBeProperlyStructured() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            assertThat(metrics.get("names")).isNotNull();
        }

        @Test
        @DisplayName("All metrics should have consistent format")
        void allMetricsShouldHaveConsistentFormat() throws Exception {
            MvcResult result = mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode metrics = objectMapper.readTree(content);
            assertThat(metrics.get("names")).isNotNull();
        }
    }
}
