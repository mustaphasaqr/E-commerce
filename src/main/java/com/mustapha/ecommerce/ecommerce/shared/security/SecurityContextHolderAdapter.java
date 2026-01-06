package com.mustapha.ecommerce.ecommerce.shared.security;

import org.springframework.stereotype.Component;

/**
 * Security Context Holder Adapter
 * Responsibility: Adapt Spring Security context
 */
@Component
public class SecurityContextHolderAdapter implements CurrentUserProvider {

    @Override
    public String getCurrentUserId() {
        // Get from Spring Security context
        return null;
    }

    @Override
    public String getCurrentUsername() {
        // Get from Spring Security context
        return null;
    }

    @Override
    public boolean isAuthenticated() {
        // Check Spring Security context
        return false;
    }
}
