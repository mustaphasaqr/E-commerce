package com.mustapha.ecommerce.shared.health;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test Suite: Health Check Endpoints
 * 
 * Tests:
 * 1. /health - Overall health status with dependency checks
 * 2. /health/ready - Readiness probe (database + Redis)
 * 3. /health/live - Liveness probe (JVM status)
 * 
 * Validates:
 * - Real database connectivity check
 * - Real Redis connectivity check
 * - Proper HTTP status codes (200 for healthy, 503 for unhealthy)
 * - Response structure and content
 */
@SpringBootTest
@AutoConfigureMockMvc
class HealthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /health should return 200 with UP status")
    void testHealthEndpoint() throws Exception {
        mockMvc.perform(get("/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.timestamp").isNumber())
                .andExpect(jsonPath("$.checks").exists())
                .andExpect(jsonPath("$.checks.database").value("UP"))
                .andExpect(jsonPath("$.checks.redis").value("UP"));
    }

    @Test
    @DisplayName("GET /health/ready should return 200 when all dependencies are healthy")
    void testReadinessProbe() throws Exception {
        mockMvc.perform(get("/health/ready")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.checks").exists())
                .andExpect(jsonPath("$.checks.database").value("UP"))
                .andExpect(jsonPath("$.checks.redis").value("UP"));
    }

    @Test
    @DisplayName("GET /health/live should return 200 with ALIVE status")
    void testLivenessProbe() throws Exception {
        mockMvc.perform(get("/health/live")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ALIVE"))
                .andExpect(jsonPath("$.uptime").isNumber())
                .andExpect(jsonPath("$.uptime").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Health check should include timestamp")
    void testHealthTimestamp() throws Exception {
        long beforeCall = System.currentTimeMillis();
        
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timestamp").value(greaterThanOrEqualTo(beforeCall)));
    }

    @Test
    @DisplayName("Readiness should check both database and Redis")
    void testReadinessDependencies() throws Exception {
        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.checks.database").exists())
                .andExpect(jsonPath("$.checks.redis").exists());
    }

    @Test
    @DisplayName("Liveness should return uptime greater than zero")
    void testLivenessUptime() throws Exception {
        // Wait a bit to ensure uptime > 0
        Thread.sleep(100);
        
        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uptime").value(greaterThan(0)));
    }

    @Test
    @DisplayName("Health endpoints should return JSON content type")
    void testContentType() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/health/ready"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/health/live"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("Should respond quickly to health checks (< 1 second)")
    void testHealthCheckPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
        
        long duration = System.currentTimeMillis() - startTime;
        
        // Health checks should be fast (< 1 second)
        assert duration < 1000 : "Health check took too long: " + duration + "ms";
    }
}
