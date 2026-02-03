package com.mustapha.ecommerce.user.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * EmailVerificationToken Aggregate Root (Auth Subdomain)
 * Represents an email verification request with time-limited validity.
 * 
 * Invariants:
 * - Token must be unique
 * - User ID cannot be null
 * - Token has 24-hour validity
 * - Cannot use expired or already-used token
 * - Token can only be used once
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmailVerificationToken implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String token;
    private final String userId;
    private final String email;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean used;
    private LocalDateTime usedAt;
    
    private static final int VALIDITY_HOURS = 24;

    @JsonCreator
    public EmailVerificationToken(
            @JsonProperty("token") String token,
            @JsonProperty("userId") String userId,
            @JsonProperty("email") String email,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("used") boolean used,
            @JsonProperty("usedAt") LocalDateTime usedAt) {
        this.token = Objects.requireNonNull(token, "Token cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");
        this.used = used;
        this.usedAt = usedAt;
    }

    public static EmailVerificationToken create(String userId, String email) {
        LocalDateTime now = LocalDateTime.now();
        return new EmailVerificationToken(
            UUID.randomUUID().toString(),
            userId,
            email,
            now,
            now.plusHours(VALIDITY_HOURS),
            false,
            null
        );
    }

    public static EmailVerificationToken reconstitute(String token, String userId, String email,
                                                     LocalDateTime createdAt, LocalDateTime expiresAt,
                                                     boolean used, LocalDateTime usedAt) {
        return new EmailVerificationToken(token, userId, email, createdAt, expiresAt, used, usedAt);
    }

    public void use() {
        ensureNotExpired();
        ensureNotUsed();
        
        this.used = true;
        this.usedAt = LocalDateTime.now();
    }

    private void ensureNotExpired() {
        if (LocalDateTime.now().isAfter(expiresAt)) {
            throw new ExpiredTokenException("EmailVerificationToken", expiresAt);
        }
    }

    private void ensureNotUsed() {
        if (used) {
            throw new TokenAlreadyUsedException("EmailVerificationToken");
        }
    }

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
        EmailVerificationToken that = (EmailVerificationToken) o;
        return Objects.equals(token, that.token);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token);
    }

    @Override
    public String toString() {
        return "EmailVerificationToken{" +
                "token='***'" +
                ", userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                ", used=" + used +
                '}';
    }
}
