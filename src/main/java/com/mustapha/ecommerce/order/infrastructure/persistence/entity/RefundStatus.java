package com.mustapha.ecommerce.order.infrastructure.persistence.entity;

/**
 * Refund Status Enumeration
 * 
 * Lifecycle:
 * NONE -> REQUESTED -> APPROVED -> COMPLETED
 *         or
 *         -> REJECTED
 */
public enum RefundStatus {
    /**
     * No refund requested
     */
    NONE,
    
    /**
     * Customer requested a refund
     */
    REQUESTED,
    
    /**
     * Refund request approved by admin
     */
    APPROVED,
    
    /**
     * Refund request rejected
     */
    REJECTED,
    
    /**
     * Refund completed and money returned
     */
    COMPLETED
}
