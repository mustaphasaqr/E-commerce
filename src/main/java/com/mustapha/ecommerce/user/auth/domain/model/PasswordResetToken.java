package com.mustapha.ecommerce.user.auth.domain.model;

import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * PasswordResetToken Aggregate Root (Auth Subdomain)
 * Represents a password reset request with time-limited validity.
 * 
 * Invariants:
 * - Token must be unique
 * - User ID cannot be null
 * - Token has 24-hour validity
 * - Cannot use expired or already-used token
 * - Token can only be used once
 */
public class PasswordResetToken {
    private final String token;
    private final String userId;
    private final String email; // For sending reset email
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime usedAt;
    
    private static final int VALIDITY_HOURS = 24;

    private PasswordResetToken(String token, String userId, String email,
                              LocalDateTime createdAt, LocalDateTime expiresAt,
                              boolean used, LocalDateTime usedAt) {
        this.token = Objects.requireNonNull(token, "Token cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");
        this.used = used;
        this.usedAt = usedAt;
    }

    /**
     * Factory: Create new password reset token
     */
    public static PasswordResetToken create(String userId, String email) {
        LocalDateTime now = LocalDateTime.now();
        return new PasswordResetToken(
            UUID.randomUUID().toString(),
            userId,
            email,
            now,
            now.plusHours(VALIDITY_HOURS),
            false,
            null
        );
    }

    /**
     * Factory: Reconstitute from database
     */
    public static PasswordResetToken reconstitute(String token, String userId, String email,
                                                 LocalDateTime createdAt, LocalDateTime expiresAt,
                                                 boolean used, LocalDateTime usedAt) {
        return new PasswordResetToken(token, userId, email, createdAt, expiresAt, used, usedAt);
    }

    /**
     * Validates and marks token as used
     * Rule: Token must not be expired
     * Rule: Token must not already be used
     */
    public void use() {
        ensureNotExpired();
        ensureNotUsed();
        
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    /**
     * Checks if token is valid
     */
    public boolean isValid() {
        return !used && !isExpired();
    }

    /**
     * Checks if token is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Guards
    private void ensureNotExpired() {
        if (isExpired()) {
            throw new ExpiredTokenException("PasswordResetToken", expiresAt);
        }
    }

    private void ensureNotUsed() {
        if (used) {
            throw new TokenAlreadyUsedException(token);
        }
    }

    // Getters
    public String getToken() {
        return token;
    }

    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordResetToken that = (PasswordResetToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "PasswordResetToken{" +
                "token='***'" + // Never log actual token
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", used=" + used +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
