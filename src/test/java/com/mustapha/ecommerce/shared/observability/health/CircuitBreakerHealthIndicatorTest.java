package com.mustapha.ecommerce.shared.observability.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Circuit Breaker Health Indicator Tests
 */
@DisplayName("Circuit Breaker Health Indicator Tests")
class CircuitBreakerHealthIndicatorTest {

    private CircuitBreakerRegistry circuitBreakerRegistry;
    private CircuitBreakerHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        circuitBreakerRegistry = CircuitBreakerRegistry.ofDefaults();
        healthIndicator = new CircuitBreakerHealthIndicator(circuitBreakerRegistry);
    }

    @Test
    @DisplayName("Should return UP when all circuit breakers are CLOSED")
    void shouldReturnUpWhenAllCircuitBreakersAreClosed() {
        // Given
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("testService");
        // Circuit breaker is CLOSED by default
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsKey("testService");
    }

    @Test
    @DisplayName("Should return DOWN when any circuit breaker is OPEN")
    void shouldReturnDownWhenAnyCircuitBreakerIsOpen() {
        // Given
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("testService");
        
        // Force circuit breaker to OPEN by recording failures
        for (int i = 0; i < 100; i++) {
            cb.onError(0, java.util.concurrent.TimeUnit.MILLISECONDS, new RuntimeException("Test failure"));
        }
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    @Test
    @DisplayName("Should include circuit breaker metrics in details")
    void shouldIncludeCircuitBreakerMetricsInDetails() {
        // Given
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("testService");
        cb.onSuccess(100, java.util.concurrent.TimeUnit.MILLISECONDS);
        
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getDetails()).containsKey("testService");
        assertThat(health.getDetails().get("testService")).isInstanceOf(java.util.Map.class);
    }
}
