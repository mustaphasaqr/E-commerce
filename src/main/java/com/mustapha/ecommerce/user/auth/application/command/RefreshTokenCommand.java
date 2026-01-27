package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;

/**
 * Refresh Token Command
 * Responsibility: Transfer refresh token for rotation
 */
public class RefreshTokenCommand {
    
    private final UserId userId;
    private final String refreshToken;
    private final String ipAddress;
    private final String userAgent;
    
    public RefreshTokenCommand(UserId userId, String refreshToken, String ipAddress, String userAgent) {
        this.userId = userId;
        this.refreshToken = refreshToken;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public UserId getUserId() {
        return userId;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
}
