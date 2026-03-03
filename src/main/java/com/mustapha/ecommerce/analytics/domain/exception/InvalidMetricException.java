package com.mustapha.ecommerce.analytics.domain.exception;

/**
 * Exception thrown when a metric value is invalid or out of acceptable range
 * Example: Negative sales values, null revenue when it shouldn't be
 */
public class InvalidMetricException extends AnalyticsDomainException {
    
    public InvalidMetricException(String metricName, Object value) {
        super(String.format("Invalid metric value for %s: %s", metricName, value));
    }
    
    public InvalidMetricException(String metricName, Object value, String reason) {
        super(String.format("Invalid metric value for %s: %s. Reason: %s", 
            metricName, value, reason));
    }
}
