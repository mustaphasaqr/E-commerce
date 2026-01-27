package com.mustapha.ecommerce.user.dto;

/**
 * Login Response DTO - Complete API Contract
 * Responsibility: Authentication result with tokens and user info
 * 
 * Contains:
 * - Access token (JWT for API authentication)
 * - Refresh token (long-lived token rotation)
 * - Session ID (server-side session tracking)
 * - Token expiration (in seconds)
 * - User information (full UserResponse)
 */
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String sessionId;
    private long expiresIn; // seconds
    private UserResponse user;

    public LoginResponse() {
    }

    public LoginResponse(String accessToken, String refreshToken, String sessionId, 
                        long expiresIn, UserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.sessionId = sessionId;
        this.expiresIn = expiresIn;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }
}
