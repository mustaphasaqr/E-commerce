package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;

/**
 * Domain event published when product stock is updated
 */
public class StockUpdatedEvent {
    private final ProductId productId;
    private final int previousQuantity;
    private final int newQuantity;
    private final LocalDateTime occurredOn;

    public StockUpdatedEvent(ProductId productId, int previousQuantity, int newQuantity) {
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.occurredOn = LocalDateTime.now();
    }

    public ProductId getProductId() {
        return productId;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
