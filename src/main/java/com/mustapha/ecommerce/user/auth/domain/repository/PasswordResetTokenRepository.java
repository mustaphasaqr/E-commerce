package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Optional;

/**
 * PasswordResetToken Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for PasswordResetToken aggregate.
 * Implementation will be in infrastructure layer (Redis recommended for TTL support).
 */
public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String tokenValue);
    void delete(String tokenValue);
    void deleteAllByUserId(UserId userId); // Delete all pending tokens for user
}
