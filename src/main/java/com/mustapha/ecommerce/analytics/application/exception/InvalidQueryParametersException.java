package com.mustapha.ecommerce.analytics.application.exception;

/**
 * Exception thrown when query parameters are invalid
 * Example: Null values, invalid combinations, malformed parameters
 */
public class InvalidQueryParametersException extends AnalyticsApplicationException {
    
    public InvalidQueryParametersException(String parameterName, String reason) {
        super(String.format("Invalid query parameter '%s': %s", parameterName, reason));
    }
    
    public InvalidQueryParametersException(String message) {
        super(message);
    }
}
