package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetSalesSummaryQuery;
import com.mustapha.ecommerce.analytics.domain.model.SalesSummary;
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
 * GetSalesSummaryUseCase Unit Tests
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetSalesSummaryUseCase Tests")
class GetSalesSummaryUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetSalesSummaryUseCase useCase;

    private GetSalesSummaryQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetSalesSummaryQuery(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns sales summary
        SalesSummary expectedSummary = new SalesSummary(
            100L,                           // totalOrders
            new BigDecimal("50000.00"),     // totalRevenue
            85L,                            // completedOrders
            10L,                            // cancelledOrders
            5L                              // pendingOrders
        );
        when(analyticsQueryPort.getSalesSummary(eq(startDate), eq(endDate)))
            .thenReturn(Optional.of(expectedSummary));

        // When: Execute use case
        SalesSummary result = useCase.execute(query).orElseThrow();

        // Then: Should return port result
        assertThat(result).isEqualTo(expectedSummary);
        verify(analyticsQueryPort).getSalesSummary(startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass date range correctly")
    void testExecutePassesDateRangeCorrectly() {
        // Given: Query with specific date range
        GetSalesSummaryQuery customQuery = new GetSalesSummaryQuery(
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31)
        );
        SalesSummary mockSummary = new SalesSummary(0L, BigDecimal.ZERO, 0L, 0L, 0L);
        when(analyticsQueryPort.getSalesSummary(any(), any()))
            .thenReturn(Optional.of(mockSummary));

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact dates
        verify(analyticsQueryPort).getSalesSummary(
            LocalDate.of(2026, 5, 1),
            LocalDate.of(2026, 5, 31)
        );
    }

    @Test
    @DisplayName("execute() - Should handle summary with all zero values")
    void testExecuteHandlesZeroValues() {
        // Given: Port returns summary with no sales
        SalesSummary zeroSummary = new SalesSummary(0L, BigDecimal.ZERO, 0L, 0L, 0L);
        when(analyticsQueryPort.getSalesSummary(any(), any()))
            .thenReturn(Optional.of(zeroSummary));

        // When: Execute use case
        SalesSummary result = useCase.execute(query).orElseThrow();

        // Then: Should return zero summary
        assertThat(result.getTotalOrders()).isEqualTo(0);
        assertThat(result.getTotalRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
