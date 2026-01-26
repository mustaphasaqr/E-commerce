package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when attempting an operation on a user in an invalid state.
 * Example: Activating an already active user, blocking a blocked user, etc.
 */
public class InvalidUserStateException extends UserDomainException {
    public InvalidUserStateException(String message) {
        super(message);
    }
}
