package com.mustapha.ecommerce.analytics.application.query;

import java.time.LocalDate;

/**
 * Get Daily Sales Query
 * Responsibility: Transfer query parameters from API to Application layer
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetDailySalesQuery {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public GetDailySalesQuery(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}
