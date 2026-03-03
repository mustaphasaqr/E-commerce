package com.mustapha.ecommerce.analytics.domain.exception;

/**
 * Base exception for analytics domain layer
 * Responsibility: Represent business rule violations in analytics domain
 * Pattern: Exception hierarchy
 * 
 * Domain exceptions represent violations of business rules and invariants.
 * They should be descriptive and provide context about what went wrong.
 */
public abstract class AnalyticsDomainException extends RuntimeException {
    
    protected AnalyticsDomainException(String message) {
        super(message);
    }
    
    protected AnalyticsDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
