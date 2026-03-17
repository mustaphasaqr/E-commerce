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
     * Get available stock for a product
     * Used to validate order quantity doesn't exceed available inventory
     * 
     * @param productId Product to check stock for
     * @return Available stock quantity
     * @throws ProductNotFoundException if product doesn't exist
     */
    int getAvailableStock(ProductId productId);
    
    /**
     * Check if product is discontinued
     * Discontinued products should not be available for purchase
     * 
     * @param productId Product to check
     * @return true if product is discontinued, false otherwise
     * @throws ProductNotFoundException if product doesn't exist
     */
    boolean isDiscontinued(ProductId productId);

    /**
     * Check if product is available for purchase
     * Products must be active + available for purchase to be ordered
     * 
     * @param productId Product to check
     * @return true if product is available for purchase, false otherwise
     * @throws ProductNotFoundException if product doesn't exist
     */
    boolean isAvailableForPurchase(ProductId productId);
    
    /**
     * Reserve stock for an order
     * Reduces available inventory and increases reserved stock
     * 
     * @param productId Product to reserve stock for
     * @param orderId Order ID for tracking reservation
     * @param quantity Quantity to reserve
     * @throws ProductNotFoundException if product doesn't exist
     * @throws IllegalArgumentException if quantity exceeds available stock
     */
    void reserveStock(ProductId productId, String orderId, int quantity);

    /**
     * Release reserved stock for a specific order.
     * Use for payment failure/cancellation and explicit order cancellation flows.
     *
     * @param productId Product that has reserved stock
     * @param orderId Order identifier tied to reservation
     */
    void releaseReservation(ProductId productId, String orderId);

    /**
     * Fulfill reservation for a specific order.
     * Moves reserved stock into consumed stock after successful payment.
     *
     * @param productId Product that has reserved stock
     * @param orderId Order identifier tied to reservation
     */
    void fulfillReservation(ProductId productId, String orderId);
    
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
