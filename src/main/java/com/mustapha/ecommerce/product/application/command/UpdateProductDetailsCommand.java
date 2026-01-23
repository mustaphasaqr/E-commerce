package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Update Product Details
 * 
 * Purpose: Update product name and description
 * 
 * Business Rules:
 * - Name cannot be empty or exceed 200 characters
 * - Description is optional
 * 
 * Validation:
 * - Product ID cannot be null
 * - Name cannot be null or empty
 * 
 * Pattern: Command (immutable)
 * Note: Uses ProductId value object (matches OrderId in Order commands)
 */
public class UpdateProductDetailsCommand {
    
    private final ProductId productId;
    private final String name;
    private final String description;
    
    public UpdateProductDetailsCommand(ProductId productId, String name, String description) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (name.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
        }
        this.productId = productId;
        this.name = name;
        this.description = description;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public String getName() {
        return name;
    }
    
    public String getDescription() {
        return description;
    }
}
