package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Marketing Attribution Domain Model
 * 
 * Represents sales attributed to marketing channels
 * Helps measure ROI of marketing campaigns
 */
public class MarketingAttribution {
    private final String source;
    private final String campaign;
    private final long orderCount;
    private final BigDecimal totalRevenue;
    private final BigDecimal averageOrderValue;
    private final long customerCount;
    private final double conversionRate;

    public MarketingAttribution(String source, 
                               String campaign,
                               long orderCount,
                               BigDecimal totalRevenue,
                               long customerCount,
                               long totalSessions) {
        this.source = source;
        this.campaign = campaign;
        this.orderCount = orderCount;
        this.totalRevenue = totalRevenue;
        this.customerCount = customerCount;
        this.averageOrderValue = calculateAOV(totalRevenue, orderCount);
        this.conversionRate = calculateConversionRate(orderCount, totalSessions);
    }

    private static BigDecimal calculateAOV(BigDecimal revenue, long orders) {
        if (orders == 0) {
            return BigDecimal.ZERO;
        }
        return revenue.divide(BigDecimal.valueOf(orders), 2, RoundingMode.HALF_UP);
    }

    private static double calculateConversionRate(long orders, long sessions) {
        if (sessions == 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(orders)
                        .divide(BigDecimal.valueOf(sessions), 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();
    }

    public String getSource() {
        return source;
    }

    public String getCampaign() {
        return campaign;
    }

    public long getOrderCount() {
        return orderCount;
    }

    public BigDecimal getTotalRevenue() {
        return totalRevenue;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }

    public long getCustomerCount() {
        return customerCount;
    }

    public double getConversionRate() {
        return conversionRate;
    }
    
    public String getChannelKey() {
        if (campaign != null && !campaign.isEmpty()) {
            return source + " / " + campaign;
        }
        return source;
    }
}
