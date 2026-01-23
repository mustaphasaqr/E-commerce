package com.mustapha.ecommerce.product.domain.exception;

/**
 * Exception thrown when attempting to modify a product that is currently in use by active orders
 * 
 * Business Context Examples:
 * - Cannot update price while product has active orders (price changes only affect future orders)
 * - Cannot update details while product has active orders (changes only affect future orders)
 * - Protects order integrity by preventing mid-flight changes
 * 
 * Design Note: This is a message-based exception since the context varies (price update, details update, etc.)
 * The productId is embedded in the message but not extracted as a separate field since the
 * exception is caught and translated to HTTP 409 Conflict with the full message displayed to users.
 */
public final class ProductInUseException extends RuntimeException {
    public ProductInUseException(String message) {
        super(message);
    }

    public ProductInUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
