package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when a product is activated
 */
public final class ProductActivatedEvent implements ProductDomainEvent {
    private final String eventId;
    private final ProductId productId;
    private final LocalDateTime occurredAt;

    public ProductActivatedEvent(ProductId productId) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
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
}
