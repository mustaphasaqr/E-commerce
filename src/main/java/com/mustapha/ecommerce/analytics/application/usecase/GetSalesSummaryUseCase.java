package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetSalesSummaryQuery;
import com.mustapha.ecommerce.analytics.domain.model.SalesSummary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Get Sales Summary Use Case
 * Responsibility: Orchestrate retrieving sales summary analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetSalesSummaryUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetSalesSummaryUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public Optional<SalesSummary> execute(GetSalesSummaryQuery query) {
        return analyticsQueryPort.getSalesSummary(
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
