package com.mustapha.ecommerce.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * JPA Auditing Configuration
 * 
 * Enables automatic tracking of:
 * - Who created/updated each entity
 * - When entities were created/updated
 * 
 * Pattern: Spring Data JPA Auditing
 * Layer: INFRASTRUCTURE / CONFIGURATION
 * 
 * How it works:
 * 1. @EnableJpaAuditing activates auditing
 * 2. AuditorAware<String> provides current user
 * 3. @EntityListeners(AuditingEntityListener.class) on entities
 * 4. @CreatedBy, @CreatedDate, @LastModifiedBy, @LastModifiedDate on fields
 * 
 * Security Context:
 * - If user is authenticated: Uses username from JWT
 * - If user is anonymous: Uses "SYSTEM" (for scheduled jobs, migrations)
 * - If security context is null: Uses "SYSTEM"
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {
    
    /**
     * Provides current auditor (username) from Spring Security context
     * 
     * Priority:
     * 1. Authenticated user's username (from JWT)
     * 2. "SYSTEM" for scheduled jobs, migrations, background tasks
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new SpringSecurityAuditorAware();
    }
    
    /**
     * Spring Security-aware Auditor Provider
     * 
     * Extracts username from SecurityContext for audit logging
     */
    static class SpringSecurityAuditorAware implements AuditorAware<String> {
        
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // No authentication or anonymous user
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("SYSTEM");
            }
            
            // Extract username from authentication
            String username = authentication.getName();
            
            // Fallback to SYSTEM if username is null/empty
            if (username == null || username.trim().isEmpty()) {
                return Optional.of("SYSTEM");
            }
            
            // Truncate to 100 chars to match DB column size
            if (username.length() > 100) {
                username = username.substring(0, 100);
            }
            
            return Optional.of(username);
        }
    }
}
