package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Thrown when attempting to use a token that has already been used.
 * Critical for security - tokens should be single-use (token rotation).
 */
public class TokenAlreadyUsedException extends AuthDomainException {
    public TokenAlreadyUsedException(String tokenType) {
        super(tokenType + " has already been used");
    }
}
