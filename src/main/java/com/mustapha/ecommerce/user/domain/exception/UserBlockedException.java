package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when a user is blocked and attempts to perform an action.
 */
public class UserBlockedException extends UserDomainException {
    public UserBlockedException(String reason) {
        super("User is blocked: " + reason);
    }
}
