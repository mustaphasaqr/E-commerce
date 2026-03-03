package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetBestSellingProductsQuery;
import com.mustapha.ecommerce.analytics.domain.model.ProductPerformance;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Get Best Selling Products Use Case
 * Responsibility: Orchestrate retrieving best-selling products analytics
 * Pattern: Use Case (Application Service)
 * 
 * Hexagonal Architecture:
 * - Use Case depends on Port (application layer interface)
 * - Infrastructure provides Adapter (implements the port)
 * - This decouples application from infrastructure
 * 
 * Clean Read-Only Pattern:
 * 1. Execute query via port
 * 2. Return domain objects
 * 
 * Note: Read-only queries don't need event publishing
 */
@Component
public class GetBestSellingProductsUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetBestSellingProductsUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public List<ProductPerformance> execute(GetBestSellingProductsQuery query) {
        return analyticsQueryPort.getBestSellingProducts(
            query.getLimit(),
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
