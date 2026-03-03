package com.mustapha.ecommerce.cart.application.port;

import com.mustapha.ecommerce.cart.domain.model.valueobject.Money;
import com.mustapha.ecommerce.cart.domain.model.valueobject.ProductId;

/**
 * Product Port Interface
 * Application Layer - Port (for Hexagonal Architecture)
 * 
 * Defines operations needed from the product domain.
 * Implementation is provided by infrastructure adapters.
 * 
 * Pattern: Port/Adapter (Hexagonal Architecture)
 * 
 * Uses value objects:
 * - ProductId for type-safe product identifier
 * - Money for monetary values
 */
public interface ProductPort {
    
    /**
     * Get product name
     */
    String getProductName(ProductId productId);
    
    /**
     * Get product price
     */
    Money getProductPrice(ProductId productId);
    
    /**
     * Check if product exists
     */
    boolean productExists(ProductId productId);
}
