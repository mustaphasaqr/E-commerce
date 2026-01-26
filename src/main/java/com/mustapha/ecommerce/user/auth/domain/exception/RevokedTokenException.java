package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Thrown when attempting to use a revoked token.
 * Security-critical: revoked tokens (e.g., after logout) cannot be reused.
 */
public class RevokedTokenException extends AuthDomainException {
    public RevokedTokenException(String tokenType) {
        super(tokenType + " has been revoked");
    }
}
