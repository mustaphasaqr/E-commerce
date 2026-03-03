package com.mustapha.ecommerce.analytics.application.exception;

/**
 * Exception thrown when limit parameter is invalid
 * Example: Negative limit, zero limit, limit exceeds maximum
 */
public class InvalidLimitException extends AnalyticsApplicationException {
    
    private static final int DEFAULT_MIN = 1;
    private static final int DEFAULT_MAX = 100;
    
    public InvalidLimitException(int limit) {
        super(String.format("Invalid limit: %d. Must be between %d and %d", 
            limit, DEFAULT_MIN, DEFAULT_MAX));
    }
    
    public InvalidLimitException(int limit, int min, int max) {
        super(String.format("Invalid limit: %d. Must be between %d and %d", limit, min, max));
    }
    
    public InvalidLimitException(String reason) {
        super("Invalid limit: " + reason);
    }
}
