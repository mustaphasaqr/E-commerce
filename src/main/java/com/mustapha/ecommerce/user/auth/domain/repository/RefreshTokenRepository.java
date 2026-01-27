package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

import java.util.Optional;

/**
 * RefreshToken Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for RefreshToken aggregate.
 * Implementation will be in infrastructure layer (Redis recommended for TTL support).
 */
public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String tokenValue);
    void delete(String tokenValue);
    void deleteAllByUserId(UserId userId); // Revoke all tokens for user (e.g., on password change)
}
