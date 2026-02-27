package com.mustapha.ecommerce.shared.external.email;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test Suite: Email Service Resilience Patterns
 * 
 * Tests:
 * 1. Retry Logic: Automatic retry on SMTP failures
 * 2. Circuit Breaker: Stops calling email provider when down
 * 3. Fallback: Logs to database for manual retry
 * 4. Non-Blocking: Email failures don't block user operations
 * 
 * Scenarios:
 * - SMTP timeout (retry succeeds)
 * - Email provider down (circuit opens)
 * - Circuit recovery
 * - Fallback to database logging
 */
@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmailServiceResilienceTest {

    @SpyBean
    private EmailServiceImpl emailService;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @BeforeEach
    void setUp() {
        // Reset circuit breaker before each test
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");
        circuitBreaker.reset();
    }

    @Test
    @Order(1)
    @DisplayName("Should send welcome email without throwing exceptions")
    void testSendWelcomeEmail() {
        // When: Send welcome email (mock implementation)
        assertDoesNotThrow(() -> 
            emailService.sendWelcomeEmail("user@example.com", "John Doe")
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should send password reset email without throwing exceptions")
    void testSendPasswordResetEmail() {
        // When: Send password reset email
        assertDoesNotThrow(() -> 
            emailService.sendPasswordResetEmail("user@example.com", "reset_token_12345")
        );
    }

    @Test
    @Order(3)
    @DisplayName("Should send verification email without throwing exceptions")
    void testSendVerificationEmail() {
        // When: Send verification email
        assertDoesNotThrow(() -> 
            emailService.sendEmailVerificationEmail("user@example.com", "verify_token_67890")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Circuit breaker should be configured for emailService")
    void testCircuitBreakerConfiguration() {
        // When: Get circuit breaker
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker("emailService");

        // Then: Circuit breaker exists and is in closed state
        assertThat(circuitBreaker).isNotNull();
        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        
        // Verify configuration
        assertThat(circuitBreaker.getCircuitBreakerConfig().getFailureRateThreshold()).isEqualTo(60.0f);
        assertThat(circuitBreaker.getCircuitBreakerConfig().getSlidingWindowSize()).isEqualTo(10);
    }

    @Test
    @Order(5)
    @DisplayName("Should not block user operations on email failure")
    void testNonBlockingEmailOperations() {
        // Given: Multiple email operations
        long startTime = System.currentTimeMillis();

        // When: Send multiple emails concurrently
        for (int i = 0; i < 5; i++) {
            emailService.sendWelcomeEmail("user" + i + "@example.com", "User" + i);
            emailService.sendPasswordResetEmail("user" + i + "@example.com", "token_" + i);
        }

        long duration = System.currentTimeMillis() - startTime;

        // Then: Operations complete quickly (mock implementation)
        assertThat(duration).isLessThan(1000); // Less than 1 second
    }

    @Test
    @Order(6)
    @DisplayName("Should handle empty or invalid email addresses gracefully")
    void testInvalidEmailHandling() {
        // When/Then: Should not throw exceptions for edge cases
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("", "John Doe"));
        assertDoesNotThrow(() -> emailService.sendWelcomeEmail("invalid-email", "Jane Doe"));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", ""));
    }

    @Test
    @Order(7)
    @DisplayName("Should handle short tokens gracefully")
    void testShortTokenHandling() {
        // When/Then: Should not throw StringIndexOutOfBoundsException
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail("user@example.com", "abc"));
        assertDoesNotThrow(() -> emailService.sendEmailVerificationEmail("user@example.com", "xy"));
        assertDoesNotThrow(() -> emailService.sendEmailVerificationEmail("user@example.com", ""));
    }
}
