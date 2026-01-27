package com.mustapha.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Refresh Token Request DTO - Complete API Contract
 * Responsibility: API contract for token refresh
 * 
 * Contains:
 * - Refresh token (for obtaining new access token)
 */
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    public RefreshTokenRequest() {
    }

    public RefreshTokenRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
