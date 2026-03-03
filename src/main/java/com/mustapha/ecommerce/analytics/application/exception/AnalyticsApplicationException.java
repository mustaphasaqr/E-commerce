package com.mustapha.ecommerce.analytics.application.exception;

/**
 * Base exception for analytics application layer
 * Responsibility: Represent validation errors and orchestration failures
 * Pattern: Exception hierarchy
 * 
 * Application exceptions represent failures in use cases, validation errors,
 * and coordination problems between layers.
 */
public abstract class AnalyticsApplicationException extends RuntimeException {
    
    protected AnalyticsApplicationException(String message) {
        super(message);
    }
    
    protected AnalyticsApplicationException(String message, Throwable cause) {
        super(message, cause);
    }
}
