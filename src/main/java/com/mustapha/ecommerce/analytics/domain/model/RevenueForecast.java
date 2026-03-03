package com.mustapha.ecommerce.analytics.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Revenue Forecast - Domain Model
 * 
 * Represents revenue predictions based on historical data analysis
 * Uses statistical methods: linear regression, moving average, trend analysis
 * 
 * Business Context:
 * - Helps owners plan inventory, staffing, marketing budgets
 * - Provides confidence intervals for risk assessment
 * - Identifies growth trends and seasonality
 * 
 * Pattern: Value Object (immutable)
 */
@Getter
@AllArgsConstructor
public class RevenueForecast {
    
    /**
     * Forecast period start date
     */
    private final LocalDate forecastStartDate;
    
    /**
     * Forecast period end date
     */
    private final LocalDate forecastEndDate;
    
    /**
     * Number of days forecasted
     */
    private final int forecastDays;
    
    /**
     * Predicted total revenue for forecast period
     */
    private final BigDecimal predictedRevenue;
    
    /**
     * Lower bound (pessimistic scenario)
     */
    private final BigDecimal lowerBound;
    
    /**
     * Upper bound (optimistic scenario)
     */
    private final BigDecimal upperBound;
    
    /**
     * Average daily revenue (historical baseline)
     */
    private final BigDecimal averageDailyRevenue;
    
    /**
     * Daily growth rate as percentage (-100 to +∞)
     * Example: 2.5 means 2.5% daily growth
     * Example: -1.0 means 1% daily decline
     */
    private final BigDecimal dailyGrowthRate;
    
    /**
     * Trend direction: GROWING, STABLE, DECLINING
     */
    private final String trend;
    
    /**
     * Confidence level (0-100%)
     * Based on historical data variance
     * >80% = high confidence (stable pattern)
     * 50-80% = medium confidence (some volatility)
     * <50% = low confidence (high volatility)
     */
    private final BigDecimal confidence;
    
    /**
     * Number of historical days analyzed
     */
    private final int historicalDays;
    
    /**
     * Daily forecast breakdown (optional, for detailed view)
     */
    private final List<DailyForecast> dailyForecasts;
    
    /**
     * Calculate confidence level from historical data variance
     * 
     * Algorithm:
     * - Low variance = high confidence (predictable pattern)
     * - High variance = low confidence (erratic pattern)
     * 
     * @param variance revenue variance
     * @param mean average revenue
     * @return confidence percentage (0-100)
     */
    public static BigDecimal calculateConfidence(BigDecimal variance, BigDecimal mean) {
        if (mean.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        // Coefficient of Variation (CV) = sqrt(variance) / mean
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        BigDecimal cv = stdDev.divide(mean, 4, RoundingMode.HALF_UP);
        
        // Convert CV to confidence: lower CV = higher confidence
        // CV of 0 = 100% confidence
        // CV of 1+ = 0% confidence
        BigDecimal confidence = BigDecimal.ONE.subtract(cv.min(BigDecimal.ONE))
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);
        
        return confidence.max(BigDecimal.ZERO).min(BigDecimal.valueOf(100));
    }
    
    /**
     * Determine trend direction from growth rate
     */
    public static String determineTrend(BigDecimal growthRate) {
        if (growthRate.compareTo(BigDecimal.valueOf(0.5)) > 0) {
            return "GROWING";
        } else if (growthRate.compareTo(BigDecimal.valueOf(-0.5)) < 0) {
            return "DECLINING";
        } else {
            return "STABLE";
        }
    }
    
    /**
     * Daily Forecast Detail
     */
    @Getter
    @AllArgsConstructor
    public static class DailyForecast {
        private final LocalDate date;
        private final BigDecimal predictedRevenue;
        private final BigDecimal lowerBound;
        private final BigDecimal upperBound;
    }
}
