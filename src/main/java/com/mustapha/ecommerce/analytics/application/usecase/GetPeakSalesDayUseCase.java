package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetPeakSalesDayQuery;
import com.mustapha.ecommerce.analytics.domain.model.DailySales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Get Peak Sales Day Use Case
 * Responsibility: Orchestrate retrieving peak sales day analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetPeakSalesDayUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetPeakSalesDayUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public Optional<DailySales> execute(GetPeakSalesDayQuery query) {
        return analyticsQueryPort.getPeakSalesDay(
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
