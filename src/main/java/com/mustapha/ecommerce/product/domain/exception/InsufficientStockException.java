package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when there is insufficient stock for a reservation or purchase
 * 
 * Business Context: Stock reservation failed due to insufficient available quantity
 */
public final class InsufficientStockException extends RuntimeException {
    private final String productId;
    private final int available;
    private final int requested;

    public InsufficientStockException(String productId, int available, int requested) {
        super(String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
            productId, available, requested));
        this.productId = productId;
        this.available = available;
        this.requested = requested;
    }

    public InsufficientStockException(String productId, int available, int requested, Throwable cause) {
        super(String.format("Insufficient stock for product %s. Available: %d, Requested: %d", 
            productId, available, requested), cause);
        this.productId = productId;
        this.available = available;
        this.requested = requested;
    }

    public String getProductId() {
        return productId;
    }

    public int getAvailable() {
        return available;
    }

    public int getRequested() {
        return requested;
    }
}
