package com.mustapha.ecommerce.user.auth.application.command;

import com.mustapha.ecommerce.user.auth.domain.model.valueobject.Credentials;

/**
 * Login Command (Input DTO)
 * Responsibility: Transfer login credentials and metadata
 * Pattern: Command (CQS)
 * 
 * Note: Uses Credentials value object for type safety
 */
public class LoginCommand {
    
    private final Credentials credentials;
    private final String ipAddress;
    private final String userAgent;
    
    public LoginCommand(Credentials credentials, String ipAddress, String userAgent) {
        this.credentials = credentials;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
    
    public Credentials getCredentials() {
        return credentials;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
}
