package com.mustapha.ecommerce.user.domain.exception;

/**
 * Thrown when attempting to perform actions on a deleted user.
 */
public class UserDeletedException extends UserDomainException {
    public UserDeletedException(String userId) {
        super("User is deleted: " + userId);
    }
}
