package com.mustapha.ecommerce.analytics.domain.model;

import java.math.BigDecimal;

/**
 * Top Customer Value Object
 * Represents customer purchase statistics
 */
public class TopCustomer {
    private final String customerId;
    private final String customerName;
    private final String customerEmail;
    private final long totalOrders;
    private final BigDecimal totalSpent;
    private final BigDecimal averageOrderValue;

    public TopCustomer(String customerId, String customerName, String customerEmail, 
                      long totalOrders, BigDecimal totalSpent) {
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.totalOrders = totalOrders;
        this.totalSpent = totalSpent;
        this.averageOrderValue = totalOrders > 0 
            ? totalSpent.divide(BigDecimal.valueOf(totalOrders), 2, BigDecimal.ROUND_HALF_UP)
            : BigDecimal.ZERO;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public long getTotalOrders() {
        return totalOrders;
    }

    public BigDecimal getTotalSpent() {
        return totalSpent;
    }

    public BigDecimal getAverageOrderValue() {
        return averageOrderValue;
    }
}
