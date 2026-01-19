package com.mustapha.ecommerce.product.domain.service;

import com.mustapha.ecommerce.product.domain.model.Product;
import com.mustapha.ecommerce.product.domain.exception.InsufficientStockException;

/**
 * Stock Allocation Service - Domain Service
 * Responsibility: Handle stock reservation logic across products
 * Pattern: Domain Service (pure domain, no framework dependencies!)
 * 
 * Domain Rules:
 * - Product must be active to reserve stock
 * - Stock quantity must be available
 * - Cannot reserve negative quantities
 */
public class StockAllocationService {

    /**
     * Reserve stock for a product
     * 
     * @param product the product to reserve stock for
     * @param quantity the quantity to reserve
     * @throws IllegalArgumentException if product is null or quantity is invalid
     * @throws IllegalStateException if product is inactive
     * @throws InsufficientStockException if not enough stock available
     */
    public void reserveStock(Product product, int quantity) {
        // Validation
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        // Business rule: Product must be active
        if (!product.isActive()) {
            throw new IllegalStateException("Cannot reserve stock for inactive product: " + product.getId().getValue());
        }
        
        // Business rule: Must have sufficient stock
        if (!product.isStockAvailable(quantity)) {
            throw new InsufficientStockException(
                product.getId().getValue(),
                product.getStock().getQuantity(),
                quantity
            );
        }
        
        // Execute reservation
        product.reserveStock(quantity);
    }

    /**
     * Release reserved stock back to product
     * 
     * @param product the product to restock
     * @param quantity the quantity to release
     * @throws IllegalArgumentException if product is null or quantity is invalid
     */
    public void releaseStock(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }
        
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        
        product.restock(quantity);
    }
}
