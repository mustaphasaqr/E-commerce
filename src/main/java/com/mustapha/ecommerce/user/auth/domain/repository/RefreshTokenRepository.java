package com.mustapha.ecommerce.user.auth.domain.repository;

import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;

import java.util.Optional;

/**
 * RefreshToken Repository Interface (Port in Hexagonal Architecture)
 * 
 * Defines data access operations for RefreshToken aggregate.
 * Implementation will be in infrastructure layer.
 */
public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken token);
    Optional<RefreshToken> findByToken(String tokenValue);
    void delete(String tokenValue);
    void deleteAllByUserId(String userId); // Revoke all user tokens (e.g., on logout all devices)
}
