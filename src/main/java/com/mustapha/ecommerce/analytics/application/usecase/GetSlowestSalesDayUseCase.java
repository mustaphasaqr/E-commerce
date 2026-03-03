package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetSlowestSalesDayQuery;
import com.mustapha.ecommerce.analytics.domain.model.DailySales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Get Slowest Sales Day Use Case
 * Responsibility: Orchestrate retrieving slowest sales day analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetSlowestSalesDayUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetSlowestSalesDayUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public Optional<DailySales> execute(GetSlowestSalesDayQuery query) {
        return analyticsQueryPort.getSlowestSalesDay(
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
