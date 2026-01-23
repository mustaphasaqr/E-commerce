package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a product is created
 */
public record ProductCreatedEvent(
    String eventId,
    ProductId productId,
    String sku,
    String name,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public ProductCreatedEvent(ProductId productId, String sku, String name) {
        this(
            UUID.randomUUID().toString(),
            productId,
            sku,
            name,
            LocalDateTime.now()
        );
        
        // Validation in compact constructor pattern
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
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
        ProductCreatedEvent that = (ProductCreatedEvent) o;
        return eventId.equals(that.eventId);
    }
    
    @Override
    public int hashCode() {
        return eventId.hashCode();
    }
}
