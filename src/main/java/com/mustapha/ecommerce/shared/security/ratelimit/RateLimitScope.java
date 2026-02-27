package com.mustapha.ecommerce.shared.security.ratelimit;

/**
 * Scope for rate limiting
 */
public enum RateLimitScope {
    /**
     * Rate limit per IP address
     */
    IP,
    
    /**
     * Rate limit per authenticated user
     */
    USER,
    
    /**
     * Rate limit per specific parameter (e.g., email address)
     */
    PARAMETER
}
