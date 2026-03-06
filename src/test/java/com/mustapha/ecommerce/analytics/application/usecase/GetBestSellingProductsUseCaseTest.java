package com.mustapha.ecommerce.analytics.application.usecase;

import com.mustapha.ecommerce.analytics.application.port.out.AnalyticsQueryPort;
import com.mustapha.ecommerce.analytics.application.query.GetBestSellingProductsQuery;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * GetBestSellingProductsUseCase Unit Tests
 * Tests the thin orchestration layer with mocked port
 * 
 * Pattern: UseCase tests mock the port interface (hexagonal architecture)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GetBestSellingProductsUseCase Tests")
class GetBestSellingProductsUseCaseTest {

    @Mock
    private AnalyticsQueryPort analyticsQueryPort;

    @InjectMocks
    private GetBestSellingProductsUseCase useCase;

    private GetBestSellingProductsQuery query;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        startDate = LocalDate.of(2026, 3, 1);
        endDate = LocalDate.of(2026, 3, 31);
        query = new GetBestSellingProductsQuery(10, startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should delegate to analytics query port")
    void testExecuteDelegatesToPort() {
        // Given: Mock port returns product performance data
        List<ProductPerformance> expectedProducts = List.of(
            new ProductPerformance("PROD-001", "Laptop", 50, new BigDecimal("50000.00"), 25),
            new ProductPerformance("PROD-002", "Mouse", 100, new BigDecimal("2500.00"), 50)
        );
        when(analyticsQueryPort.getBestSellingProducts(eq(10), eq(startDate), eq(endDate)))
            .thenReturn(expectedProducts);

        // When: Execute use case
        List<ProductPerformance> result = useCase.execute(query);

        // Then: Should return port result and verify delegation
        assertThat(result).isEqualTo(expectedProducts);
        verify(analyticsQueryPort).getBestSellingProducts(10, startDate, endDate);
    }

    @Test
    @DisplayName("execute() - Should pass query parameters correctly")
    void testExecutePassesParametersCorrectly() {
        // Given: Query with specific parameters
        GetBestSellingProductsQuery customQuery = new GetBestSellingProductsQuery(
            25, 
            LocalDate.of(2026, 1, 1), 
            LocalDate.of(2026, 12, 31)
        );
        when(analyticsQueryPort.getBestSellingProducts(anyInt(), any(), any()))
            .thenReturn(List.of());

        // When: Execute use case
        useCase.execute(customQuery);

        // Then: Should pass exact parameters from query
        verify(analyticsQueryPort).getBestSellingProducts(
            25,
            LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 12, 31)
        );
    }

    @Test
    @DisplayName("execute() - Should handle empty results")
    void testExecuteHandlesEmptyResults() {
        // Given: Port returns empty list
        when(analyticsQueryPort.getBestSellingProducts(anyInt(), any(), any()))
            .thenReturn(List.of());

        // When: Execute use case
        List<ProductPerformance> result = useCase.execute(query);

        // Then: Should return empty list
        assertThat(result).isEmpty();
        verify(analyticsQueryPort).getBestSellingProducts(10, startDate, endDate);
    }
}
