package com.mustapha.ecommerce.order.infrastructure.persistence.entity;

/**
 * Cart Status Enumeration
 * 
 * Lifecycle:
 * ACTIVE -> CONVERTED (to order)
 *        -> ABANDONED (timeout after inactivity)
 */
public enum CartStatus {
    /**
     * Cart is active and can be modified
     */
    ACTIVE,
    
    /**
     * Cart was converted to an order
     */
    CONVERTED,
    
    /**
     * Cart was abandoned (no activity for 24+ hours)
     */
    ABANDONED
}
