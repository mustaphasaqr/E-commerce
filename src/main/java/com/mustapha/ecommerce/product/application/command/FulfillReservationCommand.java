package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Fulfill Stock Reservation
 * 
 * Purpose: Complete stock reservation for a specific order (decrements both reserved and total)
 * 
 * Business Rules:
 * - Removes reservation from tracking
 * - Decrements total stock (physically shipped)
 * - NOT idempotent (throws exception if reservation doesn't exist)
 * 
 * Validation:
 * - Product ID cannot be null
 * - Order ID cannot be null or empty
 * 
 * Pattern: Command (immutable)
 * Use Case: Order shipment, order completion
 * Note: ProductId is value object, orderId is primitive (cross-aggregate reference)
 */
public class FulfillReservationCommand {
    
    private final ProductId productId;
    private final String orderId;
    
    public FulfillReservationCommand(ProductId productId, String orderId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        this.productId = productId;
        this.orderId = orderId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getOrderId() {
        return orderId;
    }
}
