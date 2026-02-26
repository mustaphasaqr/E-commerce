package com.mustapha.ecommerce.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security Headers Filter
 * 
 * Adds comprehensive security headers to all HTTP responses.
 * Defense-in-depth: Even if application logic has vulnerabilities, 
 * these headers provide browser-level protection.
 * 
 * Headers Added:
 * 1. X-Frame-Options: Prevents clickjacking attacks
 * 2. X-Content-Type-Options: Prevents MIME sniffing
 * 3. X-XSS-Protection: Enables browser XSS filter (legacy support)
 * 4. Strict-Transport-Security (HSTS): Forces HTTPS
 * 5. Content-Security-Policy (CSP): Prevents XSS, injection attacks
 * 6. Referrer-Policy: Controls referrer information leakage
 * 7. Permissions-Policy: Restricts browser features (camera, mic, etc.)
 * 
 * Executes FIRST in filter chain (HIGHEST_PRECEDENCE + 1) to ensure all responses are protected.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class SecurityHeadersFilter extends OncePerRequestFilter {

    /**
     * Content Security Policy (CSP) - Defense against XSS and injection attacks
     * 
     * Policy Breakdown:
     * - default-src 'self': Only load resources from same origin by default
     * - script-src 'self': Scripts only from same origin (no inline scripts!)
     * - style-src 'self' 'unsafe-inline': Styles from same origin + inline (needed for some UI frameworks)
     * - img-src 'self' data: https:: Images from same origin, data URIs, or HTTPS
     * - font-src 'self': Fonts only from same origin
     * - connect-src 'self': AJAX/WebSocket connections only to same origin
     * - frame-ancestors 'none': Cannot be embedded in iframe (redundant with X-Frame-Options, but more powerful)
     * - base-uri 'self': Prevents base tag injection attacks
     * - form-action 'self': Forms can only submit to same origin
     * - upgrade-insecure-requests: HTTP → HTTPS auto-upgrade
     * 
     * Note: 'unsafe-inline' for styles is a calculated trade-off for UI frameworks.
     * Remove it when using CSS modules or styled-components in production.
     */
    private static final String CONTENT_SECURITY_POLICY = 
        "default-src 'self'; " +
        "script-src 'self'; " +
        "style-src 'self' 'unsafe-inline'; " +
        "img-src 'self' data: https:; " +
        "font-src 'self'; " +
        "connect-src 'self'; " +
        "frame-ancestors 'none'; " +
        "base-uri 'self'; " +
        "form-action 'self'; " +
        "upgrade-insecure-requests";

    /**
     * Permissions Policy (formerly Feature Policy)
     * 
     * Disables dangerous browser features that e-commerce doesn't need:
     * - geolocation: Location tracking (privacy risk)
     * - microphone: Audio recording (not needed)
     * - camera: Video recording (not needed)
     * - payment: Payment Request API (we use backend payment processing)
     * - usb: USB device access (not needed)
     * - magnetometer/accelerometer/gyroscope: Motion sensors (not needed)
     * - ambient-light-sensor: Light sensor (not needed)
     * 
     * Format: feature=(allowed-origins)
     * () = disabled for all origins, including self
     */
    private static final String PERMISSIONS_POLICY = 
        "geolocation=(), " +
        "microphone=(), " +
        "camera=(), " +
        "payment=(), " +
        "usb=(), " +
        "magnetometer=(), " +
        "accelerometer=(), " +
        "gyroscope=(), " +
        "ambient-light-sensor=()";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // 1. Clickjacking Protection
        // DENY: Cannot be embedded in iframe at all (most secure)
        // Alternatives: SAMEORIGIN (same domain), ALLOW-FROM (specific domain)
        response.setHeader("X-Frame-Options", "DENY");
        
        // 2. MIME Type Sniffing Protection
        // Prevents browser from guessing content type (e.g., treating image as script)
        // Attack: Upload "image.jpg" that's actually JavaScript → XSS
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // 3. XSS Protection (Legacy)
        // Modern browsers have built-in XSS filters, but this enables them explicitly
        // mode=block: Stop page rendering if XSS detected (don't try to sanitize)
        // Note: Deprecated in modern browsers (CSP is preferred), but harmless to include
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 4. HTTP Strict Transport Security (HSTS)
        // Forces HTTPS for 1 year (31536000 seconds) for this domain + subdomains
        // Browser will auto-upgrade HTTP → HTTPS, preventing man-in-the-middle attacks
        // includeSubDomains: Apply to all subdomains (www, api, etc.)
        // Note: Only send this in production with valid HTTPS certificate!
        // TODO: Add "preload" directive after registering domain on hstspreload.org
        if (request.isSecure() || isProduction()) {
            response.setHeader("Strict-Transport-Security", 
                "max-age=31536000; includeSubDomains");
        }
        
        // 5. Content Security Policy (CSP) - Most powerful defense!
        // Prevents XSS, clickjacking, and other injection attacks at browser level
        response.setHeader("Content-Security-Policy", CONTENT_SECURITY_POLICY);
        
        // 6. Referrer Policy
        // Controls how much referrer information is sent with requests
        // strict-origin-when-cross-origin: Send full URL for same-origin, only origin for cross-origin
        // Prevents leaking sensitive URL parameters (tokens, IDs) to third parties
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // 7. Permissions Policy (Feature Policy)
        // Disables unnecessary browser features to reduce attack surface
        response.setHeader("Permissions-Policy", PERMISSIONS_POLICY);
        
        // 8. Additional Security Headers
        // Cache-Control for sensitive endpoints (prevent caching of auth responses)
        if (request.getRequestURI().contains("/auth/") || 
            request.getRequestURI().contains("/admin/")) {
            response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * Checks if running in production environment.
     * Uses Spring profile or environment variable.
     */
    private boolean isProduction() {
        String env = System.getenv("SPRING_PROFILES_ACTIVE");
        return env != null && env.contains("prod");
    }
}
