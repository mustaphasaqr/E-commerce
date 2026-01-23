package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Reserve Stock for Order
 * 
 * Purpose: Reserve product stock for a specific order (cross-aggregate interaction)
 * 
 * Business Rules:
 * - Idempotent per orderId (duplicate calls return unchanged)
 * - Product must be active and available for purchase
 * - Sufficient stock must be available
 * 
 * Validation:
 * - Product ID cannot be null
 * - Order ID cannot be null or empty (ensures traceability)
 * - Quantity must be positive
 * 
 * Pattern: Command (immutable)
 * Use Case: Order checkout, inventory allocation
 * Note: ProductId is value object, orderId is primitive (cross-aggregate reference)
 */
public class ReserveStockCommand {
    
    private final ProductId productId;
    private final String orderId;
    private final int quantity;
    
    public ReserveStockCommand(ProductId productId, String orderId, int quantity) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (orderId == null || orderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.productId = productId;
        this.orderId = orderId;
        this.quantity = quantity;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public int getQuantity() {
        return quantity;
    }
}
