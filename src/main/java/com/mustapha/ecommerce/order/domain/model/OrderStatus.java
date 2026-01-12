package com.mustapha.ecommerce.order.domain.model;

/**
 * Order Status Enumeration - Rich Domain Model
 * Represents the complete lifecycle state of an order
 * Payment state is part of this flow (no separate isPaid flag)
 * 
 * Valid transitions:
 * PENDING → CONFIRMED → PAID → PROCESSING → SHIPPED → DELIVERED
 * Any (except SHIPPED/DELIVERED) → CANCELLED
 */
public enum OrderStatus {
    PENDING,      // Initial state - items being added
    CONFIRMED,    // Order confirmed - ready for payment
    PAID,         // Payment received - ready for processing
    PROCESSING,   // Being prepared/packaged
    SHIPPED,      // In transit
    DELIVERED,    // Successfully delivered
    CANCELLED;    // Cancelled by user/system
    
    // ========== Domain Behavior ==========
    
    /**
     * Check if status represents a paid order
     */
    public boolean isPaid() {
        return this == PAID || this == PROCESSING || this == SHIPPED || this == DELIVERED;
    }
    
    /**
     * Check if status is terminal (no further transitions possible)
     */
    public boolean isTerminal() {
        return this == DELIVERED || this == CANCELLED;
    }
    
    /**
     * Check if status can be cancelled
     */
    public boolean isCancellable() {
        return this != SHIPPED && this != DELIVERED;
    }
    
    /**
     * Check if order can be modified (items added/removed)
     */
    public boolean isModifiable() {
        return this == PENDING;
    }
    
    /**
     * Validate if transition to target status is allowed
     * Business rule enforcement at enum level
     */
    public boolean canTransitionTo(OrderStatus target) {
        if (this == target) {
            return false; // Already in this status
        }
        
        switch (this) {
            case PENDING:
                return target == CONFIRMED || target == CANCELLED;
            case CONFIRMED:
                return target == PAID || target == CANCELLED;
            case PAID:
                return target == PROCESSING || target == CANCELLED;
            case PROCESSING:
                return target == SHIPPED || target == CANCELLED;
            case SHIPPED:
                return target == DELIVERED; // Cannot cancel after shipping
            case DELIVERED:
            case CANCELLED:
                return false; // Terminal states - no transitions
            default:
                return false;
        }
    }
}
