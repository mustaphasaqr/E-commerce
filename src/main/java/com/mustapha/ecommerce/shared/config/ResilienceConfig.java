package com.mustapha.ecommerce.shared.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;

/**
 * Resilience4j Configuration
 * 
 * Purpose: Configure resilience patterns for production readiness
 * Patterns:
 * - Circuit Breaker: Prevent cascade failures when downstream services fail
 * - Retry: Handle transient failures (network timeouts, temporary errors)
 * - Bulkhead: Resource isolation (limit concurrent calls)
 * - Time Limiter: Prevent thread starvation from slow calls
 * 
 * Configuration Strategy:
 * - Base configuration in application.properties
 * - Event listeners for observability (logs circuit state changes)
 * - Custom beans for HTTP clients with timeouts
 * 
 * Services configured:
 * - paymentService: Stripe payment gateway (critical, 3 retries, 10s circuit wait)
 * - emailService: SMTP/Email provider (non-critical, 3 retries, 30s circuit wait)
 * - productService: Internal product operations (3 retries, 5s circuit wait)
 */
@Configuration
public class ResilienceConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(ResilienceConfig.class);
    
    @Autowired
    @Lazy
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @Autowired
    @Lazy
    private RetryRegistry retryRegistry;
    
    /**
     * Configure Circuit Breaker event listeners for observability
     * Logs: state transitions, errors, success rate
     */
    @PostConstruct
    public void configureCircuitBreakerEventListeners() {
        circuitBreakerRegistry.circuitBreaker("paymentService").getEventPublisher()
            .onStateTransition(event -> 
                logger.warn("🔴 Payment Circuit Breaker state changed: {} -> {}", 
                           event.getStateTransition().getFromState(), 
                           event.getStateTransition().getToState()))
            .onError(event -> 
                logger.error("💥 Payment Circuit Breaker recorded error: {}", 
                            event.getThrowable().getMessage()))
            .onSuccess(event -> 
                logger.debug("✅ Payment Circuit Breaker recorded success"));
        
        circuitBreakerRegistry.circuitBreaker("emailService").getEventPublisher()
            .onStateTransition(event -> 
                logger.warn("📧 Email Circuit Breaker state changed: {} -> {}", 
                           event.getStateTransition().getFromState(), 
                           event.getStateTransition().getToState()))
            .onError(event -> 
                logger.error("💥 Email Circuit Breaker recorded error: {}", 
                            event.getThrowable().getMessage()));
        
        circuitBreakerRegistry.circuitBreaker("productService").getEventPublisher()
            .onStateTransition(event -> 
                logger.warn("📦 Product Circuit Breaker state changed: {} -> {}", 
                           event.getStateTransition().getFromState(), 
                           event.getStateTransition().getToState()));
        
        logger.info("✅ Circuit Breaker event listeners configured");
    }
    
    /**
     * Configure Retry event listeners for observability
     * Logs: retry attempts, final success/failure
     */
    @PostConstruct
    public void configureRetryEventListeners() {
        retryRegistry.retry("paymentService").getEventPublisher()
            .onRetry(event -> 
                logger.warn("🔁 Payment retry attempt {} of {}: {}", 
                           event.getNumberOfRetryAttempts(), 
                           retryRegistry.retry("paymentService").getRetryConfig().getMaxAttempts(),
                           event.getLastThrowable().getMessage()));
        
        retryRegistry.retry("emailService").getEventPublisher()
            .onRetry(event -> 
                logger.warn("🔁 Email retry attempt {} of {}: {}", 
                           event.getNumberOfRetryAttempts(),
                           retryRegistry.retry("emailService").getRetryConfig().getMaxAttempts(),
                           event.getLastThrowable().getMessage()));
        
        logger.info("✅ Retry event listeners configured");
    }
    
    /**
     * RestTemplate with timeout configuration
     * Used by: PasswordBreachChecker, future HTTP clients
     * Timeouts prevent thread pool exhaustion from hanging connections
     */
    @Bean(name = "resilientRestTemplate")
    public RestTemplate resilientRestTemplate() {
        org.springframework.http.client.SimpleClientHttpRequestFactory factory = 
            new org.springframework.http.client.SimpleClientHttpRequestFactory();
        
        // Connection timeout: 5 seconds to establish TCP connection
        factory.setConnectTimeout(5000);
        
        // Read timeout: 10 seconds to receive response
        factory.setReadTimeout(10000);
        
        RestTemplate restTemplate = new RestTemplate(factory);
        
        logger.info("✅ Created resilient RestTemplate with timeouts: connect=5s, read=10s");
        
        return restTemplate;
    }
}
