package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetWorstSellingProductsQuery;
import com.mustapha.ecommerce.analytics.domain.model.ProductPerformance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetWorstSellingProductsUseCase Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetWorstSellingProductsUseCase Tests")
class GetWorstSellingProductsUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetWorstSellingProductsUseCase useCase;

    private GetWorstSellingProductsQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetWorstSellingProductsQuery(10, startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns worst performing products
        List<ProductPerformance> expectedProducts = List.of(
            new ProductPerformance("PROD-003", "Keyboard", 2, new BigDecimal("100.00"), 1),
            new ProductPerformance("PROD-004", "Monitor", 5, new BigDecimal("500.00"), 2)
        );
        when(analyticsQueryPort.getWorstSellingProducts(eq(10), eq(startDate), eq(endDate)))
            .thenReturn(expectedProducts);

        // When: Execute use case
        List<ProductPerformance> result = useCase.execute(query);

        // Then: Should return port result
        assertThat(result).isEqualTo(expectedProducts);
        verify(analyticsQueryPort).getWorstSellingProducts(10, startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass query parameters correctly")
    void testExecutePassesParametersCorrectly() {
        // Given: Query with specific parameters
        GetWorstSellingProductsQuery customQuery = new GetWorstSellingProductsQuery(
            15, 
            LocalDate.of(2026, 6, 1), 
            LocalDate.of(2026, 6, 30)
        );
        when(analyticsQueryPort.getWorstSellingProducts(any(), any(), any()))
            .thenReturn(List.of());

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact parameters
        verify(analyticsQueryPort).getWorstSellingProducts(
            15,
            LocalDate.of(2026, 6, 1),
            LocalDate.of(2026, 6, 30)
        );
    }
}
