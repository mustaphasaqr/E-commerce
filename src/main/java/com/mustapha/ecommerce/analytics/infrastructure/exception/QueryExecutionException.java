package com.mustapha.ecommerce.analytics.infrastructure.exception;

/**
 * Exception thrown when a database query fails to execute
 * Wraps underlying SQLException or JPA exceptions with context
 */
public class QueryExecutionException extends AnalyticsInfrastructureException {
    
    public QueryExecutionException(String queryDescription, Throwable cause) {
        super(String.format("Failed to execute analytics query: %s. Reason: %s", 
            queryDescription, cause.getMessage()), cause);
    }
    
    public QueryExecutionException(String queryDescription, String reason) {
        super(String.format("Failed to execute analytics query: %s. Reason: %s", 
            queryDescription, reason));
    }
}
