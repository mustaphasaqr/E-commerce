package com.mustapha.ecommerce.order.application.port;

import com.mustapha.ecommerce.order.application.exception.ProductNotFoundException;
import com.mustapha.ecommerce.order.domain.model.valueobject.Money;
import com.mustapha.ecommerce.order.domain.model.valueobject.ProductId;

/**
 * Product Port (Outbound Port)
 * Responsibility: Product catalog operations and validation
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: ProductService, CatalogAPI, ProductRepository
 */
public interface ProductPort {
    
    /**
     * Validate that product exists in catalog
     * 
     * @param productId Product to validate
     * @return true if product exists, false otherwise
     */
    boolean productExists(ProductId productId);
    
    /**
     * Get current price for a product
     * Used to validate order item prices match catalog
     * 
     * @param productId Product to get price for
     * @return Current product price
     * @throws ProductNotFoundException if product doesn't exist
     */
    Money getProductPrice(ProductId productId);
    
    /**
     * Get product details (name, description, etc.)
     * 
     * @param productId Product to get details for
     * @return Product information
     * @throws ProductNotFoundException if product doesn't exist
     */
    ProductInfo getProductInfo(ProductId productId);
    
    /**
     * Product Information DTO
     * Data returned from product catalog
     */
    record ProductInfo(
        ProductId productId,
        String name,
        String description,
        Money price,
        boolean active  // Is product still available for purchase?
    ) {
        /**
         * Compact constructor - validation
         */
        public ProductInfo {
            if (productId == null) {
                throw new IllegalArgumentException("Product ID cannot be null");
            }
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Product name cannot be null or blank");
            }
            if (price == null) {
                throw new IllegalArgumentException("Product price cannot be null");
            }
        }
    }
}
