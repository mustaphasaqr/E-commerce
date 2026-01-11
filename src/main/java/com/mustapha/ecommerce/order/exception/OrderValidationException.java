package com.mustapha.ecommerce.order.exception;

/**
 * Order Validation Exception
 */
public class OrderValidationException extends RuntimeException {
    public OrderValidationException(String message) {
        super(message);
    }
}
