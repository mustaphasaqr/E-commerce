package com.mustapha.ecommerce.product.domain.specification;

import com.mustapha.ecommerce.product.domain.model.Product;

/**
 * In Stock Products Specification
 * 
 * Business Rule: "Product is in stock"
 * Criteria: Product is active AND has available stock
 * 
 * Pattern: Specification Pattern
 */
public class InStockSpecification implements ProductSpecification {
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        if (product == null) {
            return false;
        }
        
        return product.isActive() && product.getStock().isAvailable();
    }
}
