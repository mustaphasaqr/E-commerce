package com.mustapha.ecommerce.user.domain.exception;

/**
 * Base exception for all User domain exceptions.
 * Extends RuntimeException to avoid checked exception hell.
 */
public class UserDomainException extends RuntimeException {
    public UserDomainException(String message) {
        super(message);
    }

    public UserDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
