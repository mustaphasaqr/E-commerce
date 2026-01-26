package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Thrown when a token is invalid, malformed, or not found.
 */
public class InvalidTokenException extends AuthDomainException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
