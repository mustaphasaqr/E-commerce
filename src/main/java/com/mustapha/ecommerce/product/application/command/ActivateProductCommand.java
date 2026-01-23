package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Activate Product
 * 
 * Purpose: Make product active and available for purchase
 * 
 * Business Rules:
 * - Cannot activate already active product
 * - Cannot activate discontinued product (terminal state)
 * 
 * Validation:
 * - Product ID cannot be null
 * 
 * Pattern: Command (immutable)
 * Note: Uses ProductId value object (matches OrderId in Order commands)
 */
public class ActivateProductCommand {
    
    private final ProductId productId;
    
    public ActivateProductCommand(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
