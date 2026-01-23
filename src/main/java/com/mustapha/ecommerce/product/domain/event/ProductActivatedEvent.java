package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductActivatedEvent(
    String eventId,
    ProductId productId,
    LocalDateTime occurredAt
) implements ProductDomainEvent {
    
    public ProductActivatedEvent(ProductId productId) {
        this(
            UUID.randomUUID().toString(),
            productId,
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

