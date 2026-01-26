package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when attempting to register a user with a username that already exists.
 */
public class DuplicateUsernameException extends UserDomainException {
    public DuplicateUsernameException(String username) {
        super("User with username already exists: " + username);
    }
}
