package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetWorstSellingProductsQuery;
import com.mustapha.ecommerce.analytics.domain.model.ProductPerformance;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Get Worst Selling Products Use Case
 * Responsibility: Orchestrate retrieving worst-selling products analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetWorstSellingProductsUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetWorstSellingProductsUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public List<ProductPerformance> execute(GetWorstSellingProductsQuery query) {
        return analyticsQueryPort.getWorstSellingProducts(
            query.getLimit(),
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
