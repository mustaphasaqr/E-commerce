package com.mustapha.ecommerce.shared.observability.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Circuit Breaker Health Indicator
 * Monitors Resilience4j circuit breakers state
 * 
 * Returns UP if all circuit breakers are CLOSED or HALF_OPEN
 * Returns DOWN if any circuit breaker is OPEN
 */
@Component
public class CircuitBreakerHealthIndicator implements HealthIndicator {

    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public CircuitBreakerHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean allHealthy = true;
        
        for (CircuitBreaker circuitBreaker : circuitBreakerRegistry.getAllCircuitBreakers()) {
            String name = circuitBreaker.getName();
            CircuitBreaker.State state = circuitBreaker.getState();
            
            Map<String, Object> cbDetails = new HashMap<>();
            cbDetails.put("state", state.toString());
            cbDetails.put("failureRate", circuitBreaker.getMetrics().getFailureRate());
            cbDetails.put("numberOfFailedCalls", circuitBreaker.getMetrics().getNumberOfFailedCalls());
            cbDetails.put("numberOfSuccessfulCalls", circuitBreaker.getMetrics().getNumberOfSuccessfulCalls());
            cbDetails.put("numberOfBufferedCalls", circuitBreaker.getMetrics().getNumberOfBufferedCalls());
            
            details.put(name, cbDetails);
            
            // Circuit breaker OPEN means service is down
            if (state == CircuitBreaker.State.OPEN) {
                allHealthy = false;
            }
        }
        
        if (allHealthy) {
            return Health.up().withDetails(details).build();
        } else {
            return Health.down().withDetails(details).build();
        }
    }
}
