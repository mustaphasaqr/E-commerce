package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when there is insufficient stock
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productId, int available, int requested) {
        super(String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
            productId, available, requested));
    }
}
