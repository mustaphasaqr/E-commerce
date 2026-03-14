package com.mustapha.ecommerce.shared.security.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to apply rate limiting to specific endpoints.
 * 
 * <p>Can be applied to controller methods to enforce rate limits
 * based on IP address or user identity.</p>
 * 
 * <p>Example usage:</p>
 * <pre>
 * {@code @PostMapping("/password-reset/request")
 * @RateLimit(maxRequests = 3, windowSeconds = 300, scope = RateLimitScope.IP)
 * public ResponseEntity<Void> requestPasswordReset(@RequestBody PasswordResetRequest request) {
 *     // Only 3 requests per 5 minutes per IP
 * }}
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
        /**
         * Name of the parameter to use for PARAMETER scope (e.g., "email").
         * Only used if scope = PARAMETER.
         */
        String parameterName() default "";
    
    /**
     * Maximum number of requests allowed within the time window
     */
    int maxRequests() default 5;
    
    /**
     * Time window in seconds
     */
    long windowSeconds() default 60;
    
    /**
     * Scope of rate limiting (IP-based or user-based)
     */
    RateLimitScope scope() default RateLimitScope.IP;
    
    /**
     * Custom error message when rate limit is exceeded
     */
    String message() default "Too many requests. Please try again later.";
}
