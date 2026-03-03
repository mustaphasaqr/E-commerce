package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetTopRevenueProductsQuery;
import com.mustapha.ecommerce.analytics.domain.model.ProductPerformance;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Get Top Revenue Products Use Case
 * Responsibility: Orchestrate retrieving top revenue products analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetTopRevenueProductsUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetTopRevenueProductsUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public List<ProductPerformance> execute(GetTopRevenueProductsQuery query) {
        return analyticsQueryPort.getTopRevenueProducts(
            query.getLimit(),
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
