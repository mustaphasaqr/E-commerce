package com.mustapha.ecommerce.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

/**
 * Spring Security Configuration
 * 
 * Security Strategy:
 * - Stateless JWT authentication (no sessions)
 * - Public endpoints: /api/auth/** (registration, login)
 * - Protected endpoints: All other /api/** (requires JWT)
 * - Method-level security: @PreAuthorize on controllers
 * 
 * CSRF: Disabled (stateless API with JWT)
 * CORS: TODO - Configure for production frontend
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final GlobalApiRateLimitFilter globalApiRateLimitFilter;
    private final AdminIpWhitelistFilter adminIpWhitelistFilter;
    private final ExponentialBackoffFilter exponentialBackoffFilter;
    private final RequestIdFilter requestIdFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         GlobalApiRateLimitFilter globalApiRateLimitFilter,
                         AdminIpWhitelistFilter adminIpWhitelistFilter,
                         ExponentialBackoffFilter exponentialBackoffFilter,
                         RequestIdFilter requestIdFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.globalApiRateLimitFilter = globalApiRateLimitFilter;
        this.adminIpWhitelistFilter = adminIpWhitelistFilter;
        this.exponentialBackoffFilter = exponentialBackoffFilter;
        this.requestIdFilter = requestIdFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless API)
            .csrf(csrf -> csrf.disable())
            
            // Security Headers (Production-Ready)
            .headers(headers -> headers
                // Content Security Policy - Prevent XSS, clickjacking, code injection
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'"))
                // X-Frame-Options - Prevent clickjacking
                .frameOptions(frame -> frame.deny())
                // X-Content-Type-Options - Prevent MIME-sniffing
                .contentTypeOptions(contentType -> contentType.disable())
                // X-XSS-Protection - Enable browser XSS filter
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                // HTTP Strict Transport Security - Force HTTPS (31536000 = 1 year)
                .httpStrictTransportSecurity(hsts -> hsts
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true))
                // Referrer Policy - Control referrer information
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                // Permissions Policy - Control browser features
                .permissionsPolicy(permissions -> permissions
                    .policy("geolocation=(), microphone=(), camera=()"))
            )
            
            // CORS configuration for frontend
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                
                // Production: Use environment variable for allowed origins
                String allowedOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
                if (allowedOrigins != null && !allowedOrigins.isEmpty()) {
                    corsConfig.setAllowedOrigins(java.util.Arrays.asList(allowedOrigins.split(",")));
                } else {
                    // Development fallback
                    corsConfig.setAllowedOrigins(java.util.List.of(
                        "http://localhost:3000",  // React dev server
                        "http://localhost:4200",  // Angular dev server
                        "http://localhost:5173"   // Vite dev server
                    ));
                }
                
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Health checks
                .requestMatchers("/health/**").permitAll()
                
                // Public endpoints - Authentication (login, register, refresh, password reset, email verification)
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/refresh").permitAll()
                .requestMatchers("/api/auth/password-reset/**").permitAll()
                .requestMatchers("/api/auth/email-verification/**").permitAll()
                
                // Protected endpoints - Auth (logout requires authentication)
                .requestMatchers("/api/auth/logout").authenticated()
                .requestMatchers("/api/auth/logout-all").authenticated()
                
                // Public endpoints - User registration (POST only)
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                
                // Public endpoints - Product browsing (GET only)
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                
                // All other /api/** endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Allow all other requests (for development)
                .anyRequest().permitAll()
            )
            
            // Stateless session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Add filters in order: RequestId -> Backoff -> Admin IP -> Rate Limit -> JWT -> Auth
            .addFilterBefore(requestIdFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(exponentialBackoffFilter, RequestIdFilter.class)
            .addFilterAfter(adminIpWhitelistFilter, ExponentialBackoffFilter.class)
            .addFilterAfter(globalApiRateLimitFilter, AdminIpWhitelistFilter.class)
            .addFilterAfter(jwtAuthenticationFilter, GlobalApiRateLimitFilter.class);
        
        return http.build();
    }
}
