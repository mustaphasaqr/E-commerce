package com.mustapha.ecommerce.user.dto;

import jakarta.validation.constraints.NotBlank;

public class VerifyEmailWithTokenRequest {
    @NotBlank(message = "Verification token is required")
    private String token;

    public VerifyEmailWithTokenRequest() {
    }

    public VerifyEmailWithTokenRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
