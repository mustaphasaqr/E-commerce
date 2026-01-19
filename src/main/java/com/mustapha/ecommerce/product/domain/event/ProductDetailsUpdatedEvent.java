package com.mustapha.ecommerce.product.domain.event;

import com.mustapha.ecommerce.product.domain.ProductDomainEvent;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Product Details Updated
 * Triggered when: Product name or description changes
 * 
 * Use cases:
 * - Update search index
 * - Invalidate cache
 * - Sync to catalog
 * - Audit trail
 */
public class ProductDetailsUpdatedEvent implements ProductDomainEvent {
    private final String eventId;
    private final ProductId productId;
    private final String oldName;
    private final String newName;
    private final String oldDescription;
    private final String newDescription;
    private final LocalDateTime occurredAt;

    public ProductDetailsUpdatedEvent(ProductId productId, String oldName, String newName, 
                                     String oldDescription, String newDescription) {
        this.eventId = UUID.randomUUID().toString();
        this.productId = productId;
        this.oldName = oldName;
        this.newName = newName;
        this.oldDescription = oldDescription;
        this.newDescription = newDescription;
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

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }

    public String getOldDescription() {
        return oldDescription;
    }

    public String getNewDescription() {
        return newDescription;
    }
    
    public boolean nameChanged() {
        return !oldName.equals(newName);
    }
    
    public boolean descriptionChanged() {
        return (oldDescription == null && newDescription != null) ||
               (oldDescription != null && !oldDescription.equals(newDescription));
    }

    @Override
    public String toString() {
        return "ProductDetailsUpdatedEvent{productId=" + productId.getValue() + 
               ", nameChanged=" + nameChanged() + 
               ", descriptionChanged=" + descriptionChanged() + "}";
    }
}
