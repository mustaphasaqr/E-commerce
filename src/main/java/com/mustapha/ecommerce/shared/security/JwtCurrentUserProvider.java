package com.mustapha.ecommerce.shared.security;

import org.springframework.stereotype.Component;

/**
 * JWT Current User Provider
 * Responsibility: Extract user from JWT token
 */
@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    @Override
    public String getCurrentUserId() {
        // Extract from JWT token
        return "user-123"; // Mock
    }

    @Override
    public String getCurrentUsername() {
        // Extract from JWT token
        return "john.doe"; // Mock
    }

    @Override
    public boolean isAuthenticated() {
        // Check if user is authenticated
        return true; // Mock
    }
}
