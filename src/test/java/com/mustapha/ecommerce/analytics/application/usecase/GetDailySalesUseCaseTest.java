package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetDailySalesQuery;
import com.mustapha.ecommerce.analytics.domain.model.DailySales;
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
 * GetDailySalesUseCase Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetDailySalesUseCase Tests")
class GetDailySalesUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetDailySalesUseCase useCase;

    private GetDailySalesQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetDailySalesQuery(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns daily sales data
        List<DailySales> expectedSales = List.of(
            new DailySales(LocalDate.of(2026, 3, 1), 10, new BigDecimal("5000.00")),
            new DailySales(LocalDate.of(2026, 3, 2), 15, new BigDecimal("7500.00"))
        );
        when(analyticsQueryPort.getDailySales(eq(startDate), eq(endDate)))
            .thenReturn(expectedSales);

        // When: Execute use case
        List<DailySales> result = useCase.execute(query);

        // Then: Should return port result
        assertThat(result).isEqualTo(expectedSales);
        verify(analyticsQueryPort).getDailySales(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass date range correctly")
    void testExecutePassesDateRangeCorrectly() {
        // Given: Query with specific date range
        GetDailySalesQuery customQuery = new GetDailySalesQuery(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31)
        );
        when(analyticsQueryPort.getDailySales(any(), any()))
            .thenReturn(List.of());

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact dates
        verify(analyticsQueryPort).getDailySales(
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31)
        );
    }
}
