package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Deactivate Product
 * 
 * Purpose: Make product inactive (not available for purchase)
 * 
 * Business Rules:
 * - Cannot deactivate already inactive product
 * - Cannot deactivate with reserved stock (active reservations exist)
 * - Cannot deactivate discontinued product
 * 
 * Validation:
 * - Product ID cannot be null
 * 
 * Pattern: Command (immutable)
 * Note: Uses ProductId value object (matches OrderId in Order commands)
 */
public class DeactivateProductCommand {
    
    private final ProductId productId;
    
    public DeactivateProductCommand(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
