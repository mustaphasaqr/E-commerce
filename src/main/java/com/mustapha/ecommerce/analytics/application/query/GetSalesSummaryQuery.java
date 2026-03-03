package com.mustapha.ecommerce.analytics.application.query;

import java.time.LocalDate;

/**
 * Get Sales Summary Query
 * Responsibility: Transfer query parameters from API to Application layer
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetSalesSummaryQuery {
    
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public GetSalesSummaryQuery(LocalDate startDate, LocalDate endDate) {
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
