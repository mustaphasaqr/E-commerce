package com.mustapha.ecommerce.product.domain.specification;

import com.mustapha.ecommerce.product.domain.model.Product;

/**
 * In Stock Products Specification
 * 
 * Business Rule: "Product is in stock and available for purchase"
 * 
 * Criteria:
 * - Product is active (not deactivated)
 * - Product is available for purchase (orderable)
 * - Product is not discontinued (terminal state)
 * - Product has available stock (getAvailableQuantity() > 0)
 * 
 * Pattern: Specification Pattern
 * 
 * Use this for:
 * - Filtering catalog products
 * - Checking product availability
 * - Search/filter queries
 */
public final class InStockSpecification implements ProductSpecification {
    
    @Override
    public boolean isSatisfiedBy(Product product) {
        if (product == null) {
            return false;
        }
        
        return product.isActive() 
            && product.isAvailableForPurchase()
            && !product.isDiscontinued()
            && product.getStock().getAvailableQuantity() > 0;
    }
}
