package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Release Stock Reservation
 * 
 * Purpose: Cancel stock reservation for a specific order
 * 
 * Business Rules:
 * - Idempotent (returns unchanged if no reservation exists)
 * - Restores available stock
 * 
 * Validation:
 * - Product ID cannot be null
 * - Order ID cannot be null or empty
 * 
 * Pattern: Command (immutable)
 * Use Case: Order cancellation, payment failure, order timeout
 * Note: ProductId is value object, orderId is primitive (cross-aggregate reference)
 */
public class ReleaseReservationCommand {
    
    private final ProductId productId;
    private final String orderId;
    
    public ReleaseReservationCommand(ProductId productId, String orderId) {
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
