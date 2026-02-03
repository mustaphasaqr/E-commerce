package com.mustapha.ecommerce.user.auth.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import com.mustapha.ecommerce.user.auth.domain.exception.InvalidTokenException;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * LoginSession Aggregate Root (Auth Subdomain)
 * Represents an active user session after successful authentication.
 * 
 * Invariants:
 * - Session ID cannot be null
 * - User ID cannot be null
 * - Session must have creation and expiry times
 * - Cannot use expired session
 * - Session can only be invalidated once
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginSession implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String sessionId;
    private final String userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt;
    private boolean active;
    private LocalDateTime lastAccessedAt;
    private String ipAddress;
    private String userAgent;
    
    // Session validity
    private static final int SESSION_DURATION_HOURS = 24;

    @JsonCreator
    public LoginSession(
            @JsonProperty("sessionId") String sessionId,
            @JsonProperty("userId") String userId,
            @JsonProperty("createdAt") LocalDateTime createdAt,
            @JsonProperty("expiresAt") LocalDateTime expiresAt,
            @JsonProperty("active") boolean active,
            @JsonProperty("lastAccessedAt") LocalDateTime lastAccessedAt,
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("userAgent") String userAgent) {
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.userId = Objects.requireNonNull(userId, "User ID cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.expiresAt = Objects.requireNonNull(expiresAt, "Expires at cannot be null");
        this.active = active;
        this.lastAccessedAt = lastAccessedAt;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    /**
     * Factory: Create new session after successful login
     */
    public static LoginSession create(String userId, String ipAddress, String userAgent) {
        LocalDateTime now = LocalDateTime.now();
        return new LoginSession(
            UUID.randomUUID().toString(),
            userId,
            now,
            now.plusHours(SESSION_DURATION_HOURS),
            true,
            now,
            ipAddress,
            userAgent
        );
    }

    /**
     * Factory: Reconstitute from database
     */
    public static LoginSession reconstitute(String sessionId, String userId, LocalDateTime createdAt,
                                           LocalDateTime expiresAt, boolean active, LocalDateTime lastAccessedAt,
                                           String ipAddress, String userAgent) {
        return new LoginSession(sessionId, userId, createdAt, expiresAt, active, 
                               lastAccessedAt, ipAddress, userAgent);
    }

    /**
     * Updates last accessed time to keep session alive
     * Rule: Cannot access expired or inactive session
     */
    public void access() {
        ensureActive();
        ensureNotExpired();
        this.lastAccessedAt = LocalDateTime.now();
    }

    /**
     * Invalidates the session (logout)
     * Rule: Can only invalidate active session
     */
    public void invalidate() {
        if (!active) {
            throw new InvalidTokenException("Session " + sessionId + " is already invalidated");
        }
        this.active = false;
    }

    /**
     * Checks if session is valid
     */
    public boolean isValid() {
        return active && !isExpired();
    }

    /**
     * Checks if session is expired
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // Guards
    private void ensureActive() {
        if (!active) {
            throw new InvalidTokenException("Session " + sessionId + " is not active");
        }
    }

    private void ensureNotExpired() {
        if (isExpired()) {
            throw new ExpiredTokenException("LoginSession", expiresAt);
        }
    }

    // Getters
    public String getSessionId() {
        return sessionId;
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

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginSession that = (LoginSession) o;
        return Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
    }

    @Override
    public String toString() {
        return "LoginSession{" +
                "sessionId='" + sessionId + '\'' +
                ", userId='" + userId + '\'' +
                ", active=" + active +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
