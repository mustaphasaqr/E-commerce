package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event published when product price changes
 */
public final class PriceChangedEvent implements ProductDomainEvent {
    private final String eventId;
    private final ProductId productId;
    private final Price oldPrice;
    private final Price newPrice;
    private final LocalDateTime occurredAt;

    public PriceChangedEvent(ProductId productId, Price oldPrice, Price newPrice) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
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

    public Price getOldPrice() {
        return oldPrice;
    }

    public Price getNewPrice() {
        return newPrice;
    }
}
