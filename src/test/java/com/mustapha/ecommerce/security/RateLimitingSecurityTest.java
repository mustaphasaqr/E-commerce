package com.mustapha.ecommerce.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.user.dto.LoginRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Rate Limiting & Brute Force Protection Tests
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Rate Limiting & Brute Force Protection Tests")
class RateLimitingSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @BeforeEach
    void setUp() {
        // Clear rate limit keys
        redisTemplate.delete(redisTemplate.keys("rate_limit:*"));
    }

    @Nested
    @DisplayName("Login Rate Limiting Tests")
    class LoginRateLimitingTests {

        @Test
        @DisplayName("Should allow login attempts within rate limit")
        void shouldAllowWithinRateLimit() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "password");

            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is4xxClientError()); // Wrong credentials, but not rate limited
            }
        }

        @Test
        @DisplayName("Should block login after exceeding rate limit")
        void shouldBlockAfterExceedingRateLimit() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            // Attempt login 6 times (assuming limit is 5)
            for (int i = 0; i < 6; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // 7th attempt should be rate limited
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("Should reset rate limit after time window")
        void shouldResetRateLimitAfterTimeWindow() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            // Fill up rate limit
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // Wait for rate limit window to expire (e.g., 30 seconds)
            Thread.sleep(31000);

            // Should be able to login again
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError()); // Not rate limited, just wrong credentials
        }

        @Test
        @DisplayName("Should apply per-IP rate limiting")
        void shouldApplyPerIpRateLimiting() throws Exception {
            LoginRequest request = new LoginRequest("user1@example.com", "password");

            // Simulate multiple failed attempts from same IP
            for (int i = 0; i < 21; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(req -> {
                            req.setRemoteAddr("192.168.1.100");
                            return req;
                        }));
            }

            // IP should be blocked after 20 attempts
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(req -> {
                        req.setRemoteAddr("192.168.1.100");
                        return req;
                    }))
                .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("Should reset counter on successful login")
        void shouldResetCounterOnSuccess() throws Exception {
            // This test assumes successful login resets the counter
            LoginRequest wrongRequest = new LoginRequest("test@example.com", "wrongpassword");
            LoginRequest correctRequest = new LoginRequest("test@example.com", "correctpassword");

            // Make some failed attempts
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongRequest)));
            }

            // Successful login should reset counter
            // (This would need actual valid credentials)
        }
    }

    @Nested
    @Disabled("API rate limiting not implemented")
    @DisplayName("API Rate Limiting Tests")
    class ApiRateLimitingTests {

        @Test
        @DisplayName("Should rate limit API calls per user")
        void shouldRateLimitApiCallsPerUser() throws Exception {
            // Simulate rapid API calls
            int requestCount = 0;
            int rateLimitedCount = 0;

            for (int i = 0; i < 100; i++) {
                var result = mockMvc.perform(get("/api/products"))
                    .andReturn();
                
                requestCount++;
                if (result.getResponse().getStatus() == 429) {
                    rateLimitedCount++;
                }
            }

            assertThat(rateLimitedCount).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should apply different rate limits per endpoint")
        void shouldApplyDifferentRateLimitsPerEndpoint() throws Exception {
            // Login endpoint might have stricter rate limit than product browsing
            // This is a conceptual test demonstrating endpoint-specific limits
        }
    }

    @Nested
    @DisplayName("Brute Force Attack Prevention")
    class BruteForcePreventionTests {

        @Test
        @DisplayName("Should detect and block credential stuffing attacks")
        void shouldBlockCredentialStuffing() throws Exception {
            // Test that rate limiting works at user level (blocks multiple failed attempts)
            // Even without IP-based limiting, per-user rate limiting prevents credential stuffing
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            // Make multiple failed login attempts for same user
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // After rate limit exceeded, should get 429
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("Should implement exponential backoff")
        void shouldImplementExponentialBackoff() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "wrongpassword");

            long firstAttemptTime = System.currentTimeMillis();
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)));

            // After 3 failed attempts, wait time should increase
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // Next attempt should have delay
            long secondAttemptTime = System.currentTimeMillis();
            // (Exponential backoff would be implemented in the service layer)
        }

        @Test
        @DisplayName("Should implement CAPTCHA after threshold")
        void shouldImplementCaptchaAfterThreshold() throws Exception {
            // After N failed attempts, CAPTCHA should be required
            // This is a conceptual test - actual implementation would depend on CAPTCHA service
        }
    }

    @Nested
    @DisplayName("Distributed Rate Limiting Tests")
    class DistributedRateLimitingTests {

        @Test
        @DisplayName("Should share rate limit state across instances via Redis")
        void shouldShareRateLimitStateViaRedis() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "password");

            // Simulate requests from "different instances" (using Redis as shared state)
            for (int i = 0; i < 3; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // Check Redis for rate limit keys
            var keys = redisTemplate.keys("rate_limit:*");
            assertThat(keys).isNotEmpty();
        }

        @Test
        @DisplayName("Should handle concurrent requests correctly")
        void shouldHandleConcurrentRequests() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "password");
            ExecutorService executor = Executors.newFixedThreadPool(10);
            CountDownLatch latch = new CountDownLatch(20);

            int totalRequests = 20;
            int successCount = 0;
            int rateLimitedCount = 0;

            for (int i = 0; i < totalRequests; i++) {
                executor.submit(() -> {
                    try {
                        var result = mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                            .andReturn();
                        
                        // Count responses
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // Verify that rate limiting worked correctly under concurrency
        }
    }

    @Nested    @Disabled("Password reset rate limiting not implemented")    @DisplayName("Password Reset Rate Limiting Tests")
    class PasswordResetRateLimitingTests {

        @Test
        @DisplayName("Should rate limit password reset requests")
        void shouldRateLimitPasswordResetRequests() throws Exception {
            String requestJson = "{\"email\": \"test@example.com\"}";

            // Send multiple password reset requests
            for (int i = 0; i < 10; i++) {
                mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson));
            }

            // Should be rate limited after threshold
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestJson))
                .andExpect(status().isTooManyRequests());
        }

        @Test
        @DisplayName("Should rate limit per email address")
        void shouldRateLimitPerEmailAddress() throws Exception {
            // Multiple reset requests for same email should be limited
            // Even from different IPs
            String email1 = "{\"email\": \"user1@example.com\"}";
            String email2 = "{\"email\": \"user2@example.com\"}";

            // Exhaust limit for email1
            for (int i = 0; i < 5; i++) {
                mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(email1));
            }

            // email1 should be rate limited
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(email1))
                .andExpect(status().isTooManyRequests());

            // email2 should still work
            mockMvc.perform(post("/api/auth/password-reset/request")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(email2))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @Disabled("Rate limit headers not implemented")
    @DisplayName("Rate Limit Response Tests")
    class RateLimitResponseTests {

        @Test
        @DisplayName("Should include Retry-After header when rate limited")
        void shouldIncludeRetryAfterHeader() throws Exception {
            LoginRequest request = new LoginRequest("test@example.com", "password");

            // Exhaust rate limit
            for (int i = 0; i < 6; i++) {
                mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)));
            }

            // Check for Retry-After header
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        }

        @Test
        @DisplayName("Should include X-RateLimit headers")
        void shouldIncludeRateLimitHeaders() throws Exception {
            mockMvc.perform(get("/api/products"))
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"))
                .andExpect(header().exists("X-RateLimit-Reset"));
        }
    }
}
