package com.mustapha.ecommerce.analytics.infrastructure.exception;

/**
 * Exception thrown when database connection or persistence context issues occur
 * Example: EntityManager unavailable, transaction issues, connection timeout
 */
public class PersistenceException extends AnalyticsInfrastructureException {
    
    public PersistenceException(String message) {
        super(message);
    }
    
    public PersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
