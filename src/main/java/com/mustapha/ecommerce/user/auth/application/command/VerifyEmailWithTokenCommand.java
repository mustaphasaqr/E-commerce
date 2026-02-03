package com.mustapha.ecommerce.user.auth.application.command;

public class VerifyEmailWithTokenCommand {
    private final String token;

    public VerifyEmailWithTokenCommand(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
