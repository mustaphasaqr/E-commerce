package com.mustapha.ecommerce.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Test JPA Auditing Configuration  
 * 
 * Disables automatic timestamp setting in tests.
 * Allows tests to manually control createdAt/updatedAt for date-based queries.
 * 
 * Note: Does NOT provide DateTimeProvider bean, preventing auto-timestamp.
 */
@TestConfiguration
public class TestJpaAuditingConfig {
    
    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        // Always return "TEST" as the auditor in test environment
        return () -> Optional.of("TEST");
    }
    
    // NO DateTimeProvider bean - this should disable auto-timestamp
}
