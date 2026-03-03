package com.mustapha.ecommerce.analytics.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue Forecast DTO
 * 
 * API representation of revenue predictions
 * Includes confidence intervals and trend analysis
 */
public record RevenueForecastDTO(
    LocalDate forecastStartDate,
    LocalDate forecastEndDate,
    int forecastDays,
    BigDecimal predictedRevenue,
    BigDecimal lowerBound,
    BigDecimal upperBound,
    BigDecimal averageDailyRevenue,
    BigDecimal dailyGrowthRate,
    String trend,
    BigDecimal confidence,
    int historicalDays,
    List<DailyForecastDTO> dailyForecasts
) {
    /**
     * Daily Forecast Detail DTO
     */
    public record DailyForecastDTO(
        LocalDate date,
        BigDecimal predictedRevenue,
        BigDecimal lowerBound,
        BigDecimal upperBound
    ) {}
}
