package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when product stock is updated
 * 
 * Enhanced with reserved quantity tracking for accurate inventory management
 * 
 * Business Invariants:
 * - All quantities must be non-negative
 * - Reserved quantity must not exceed total (both previous and new states)
 */
public record StockUpdatedEvent(
    String eventId,
    ProductId productId,
    int previousTotal,
    int newTotal,
    int previousReserved,
    int newReserved,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public StockUpdatedEvent(ProductId productId, 
                            int previousTotal, int newTotal,
                            int previousReserved, int newReserved) {
        this(
            UUID.randomUUID().toString(),
            productId,
            previousTotal,
            newTotal,
            previousReserved,
            newReserved,
            LocalDateTime.now()
        );
        
        // Validation
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        
        // Business invariants: Non-negative quantities
        if (previousTotal < 0 || newTotal < 0) {
            throw new IllegalArgumentException(
                "Total stock quantities cannot be negative. Previous: " + previousTotal + ", New: " + newTotal
            );
        }
        if (previousReserved < 0 || newReserved < 0) {
            throw new IllegalArgumentException(
                "Reserved stock quantities cannot be negative. Previous: " + previousReserved + ", New: " + newReserved
            );
        }
        
        // Business invariant: Reserved <= Total
        if (previousReserved > previousTotal) {
            throw new IllegalArgumentException(
                "Previous reserved (" + previousReserved + ") cannot exceed previous total (" + previousTotal + ")"
            );
        }
        if (newReserved > newTotal) {
            throw new IllegalArgumentException(
                "New reserved (" + newReserved + ") cannot exceed new total (" + newTotal + ")"
            );
        }
    }
    
    public int getPreviousAvailable() {
        return previousTotal - previousReserved;
    }
    
    public int getNewAvailable() {
        return newTotal - newReserved;
    }
    
    /**
     * Backwards compatibility - use previousTotal()
     */
    @Deprecated
    public int getPreviousQuantity() {
        return previousTotal;
    }

    /**
     * Backwards compatibility - use newTotal()
     */
    @Deprecated
    public int getNewQuantity() {
        return newTotal;
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StockUpdatedEvent that = (StockUpdatedEvent) o;
        return eventId.equals(that.eventId);
    }
    
    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
