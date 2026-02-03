package com.mustapha.ecommerce.user.auth.api;

import com.mustapha.ecommerce.user.auth.application.facade.AuthFacade;
import com.mustapha.ecommerce.user.dto.*;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final JwtTokenGenerator jwtTokenGenerator;

    public AuthController(AuthFacade authFacade, JwtTokenGenerator jwtTokenGenerator) {
        this.authFacade = authFacade;
        this.jwtTokenGenerator = jwtTokenGenerator;
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
     * Extracts JWT token from Authorization header to blacklist it
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // Get username from Principal
        String sessionId = (String) authentication.getDetails();
        
        // Extract JWT token from Authorization header for blacklisting
        String token = extractJwtToken(httpRequest);
        
        authFacade.logout(userId, sessionId, token);
        return ResponseEntity.noContent().build();
    }

    /**
     * Refresh Token
     * POST /api/auth/refresh
     * 
     * Requires refresh token in request body
     * Extracts userId from the refresh token itself
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        
        TokenResponse response = authFacade.refreshToken(request, ipAddress, userAgent);
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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // Get username from Principal
        String currentSessionId = (String) authentication.getDetails();
        
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

    /**
     * Request Email Verification
     * POST /api/auth/email-verification/request
     * 
     * Public endpoint - resends verification email
     */
    @PostMapping("/email-verification/request")
    public ResponseEntity<Void> requestEmailVerification(
            @Valid @RequestBody RequestEmailVerificationRequest request) {
        
        authFacade.requestEmailVerification(request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Verify Email With Token
     * POST /api/auth/email-verification/verify
     * 
     * Public endpoint - verifies email with token
     */
    @PostMapping("/email-verification/verify")
    public ResponseEntity<Void> verifyEmailWithToken(
            @Valid @RequestBody VerifyEmailWithTokenRequest request) {
        
        authFacade.verifyEmailWithToken(request);
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
    
    /**
     * Extract JWT token from Authorization header
     * Removes "Bearer " prefix
     */
    private String extractJwtToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
