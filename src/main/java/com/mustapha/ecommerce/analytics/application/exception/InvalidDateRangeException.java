package com.mustapha.ecommerce.analytics.application.exception;

import java.time.LocalDate;

/**
 * Exception thrown when date range is invalid
 * Example: Start date after end date, date range too large, future dates
 */
public class InvalidDateRangeException extends AnalyticsApplicationException {
    
    public InvalidDateRangeException(LocalDate startDate, LocalDate endDate) {
        super(String.format("Invalid date range: start date %s must be before or equal to end date %s", 
            startDate, endDate));
    }
    
    public InvalidDateRangeException(String reason) {
        super("Invalid date range: " + reason);
    }
    
    public InvalidDateRangeException(LocalDate startDate, LocalDate endDate, String reason) {
        super(String.format("Invalid date range [%s to %s]: %s", startDate, endDate, reason));
    }
}
