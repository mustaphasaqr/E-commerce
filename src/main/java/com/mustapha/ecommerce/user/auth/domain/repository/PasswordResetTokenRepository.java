package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;

import java.util.Optional;

/**
 * PasswordResetToken Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for PasswordResetToken aggregate.
 * Implementation will be in infrastructure layer.
 */
public interface PasswordResetTokenRepository {
    PasswordResetToken save(PasswordResetToken token);
    Optional<PasswordResetToken> findByToken(String token);
    void delete(String token);
    void deleteAllByUserId(String userId); // Invalidate all pending resets for user
}
