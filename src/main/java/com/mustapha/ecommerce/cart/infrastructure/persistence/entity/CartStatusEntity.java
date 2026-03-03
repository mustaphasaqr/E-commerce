package com.mustapha.ecommerce.cart.infrastructure.persistence.entity;

/**
 * Cart Status Enumeration (Infrastructure Layer)
 * 
 * Lifecycle:
 * ACTIVE -> CONVERTED (to order)
 *        -> ABANDONED (timeout after inactivity)
 */
public enum CartStatusEntity {
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
