package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Discontinue Product
 * 
 * Purpose: Mark product as discontinued (terminal state - irreversible)
 * 
 * Business Rules:
 * - Terminal state (cannot be reactivated)
 * - Product becomes inactive and unavailable
 * - No modifications allowed after discontinuation
 * 
 * Validation:
 * - Product ID cannot be null
 * 
 * Pattern: Command (immutable)
 * Note: Advanced DDD pattern - demonstrates irreversible state transitions
 * Uses ProductId value object (matches OrderId in Order commands)
 */
public class DiscontinueProductCommand {
    
    private final ProductId productId;
    
    public DiscontinueProductCommand(ProductId productId) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        this.productId = productId;
    }
    
    public ProductId getProductId() {
        return productId;
    }
}
