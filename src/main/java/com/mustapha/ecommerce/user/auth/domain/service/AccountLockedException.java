package com.mustapha.ecommerce.user.auth.domain.service;

/**
 * Exception thrown when attempting to log in to a locked account.
 * 
 * Contains remaining lockout time for user-friendly error messages.
 */
public class AccountLockedException extends RuntimeException {
    
    private final long remainingLockoutSeconds;
    
    public AccountLockedException(String message, long remainingLockoutSeconds) {
        super(message);
        this.remainingLockoutSeconds = remainingLockoutSeconds;
    }
    
    public long getRemainingLockoutSeconds() {
        return remainingLockoutSeconds;
    }
    
    public long getRemainingLockoutMinutes() {
        return (remainingLockoutSeconds + 59) / 60; // Round up
    }
}
