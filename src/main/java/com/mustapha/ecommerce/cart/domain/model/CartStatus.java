package com.mustapha.ecommerce.cart.domain.model;

/**
 * Cart Status Enumeration
 * Domain Layer - Business State
 * 
 * Represents the lifecycle states of a shopping cart.
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
