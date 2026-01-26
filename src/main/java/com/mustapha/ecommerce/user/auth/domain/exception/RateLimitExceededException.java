package com.mustapha.ecommerce.user.auth.domain.exception;

import java.time.LocalDateTime;

/**
 * Thrown when login attempts exceed rate limit.
 * Prevents brute-force attacks.
 */
public class RateLimitExceededException extends AuthDomainException {
    private final LocalDateTime lockedUntil;
    
    public RateLimitExceededException(String message, LocalDateTime lockedUntil) {
        super(message);
        this.lockedUntil = lockedUntil;
    }
    
    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }
}
