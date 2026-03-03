package com.mustapha.ecommerce.analytics.application.query;

import java.time.LocalDate;

/**
 * Get Best Selling Products Query
 * Responsibility: Transfer query parameters from API to Application layer
 * Pattern: Query (CQS - Command Query Separation)
 */
public class GetBestSellingProductsQuery {
    
    private final int limit;
    private final LocalDate startDate;
    private final LocalDate endDate;
    
    public GetBestSellingProductsQuery(int limit, LocalDate startDate, LocalDate endDate) {
        this.limit = limit;
        this.startDate = startDate;
        this.endDate = endDate;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
}
