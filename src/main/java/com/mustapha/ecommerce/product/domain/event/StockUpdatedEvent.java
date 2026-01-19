package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when product stock is updated
 * 
 * Enhanced with reserved quantity tracking for accurate inventory management
 */
public final class StockUpdatedEvent implements ProductDomainEvent {
    private final String eventId;
    private final ProductId productId;
    private final int previousTotal;
    private final int newTotal;
    private final int previousReserved;
    private final int newReserved;
    private final LocalDateTime occurredAt;

    public StockUpdatedEvent(ProductId productId, 
                            int previousTotal, int newTotal,
                            int previousReserved, int newReserved) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.previousTotal = previousTotal;
        this.newTotal = newTotal;
        this.previousReserved = previousReserved;
        this.newReserved = newReserved;
        this.occurredAt = LocalDateTime.now();
    }

    @Override
    public String getEventId() {
        return eventId;
    }

    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getPreviousTotal() {
        return previousTotal;
    }

    public int getNewTotal() {
        return newTotal;
    }
    
    public int getPreviousReserved() {
        return previousReserved;
    }
    
    public int getNewReserved() {
        return newReserved;
    }
    
    public int getPreviousAvailable() {
        return previousTotal - previousReserved;
    }
    
    public int getNewAvailable() {
        return newTotal - newReserved;
    }
    
    /**
     * Backwards compatibility - use getPreviousTotal()
     */
    @Deprecated
    public int getPreviousQuantity() {
        return previousTotal;
    }

    /**
     * Backwards compatibility - use getNewTotal()
     */
    @Deprecated
    public int getNewQuantity() {
        return newTotal;
    }
}
