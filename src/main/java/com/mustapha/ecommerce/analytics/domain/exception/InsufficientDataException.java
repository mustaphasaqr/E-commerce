package com.mustapha.ecommerce.analytics.domain.exception;

/**
 * Exception thrown when there is insufficient data to perform analytics
 * Example: Trying to calculate trends with less than 2 data points
 */
public class InsufficientDataException extends AnalyticsDomainException {
    
    public InsufficientDataException(String metric) {
        super("Insufficient data to calculate " + metric);
    }
    
    public InsufficientDataException(String metric, int required, int actual) {
        super(String.format("Insufficient data to calculate %s: requires %d data points, found %d", 
            metric, required, actual));
    }
}
