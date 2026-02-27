package com.mustapha.ecommerce.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Global API Rate Limiting Filter
 * 
 * Protects ALL API endpoints from abuse
 * - Per-IP rate limiting: 100 requests per minute
 * - Per-User rate limiting: 200 requests per minute
 * 
 * Returns 429 Too Many Requests when limit exceeded
 */
@Component
public class GlobalApiRateLimitFilter extends OncePerRequestFilter {

    private static final String IP_RATE_LIMIT_PREFIX = "api:rate:ip:";
    private static final String USER_RATE_LIMIT_PREFIX = "api:rate:user:";
    private static final int IP_LIMIT = 100;  // requests per minute
    private static final int USER_LIMIT = 200;  // requests per minute
    private static final long WINDOW_SECONDS = 60;

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean rateLimitingEnabled;

    public GlobalApiRateLimitFilter(RedisTemplate<String, Object> redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        // Enable rate limiting unless explicitly disabled via property
        this.rateLimitingEnabled = !environment.getProperty("rate-limiting.enabled", "true").equals("false");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // Skip rate limiting if disabled via configuration
        if (!rateLimitingEnabled) {
            filterChain.doFilter(request, response);
            return;
        }
        
        // Skip rate limiting for non-API endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String ipAddress = extractIpAddress(request);
        
        // Check IP-based rate limit
        if (!checkRateLimit(IP_RATE_LIMIT_PREFIX + ipAddress, IP_LIMIT)) {
            response.setStatus(429);
            response.setContentType("application/json");
            
            // Add rate limit headers
            Long ttl = redisTemplate.getExpire(IP_RATE_LIMIT_PREFIX + ipAddress, TimeUnit.SECONDS);
            long retryAfter = (ttl != null && ttl > 0) ? ttl : WINDOW_SECONDS;
            
            response.setHeader("X-RateLimit-Limit", String.valueOf(IP_LIMIT));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + retryAfter));
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            
            response.getWriter().write("{\"error\":\"Too many requests from your IP. Please try again later.\"}");
            return;
        }

        // Check user-based rate limit if authenticated
        String userId = extractUserId(request);
        if (userId != null && !checkRateLimit(USER_RATE_LIMIT_PREFIX + userId, USER_LIMIT)) {
            response.setStatus(429);
            response.setContentType("application/json");
            
            // Add rate limit headers
            Long ttl = redisTemplate.getExpire(USER_RATE_LIMIT_PREFIX + userId, TimeUnit.SECONDS);
            long retryAfter = (ttl != null && ttl > 0) ? ttl : WINDOW_SECONDS;
            
            response.setHeader("X-RateLimit-Limit", String.valueOf(USER_LIMIT));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("X-RateLimit-Reset", String.valueOf(System.currentTimeMillis() / 1000 + retryAfter));
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            
            response.getWriter().write("{\"error\":\"Too many requests. Please slow down.\"}");
            return;
        }

        // Add rate limit headers for successful requests
        addRateLimitHeaders(response, IP_RATE_LIMIT_PREFIX + ipAddress, IP_LIMIT);
        if (userId != null) {
            addRateLimitHeaders(response, USER_RATE_LIMIT_PREFIX + userId, USER_LIMIT);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check and increment rate limit counter
     * 
     * @param key Redis key for this rate limit
     * @param limit Maximum requests allowed
     * @return true if within limit, false if exceeded
     */
    private boolean checkRateLimit(String key, int limit) {
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == null) {
            return true;
        }
        
        // Set expiration on first request
        if (count == 1) {
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
        
        return count <= limit;
    }
    
    /**
     * Add rate limit headers to response
     */
    private void addRateLimitHeaders(HttpServletResponse response, String key, int limit) {
        Long count = redisTemplate.opsForValue().get(key) != null ? 
            ((Number) redisTemplate.opsForValue().get(key)).longValue() : 0L;
        
        long remaining = Math.max(0, limit - count);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        long reset = System.currentTimeMillis() / 1000 + (ttl != null && ttl > 0 ? ttl : WINDOW_SECONDS);
        
        response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
        response.setHeader("X-RateLimit-Reset", String.valueOf(reset));
    }

    /**
     * Extract IP address (handles X-Forwarded-For)
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract userId from Authorization header (if authenticated)
     */
    private String extractUserId(HttpServletRequest request) {
        // This will be set by JwtAuthenticationFilter if present
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        
        return null;
    }
}
