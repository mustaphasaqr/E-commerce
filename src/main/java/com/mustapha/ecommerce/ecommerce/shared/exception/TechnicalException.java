package com.mustapha.ecommerce.ecommerce.shared.exception;

/**
 * Technical Exception
 * Exception for technical/infrastructure errors
 */
public class TechnicalException extends RuntimeException {
    public TechnicalException(String message) {
        super(message);
    }

    public TechnicalException(String message, Throwable cause) {
        super(message, cause);
    }
}
