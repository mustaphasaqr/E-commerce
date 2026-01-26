package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Base exception for all Auth subdomain exceptions.
 * Extends RuntimeException to avoid checked exception hell.
 */
public class AuthDomainException extends RuntimeException {
    public AuthDomainException(String message) {
        super(message);
    }

    public AuthDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
