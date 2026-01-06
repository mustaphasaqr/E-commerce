package com.mustapha.ecommerce.ecommerce.order.exception;

/**
 * Order Not Found Exception
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String message) {
        super(message);
    }
}
