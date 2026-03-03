package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetPeakSalesDayQuery;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetPeakSalesDayUseCase Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetPeakSalesDayUseCase Tests")
class GetPeakSalesDayUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetPeakSalesDayUseCase useCase;

    private GetPeakSalesDayQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetPeakSalesDayQuery(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns peak sales day
        DailySales expectedPeakDay = new DailySales(
            LocalDate.of(2026, 3, 15),
            50,
            new BigDecimal("25000.00")
        );
        when(analyticsQueryPort.getPeakSalesDay(eq(startDate), eq(endDate)))
            .thenReturn(Optional.of(expectedPeakDay));

        // When: Execute use case
        DailySales result = useCase.execute(query).orElseThrow();

        // Then: Should return port result
        assertThat(result).isEqualTo(expectedPeakDay);
        verify(analyticsQueryPort).getPeakSalesDay(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass date range correctly")
    void testExecutePassesDateRangeCorrectly() {
        // Given: Query with specific date range
        GetPeakSalesDayQuery customQuery = new GetPeakSalesDayQuery(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        );
        DailySales mockDay = new DailySales(LocalDate.now(), 0, BigDecimal.ZERO);
        when(analyticsQueryPort.getPeakSalesDay(any(), any()))
            .thenReturn(Optional.of(mockDay));

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact dates
        verify(analyticsQueryPort).getPeakSalesDay(
            LocalDate.of(2026, 2, 1),
            LocalDate.of(2026, 2, 28)
        );
    }
}
