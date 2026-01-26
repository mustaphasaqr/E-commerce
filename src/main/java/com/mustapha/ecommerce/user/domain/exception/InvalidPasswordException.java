package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when password verification fails during login.
 */
public class InvalidPasswordException extends UserDomainException {
    public InvalidPasswordException() {
        super("Invalid password");
    }
}
