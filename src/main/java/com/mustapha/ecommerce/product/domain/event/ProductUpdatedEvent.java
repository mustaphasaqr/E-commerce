package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Product Updated Event
 * 
 * Generic event for product updates that don't require specific event types.
 * Used for: image additions/removals, minor attribute changes, etc.
 * 
 * For critical changes (price, details, stock), use specific events:
 * - PriceChangedEvent
 * - ProductDetailsUpdatedEvent
 * - StockUpdatedEvent
 */
public record ProductUpdatedEvent(
    String eventId,
    ProductId productId,
    String updateDescription,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public ProductUpdatedEvent(ProductId productId, String updateDescription) {
        this(
            UUID.randomUUID().toString(),
            productId,
            updateDescription,
            LocalDateTime.now()
        );
    }
    
    @Override
    public String getEventId() {
        return eventId;
    }
    
    @Override
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
