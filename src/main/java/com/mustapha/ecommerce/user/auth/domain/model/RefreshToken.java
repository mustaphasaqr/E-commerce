package com.mustapha.ecommerce.user.auth.domain.model;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.RevokedTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * RefreshToken Aggregate Root (Auth Subdomain)
 * Represents a refresh token for obtaining new access tokens without re-authentication.
 * 
 * Invariants:
 * - Token value cannot be null
 * - User ID cannot be null
 * - Token must have creation and expiry times
 * - Cannot use expired or revoked token
 * - Token can only be used once (rotate on use)
 */@JsonIgnoreProperties(ignoreUnknown = true)public class RefreshToken implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String tokenValue;
    private final String userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean revoked;
    private LocalDateTime revokedAt;
    private LocalDateTime usedAt;
    
    // Refresh token validity (longer than session)
    private static final int TOKEN_VALIDITY_DAYS = 30;

    @JsonCreator
    public RefreshToken(
            @JsonProperty("tokenValue") String tokenValue,
            @JsonProperty("userId") String userId,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("revoked") boolean revoked,
            @JsonProperty("revokedAt") LocalDateTime revokedAt,
            @JsonProperty("usedAt") LocalDateTime usedAt) {
        this.tokenValue = Objects.requireNonNull(tokenValue, "Token value cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");
        this.revoked = revoked;
        this.revokedAt = revokedAt;
        this.usedAt = usedAt;
    }

    /**
     * Factory: Create new refresh token
     */
    public static RefreshToken create(String userId) {
        LocalDateTime now = LocalDateTime.now();
        return new RefreshToken(
            UUID.randomUUID().toString(),
            userId,
            now,
            now.plusDays(TOKEN_VALIDITY_DAYS),
            false,
            null,
            null
        );
    }

    /**
     * Factory: Reconstitute from database
     */
    public static RefreshToken reconstitute(String tokenValue, String userId, LocalDateTime createdAt,
                                           LocalDateTime expiresAt, boolean revoked, LocalDateTime revokedAt,
                                           LocalDateTime usedAt) {
        return new RefreshToken(tokenValue, userId, createdAt, expiresAt, revoked, revokedAt, usedAt);
    }

    /**
     * Uses the token to refresh session
     * Rule: Token can only be used once (token rotation for security)
     * Rule: Cannot use expired or revoked token
     */
    public void use() {
        ensureNotRevoked();
        ensureNotExpired();
        ensureNotUsed();
        
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Revokes the token (logout all devices, security breach, etc.)
     */
    public void revoke() {
        if (revoked) {
            return; // Idempotent
        }
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }

    /**
     * Checks if token is valid
     */
    public boolean isValid() {
        return !revoked && !isExpired() && usedAt == null;
    }

    /**
     * Checks if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Guards
    private void ensureNotRevoked() {
        if (revoked) {
            throw new RevokedTokenException(tokenValue);
        }
    }

    private void ensureNotExpired() {
        if (isExpired()) {
            throw new ExpiredTokenException("RefreshToken", expiresAt);
        }
    }

    private void ensureNotUsed() {
        if (usedAt != null) {
            throw new TokenAlreadyUsedException(tokenValue);
        }
    }

    // Getters
    public String getTokenValue() {
        return tokenValue;
    }

    public String getUserId() {
        return userId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RefreshToken that = (RefreshToken) o;
        return Objects.equals(tokenValue, that.tokenValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenValue);
    }

    @Override
    public String toString() {
        return "RefreshToken{" +
                "tokenValue='***'" + // Never log actual token
                ", userId='" + userId + '\'' +
                ", revoked=" + revoked +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
