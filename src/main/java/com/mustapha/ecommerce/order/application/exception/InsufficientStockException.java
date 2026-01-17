package com.mustapha.ecommerce.order.application.exception;

import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Insufficient Stock Exception
 * Thrown when trying to reserve/confirm stock that is not available
 * Layer: Application (orchestration failure, not domain rule violation)
 * 
 * This is NOT a domain exception because:
 * - Stock availability is external system concern (Infrastructure)
 * - Domain only knows about order items, not inventory levels
 */
public final class InsufficientStockException extends RuntimeException {
    
    private final ProductId productId;
    private final int requestedQuantity;
    private final int availableQuantity;
    
    public InsufficientStockException(ProductId productId, int requestedQuantity, int availableQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %d, Available: %d",
            productId.getValue(), requestedQuantity, availableQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
    }
    
    public InsufficientStockException(ProductId productId, int requestedQuantity) {
        super(String.format("Insufficient stock for product %s. Requested: %d",
            productId.getValue(), requestedQuantity));
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = 0;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public int getRequestedQuantity() {
        return requestedQuantity;
    }
    
    public int getAvailableQuantity() {
        return availableQuantity;
    }
}
