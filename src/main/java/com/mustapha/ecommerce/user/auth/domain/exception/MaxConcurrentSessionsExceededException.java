package com.mustapha.ecommerce.user.auth.domain.exception;

/**
 * Thrown when user attempts to create a session but has reached maximum concurrent sessions limit.
 * Business rule: Prevents account sharing and enforces security controls.
 */
public class MaxConcurrentSessionsExceededException extends AuthDomainException {
    private final int maxAllowed;
    private final int currentCount;
    
    public MaxConcurrentSessionsExceededException(String userId, int maxAllowed, int currentCount) {
        super("User " + userId + " has reached maximum concurrent sessions limit. Max: " + maxAllowed + ", Current: " + currentCount);
        this.maxAllowed = maxAllowed;
        this.currentCount = currentCount;
    }
    
    public int getMaxAllowed() {
        return maxAllowed;
    }
    
    public int getCurrentCount() {
        return currentCount;
    }
}
