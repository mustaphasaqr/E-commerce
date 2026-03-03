package com.mustapha.ecommerce.shared.security;

import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.boot.actuate.info.InfoEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Actuator Security Configuration
 * 
 * Security Strategy:
 * - Public Access: health, info, metrics, prometheus (for monitoring tools)
 * - Owner Only: All other actuator endpoints (heapdump, threaddump, loggers, etc.)
 * 
 * Rationale:
 * - Health/Info needed by load balancers and monitoring
 * - Metrics/Prometheus needed by monitoring systems
 * - Sensitive endpoints (heapdump, threaddump, loggers) require OWNER role
 * 
 * Note: This filter chain has higher priority (@Order(1)) than the main security filter
 */
@Configuration
@Order(1) // Applied before main SecurityConfig
public class ActuatorSecurityConfig {

    @Bean
    public SecurityFilterChain actuatorSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/actuator/**")
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - needed for monitoring and health checks
                .requestMatchers(EndpointRequest.to(
                    HealthEndpoint.class,
                    InfoEndpoint.class,
                    MetricsEndpoint.class
                )).permitAll()
                
                // Public prometheus endpoint for metrics scraping
                .requestMatchers("/actuator/prometheus").permitAll()
                
                // All other actuator endpoints require OWNER role
                .requestMatchers(EndpointRequest.toAnyEndpoint()).hasRole("OWNER")
            )
            .csrf(AbstractHttpConfigurer::disable); // Actuator endpoints don't need CSRF
        
        return http.build();
    }
}
