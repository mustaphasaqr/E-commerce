package com.mustapha.ecommerce.shared.config;

import org.springframework.context.annotation.Configuration;

/**
 * Resilience4j Configuration
 * 
 * Configuration is primarily done via application.properties
 * resilience4j-spring-boot3 starter auto-configures circuit breakers, retries, and time limiters
 */
@Configuration
public class ResilienceConfig {
    // Configuration is in application.properties
    // resilience4j.circuitbreaker.instances.productService.*
    // resilience4j.retry.instances.productService.*
    // resilience4j.timelimiter.instances.productService.*
}
