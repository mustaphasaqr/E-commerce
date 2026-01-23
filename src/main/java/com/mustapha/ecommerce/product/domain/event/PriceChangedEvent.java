package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when product price changes
 * 
 * Business Invariants:
 * - Old and new prices must have same currency (immutable)
 * - Old and new prices must be different (no-op changes don't emit events)
 */
public record PriceChangedEvent(
    String eventId,
    ProductId productId,
    Price oldPrice,
    Price newPrice,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public PriceChangedEvent(ProductId productId, Price oldPrice, Price newPrice) {
        this(
            UUID.randomUUID().toString(),
            productId,
            oldPrice,
            newPrice,
            LocalDateTime.now()
        );
        
        // Validation
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (oldPrice == null) {
            throw new IllegalArgumentException("Old price cannot be null");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("New price cannot be null");
        }
        
        // Business invariants
        oldPrice.ensureSameCurrency(newPrice);
        
        if (oldPrice.equals(newPrice)) {
            throw new IllegalArgumentException(
                "Price change event requires different prices. Old: " + oldPrice.getAmount() + 
                ", New: " + newPrice.getAmount()
            );
        }
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
        PriceChangedEvent that = (PriceChangedEvent) o;
        return eventId.equals(that.eventId);
    }
    
    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
