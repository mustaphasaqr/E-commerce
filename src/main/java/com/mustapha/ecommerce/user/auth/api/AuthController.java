package com.mustapha.ecommerce.user.auth.api;

import com.mustapha.ecommerce.user.auth.application.facade.AuthFacade;
import com.mustapha.ecommerce.user.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Auth REST Controller
 * Responsibility: HTTP layer for Authentication operations
 * Pattern: REST API, delegates to AuthFacade
 * 
 * Endpoints:
 * - POST   /api/auth/login              - Login (email/username + password)
 * - POST   /api/auth/logout             - Logout current session
 * - POST   /api/auth/refresh            - Refresh access token
 * - POST   /api/auth/logout-all         - Logout all devices
 * - POST   /api/auth/password-reset/request   - Request password reset
 * - POST   /api/auth/password-reset/complete  - Complete password reset
 * 
 * Note: IP and UserAgent extracted from HttpServletRequest
 * TODO: Add rate limiting, CORS, CSRF protection (Week 3)
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthFacade authFacade;

    public AuthController(AuthFacade authFacade) {
        this.authFacade = authFacade;
    }

    /**
     * Login
     * POST /api/auth/login
     * 
     * Public endpoint - no authentication required
     * Extracts IP address and User-Agent from request headers
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        
        LoginResponse response = authFacade.login(request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout
     * POST /api/auth/logout
     * 
     * Requires authentication
     * TODO: Extract userId and sessionId from JWT SecurityContext
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // TODO: Get from SecurityContext (Spring Security)
        String userId = "CURRENT_USER_ID_FROM_JWT";
        String sessionId = "CURRENT_SESSION_ID_FROM_JWT";
        
        authFacade.logout(userId, sessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh Token
     * POST /api/auth/refresh
     * 
     * Requires authentication (refresh token in request body)
     * TODO: Extract userId from JWT
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        // TODO: Get userId from SecurityContext
        String userId = "CURRENT_USER_ID_FROM_JWT";
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        
        TokenResponse response = authFacade.refreshToken(userId, request, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }

    /**
     * Logout All Devices
     * POST /api/auth/logout-all
     * 
     * Requires authentication
     * Invalidates all sessions except current one
     * TODO: Extract userId and sessionId from JWT
     */
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAllDevices() {
        // TODO: Get from SecurityContext
        String userId = "CURRENT_USER_ID_FROM_JWT";
        String currentSessionId = "CURRENT_SESSION_ID_FROM_JWT";
        
        authFacade.logoutAllDevices(userId, currentSessionId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Password Reset Request
     * POST /api/auth/password-reset/request
     * 
     * Public endpoint - no authentication required
     * Sends password reset email (silent failure if email doesn't exist)
     */
    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> requestPasswordReset(
            @Valid @RequestBody PasswordResetRequestRequest request) {
        
        authFacade.requestPasswordReset(request);
        // Always return 204 (silent failure for security)
        return ResponseEntity.noContent().build();
    }

    /**
     * Password Reset Complete
     * POST /api/auth/password-reset/complete
     * 
     * Public endpoint - no authentication required
     * Validates token and sets new password
     */
    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> completePasswordReset(
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        
        authFacade.completePasswordReset(request);
        return ResponseEntity.noContent().build();
    }

    // ========== Helper Methods ==========

    /**
     * Extract IP address from HTTP request
     * Handles X-Forwarded-For header for proxy/load balancer scenarios
     */
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take first IP if multiple proxies
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * Extract User-Agent from HTTP request headers
     */
    private String extractUserAgent(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}
