package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetSlowestSalesDayQuery;
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
 * GetSlowestSalesDayUseCase Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSlowestSalesDayUseCase Tests")
class GetSlowestSalesDayUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetSlowestSalesDayUseCase useCase;

    private GetSlowestSalesDayQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetSlowestSalesDayQuery(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns slowest sales day
        DailySales expectedSlowestDay = new DailySales(
            LocalDate.of(2026, 3, 5),
            5,
            new BigDecimal("1000.00")
        );
        when(analyticsQueryPort.getSlowestSalesDay(eq(startDate), eq(endDate)))
            .thenReturn(Optional.of(expectedSlowestDay));

        // When: Execute use case
        DailySales result = useCase.execute(query).orElseThrow();

        // Then: Should return port result
        assertThat(result).isEqualTo(expectedSlowestDay);
        verify(analyticsQueryPort).getSlowestSalesDay(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass date range correctly")
    void testExecutePassesDateRangeCorrectly() {
        // Given: Query with specific date range
        GetSlowestSalesDayQuery customQuery = new GetSlowestSalesDayQuery(
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 30)
        );
        DailySales mockDay = new DailySales(LocalDate.now(), 0, BigDecimal.ZERO);
        when(analyticsQueryPort.getSlowestSalesDay(any(), any()))
            .thenReturn(Optional.of(mockDay));

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact dates
        verify(analyticsQueryPort).getSlowestSalesDay(
            LocalDate.of(2026, 4, 1),
            LocalDate.of(2026, 4, 30)
        );
    }
}
