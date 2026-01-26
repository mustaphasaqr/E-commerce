package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Thrown when login credentials are invalid (wrong email/username or password).
 * Authentication failure - security-critical exception.
 */
public class InvalidCredentialsException extends AuthDomainException {
    public InvalidCredentialsException(String identifier) {
        super("Invalid credentials for: " + identifier);
    }
    
    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
