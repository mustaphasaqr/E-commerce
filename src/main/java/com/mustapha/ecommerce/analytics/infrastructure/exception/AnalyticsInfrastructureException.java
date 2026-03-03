package com.mustapha.ecommerce.analytics.infrastructure.exception;

/**
 * Base exception for analytics infrastructure layer
 * Responsibility: Represent technical/infrastructure failures
 * Pattern: Exception hierarchy
 * 
 * Infrastructure exceptions represent failures in database queries,
 * external services, I/O operations, and other technical concerns.
 */
public abstract class AnalyticsInfrastructureException extends RuntimeException {
    
    protected AnalyticsInfrastructureException(String message) {
        super(message);
    }
    
    protected AnalyticsInfrastructureException(String message, Throwable cause) {
        super(message, cause);
    }
}
