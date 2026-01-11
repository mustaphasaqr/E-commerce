package com.mustapha.ecommerce.shared.security;

/**
 * Current User Provider Interface
 * Responsibility: Provide current authenticated user context
 */
public interface CurrentUserProvider {
    String getCurrentUserId();
    String getCurrentUsername();
    boolean isAuthenticated();
}
