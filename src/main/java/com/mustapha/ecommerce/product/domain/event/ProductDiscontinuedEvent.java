package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Product Discontinued
 * Triggered when: Product enters terminal discontinued state
 * 
 * Use cases:
 * - Remove from search index
 * - Update catalog
 * - Stop recommendations
 * - Trigger reporting/analytics
 */
public class ProductDiscontinuedEvent implements ProductDomainEvent {
    private final String eventId;
    private final ProductId productId;
    private final LocalDateTime occurredAt;

    public ProductDiscontinuedEvent(ProductId productId) {
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

    @Override
    public String toString() {
        return "ProductDiscontinuedEvent{productId=" + productId.getValue() + "}";
    }
}
