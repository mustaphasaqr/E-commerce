package com.mustapha.ecommerce.shared.exception;

/**
 * Business Exception
 * Base exception for business logic errors
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
