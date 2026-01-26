package com.mustapha.ecommerce.user.auth.domain.exception;

import java.time.LocalDateTime;

/**
 * Thrown when attempting to use an expired token.
 */
public class ExpiredTokenException extends AuthDomainException {
    public ExpiredTokenException(String tokenType, LocalDateTime expiredAt) {
        super(tokenType + " expired at " + expiredAt);
    }
    
    public ExpiredTokenException(String message) {
        super(message);
    }
}
