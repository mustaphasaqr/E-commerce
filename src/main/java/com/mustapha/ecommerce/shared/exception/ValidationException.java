package com.mustapha.ecommerce.shared.exception;

/**
 * Validation Exception
 * Exception for validation errors
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
