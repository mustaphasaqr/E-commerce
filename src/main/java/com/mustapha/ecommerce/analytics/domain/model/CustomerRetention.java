package com.mustapha.ecommerce.analytics.domain.model;

/**
 * Customer Retention Value Object
 * Represents customer retention metrics
 */
public class CustomerRetention {
    private final long totalCustomers;
    private final long returningCustomers;
    private final long newCustomers;
    private final double retentionRate;
    private final double churnRate;

    public CustomerRetention(long totalCustomers, long returningCustomers, long newCustomers) {
        this.totalCustomers = totalCustomers;
        this.returningCustomers = returningCustomers;
        this.newCustomers = newCustomers;
        this.retentionRate = totalCustomers > 0 
            ? (double) returningCustomers / totalCustomers * 100.0 
            : 0.0;
        this.churnRate = totalCustomers > 0 
            ? 100.0 - retentionRate 
            : 0.0;
    }

    public long getTotalCustomers() {
        return totalCustomers;
    }

    public long getReturningCustomers() {
        return returningCustomers;
    }

    public long getNewCustomers() {
        return newCustomers;
    }

    public double getRetentionRate() {
        return retentionRate;
    }

    public double getChurnRate() {
        return churnRate;
    }
}
