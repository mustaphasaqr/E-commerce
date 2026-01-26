package com.mustapha.ecommerce.user.infrastructure.exception;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Infrastructure Exception - User Not Found
 * 
 * Thrown when a user cannot be found in the repository.
 * This is a technical/persistence concern, not a business rule violation.
 */
public final class UserNotFoundException extends RuntimeException {
    
    public UserNotFoundException(UserId userId) {
        super("User not found with ID: " + userId);
    }

    public UserNotFoundException(String email) {
        super("User not found with email: " + email);
    }

    public UserNotFoundException(String username, boolean byUsername) {
        super("User not found with username: " + username);
    }
}
