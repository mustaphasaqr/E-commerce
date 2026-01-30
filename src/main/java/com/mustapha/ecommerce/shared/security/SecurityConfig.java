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

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (stateless API)
            .csrf(csrf -> csrf.disable())
            
            // CORS configuration for frontend
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfig = new org.springframework.web.cors.CorsConfiguration();
                corsConfig.setAllowedOrigins(java.util.List.of(
                    "http://localhost:3000",  // React dev server
                    "http://localhost:4200",  // Angular dev server
                    "http://localhost:5173",  // Vite dev server
                    "https://yourdomain.com"  // Production frontend
                ));
                corsConfig.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                corsConfig.setAllowedHeaders(java.util.List.of("*"));
                corsConfig.setAllowCredentials(true);
                corsConfig.setMaxAge(3600L);
                return corsConfig;
            }))
            
            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - Authentication
                .requestMatchers("/api/auth/**").permitAll()
                s
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
            
            // Add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
