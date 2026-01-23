package com.mustapha.ecommerce.product.application.command;

import com.mustapha.ecommerce.product.domain.model.valueobject.Price;
import com.mustapha.ecommerce.product.domain.model.valueobject.ProductId;

/**
 * Command: Update Product Price
 * 
 * Purpose: Change product price (with typo protection)
 * 
 * Business Rules:
 * - Currency must match existing price currency (immutable)
 * - Price change limited to 10x increase / 90% decrease (typo protection)
 * - NO dependency on Order bounded context (reviewer approved)
 * 
 * Validation:
 * - Product ID cannot be null
 * - New price cannot be null
 * 
 * Pattern: Command (immutable)
 * Note: Product aggregate handles price change validation independently
 * Uses ProductId and Price value objects (matches OrderId and Money in Order commands)
 */
public class UpdatePriceCommand {
    
    private final ProductId productId;
    private final Price newPrice;
    
    public UpdatePriceCommand(ProductId productId, Price newPrice) {
        if (productId == null) {
            throw new IllegalArgumentException("Product ID cannot be null");
        }
        if (newPrice == null) {
            throw new IllegalArgumentException("New price cannot be null");
        }
        this.productId = productId;
        this.newPrice = newPrice;
    }
    
    public ProductId getProductId() {
        return productId;
    }
    
    public Price getNewPrice() {
        return newPrice;
    }
}
