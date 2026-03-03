package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetDailySalesQuery;
import com.mustapha.ecommerce.analytics.domain.model.DailySales;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Get Daily Sales Use Case
 * Responsibility: Orchestrate retrieving daily sales analytics
 * Pattern: Use Case (Application Service)
 */
@Component
public class GetDailySalesUseCase {
    
    private final AnalyticsQueryPort analyticsQueryPort;

    public GetDailySalesUseCase(AnalyticsQueryPort analyticsQueryPort) {
        this.analyticsQueryPort = analyticsQueryPort;
    }
    
    @Transactional(readOnly = true)
    public List<DailySales> execute(GetDailySalesQuery query) {
        return analyticsQueryPort.getDailySales(
            query.getStartDate(),
            query.getEndDate()
        );
    }
}
