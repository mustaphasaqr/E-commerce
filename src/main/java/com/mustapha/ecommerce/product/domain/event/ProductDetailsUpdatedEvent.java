package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductDetailsUpdatedEvent(
    String eventId,
    ProductId productId,
    String newName,
    String newDescription,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public ProductDetailsUpdatedEvent(ProductId productId, String newName, String newDescription) {
        this(
            UUID.randomUUID().toString(),
            productId,
            newName,
            newDescription,
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
