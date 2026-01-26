package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when attempting to register a user with an email that already exists.
 */
public class DuplicateEmailException extends UserDomainException {
    public DuplicateEmailException(String email) {
        super("User with email already exists: " + email);
    }
}
