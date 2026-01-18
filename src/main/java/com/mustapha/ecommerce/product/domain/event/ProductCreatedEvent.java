package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;

/**
 * Domain event published when a product is created
 */
public class ProductCreatedEvent {
    private final ProductId productId;
    private final String sku;
    private final String name;
    private final LocalDateTime occurredOn;

    public ProductCreatedEvent(ProductId productId, String sku, String name) {
        this.productId = productId;
        this.sku = sku;
        this.name = name;
        this.occurredOn = LocalDateTime.now();
    }

    public ProductId getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getOccurredOn() {
        return occurredOn;
    }
}
