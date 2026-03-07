package com.mustapha.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Test-Specific Security Configuration
 * 
 * Purpose: Disable strict JWT authentication for test endpoints
 * This allows integration tests to run without generating tokens for every call
 * 
 * Profile: Activated ONLY when spring.profiles.active=test
 */
@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    /**
     * For tests: Allow all analytics/admin/product endpoints without JWT
     * This is safe because:
     * 1. Tests run in isolation with H2 in-memory database
     * 2. No external access (only localhost)
     * 3. Test data is automatically cleaned up after each test (@DirtiesContext)
     */
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/**")
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no auth required
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/health/**").permitAll()
                .requestMatchers("/api/v1/products").permitAll()
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/reviews/**").permitAll()
                
                // Test endpoints - permit all for testing
                .requestMatchers("/api/v1/admin/**").permitAll()
                .requestMatchers("/api/v1/owner/**").permitAll()
                .requestMatchers("/api/v1/analytics/**").permitAll()
                .requestMatchers("/api/v1/orders/**").permitAll()
                .requestMatchers("/api/v1/cart/**").permitAll()
                .requestMatchers("/api/v1/users/**").permitAll()
                
                // Everything else - permit (for test purposes)
                .anyRequest().permitAll()
            )
            .csrf().disable()
            .cors().and()
            .httpBasic().disable();

        return http.build();
    }
}
