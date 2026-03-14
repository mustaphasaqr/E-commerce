package com.mustapha.ecommerce.user.auth.api;

import com.mustapha.ecommerce.shared.security.ratelimit.RateLimit;
import com.mustapha.ecommerce.shared.security.ratelimit.RateLimitScope;
import com.mustapha.ecommerce.user.auth.application.facade.AuthFacade;
import com.mustapha.ecommerce.user.dto.*;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
 */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and session management endpoints. Handles login, logout, token refresh, and password reset operations.")
public class AuthController {

    private final AuthFacade authFacade;
    private final JwtTokenGenerator jwtTokenGenerator;

    public AuthController(AuthFacade authFacade, JwtTokenGenerator jwtTokenGenerator) {
        this.authFacade = authFacade;
        this.jwtTokenGenerator = jwtTokenGenerator;
    }

    @Operation(
        summary = "User Login",
        description = """
            Authenticate user with email/username and password. Returns JWT access token and refresh token.
            
            **Features:**
            - Validates user credentials
            - Creates session tracking
            - Returns JWT tokens (access + refresh)
            - Tracks IP address and User-Agent
            - Rate limited per IP (see X-RateLimit headers)
            
            **Security:** Public endpoint, no authentication required
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login successful - Returns access token, refresh token, and user details",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(
                    name = "Successful Login",
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "expiresIn": 3600000,
                          "sessionId": "550e8400-e29b-41d4-a716-446655440000",
                          "user": {
                            "id": "USR-123456",
                            "username": "johndoe",
                            "email": "john@example.com",
                            "role": "CUSTOMER",
                            "status": "ACTIVE"
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid request body or validation errors",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Email is required",
                          "path": "/api/auth/login"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Invalid credentials or account locked/inactive",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Invalid email or password",
                          "path": "/api/auth/login"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - Rate limit exceeded",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 429,
                          "error": "Too Many Requests",
                          "message": "Rate limit exceeded. Try again in 60 seconds",
                          "path": "/api/auth/login"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error - Unexpected error occurred",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/login")
    @RateLimit(maxRequests = 5, windowSeconds = 300, scope = RateLimitScope.PARAMETER, message = "Too many login attempts for this account. Please try again in 5 minutes.", parameterName = "email")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials (email/username + password)", required = true)
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        // Use email as the parameter for per-user/email rate limiting
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        LoginResponse response = authFacade.login(request, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "User Logout",
        description = """
            Invalidate current session and blacklist JWT token.
            
            **Features:**
            - Blacklists access token
            - Invalidates current session
            - Clears session data from Redis
            
            **Security:** Requires JWT authentication
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Logout successful - Session invalidated"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 401,
                          "error": "Unauthorized",
                          "message": "Full authentication is required",
                          "path": "/api/auth/logout"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest httpRequest) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // Get username from Principal
        String sessionId = (String) authentication.getDetails();
        
        // Extract JWT token from Authorization header for blacklisting
        String token = extractJwtToken(httpRequest);
        
        authFacade.logout(userId, sessionId, token);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Refresh Access Token",
        description = """
            Generate new access token using refresh token. Extends session without re-authentication.
            
            **Features:**
            - Validates refresh token
            - Issues new access token
            - Maintains session continuity
            - Tracks IP and User-Agent changes
            
            **Security:** Public endpoint but requires valid refresh token
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully - Returns new access token",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TokenResponse.class),
                examples = @ExampleObject(
                    value = """
                        {
                          "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                          "expiresIn": 3600000
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid or missing refresh token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Refresh token expired or revoked",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(description = "Refresh token to generate new access token", required = true)
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = extractUserAgent(httpRequest);
        
        TokenResponse response = authFacade.refreshToken(request, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Logout All Devices",
        description = """
            Invalidate all user sessions except the current one. Useful when user suspects unauthorized access.
            
            **Features:**
            - Invalidates all sessions for the user
            - Keeps current session active
            - Blacklists all other access tokens
            - Clears session data from Redis
            
            **Security:** Requires JWT authentication
            """,
        security = @SecurityRequirement(name = "Bearer Authentication"),
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "All other sessions invalidated successfully"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Unauthorized - Missing or invalid JWT token",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAllDevices() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName(); // Get username from Principal
        String currentSessionId = (String) authentication.getDetails();
        
        authFacade.logoutAllDevices(userId, currentSessionId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Request Password Reset",
        description = """
            Initiate password reset process by sending email with reset link.
            
            **Features:**
            - Sends password reset email with token
            - Always returns 204 (silent failure for security)
            - Token expires after 1 hour
            - Rate limited: 3 requests per 5 minutes per IP
            
            **Security:** Public endpoint, heavily rate limited
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Request processed - Password reset email sent if account exists"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid email format",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - Rate limit exceeded (3 per 5 minutes)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/password-reset/request")
    @RateLimit(maxRequests = 3, windowSeconds = 300, scope = RateLimitScope.IP, 
               message = "Too many password reset requests. Please try again in 5 minutes.")
    public ResponseEntity<Void> requestPasswordReset(
            @Parameter(description = "Email address for password reset", required = true)
            @Valid @RequestBody PasswordResetRequestRequest request) {
        
        authFacade.requestPasswordReset(request);
        // Always return 204 (silent failure for security)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Complete Password Reset",
        description = """
            Complete password reset with token and new password.
            
            **Features:**
            - Validates reset token
            - Sets new password (min 8 chars, must include uppercase, lowercase, digit, special char)
            - Invalidates reset token after use
            - Invalidates all existing sessions
            - Rate limited: 5 attempts per 5 minutes per IP
            
            **Security:** Public endpoint but requires valid token, rate limited
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Password reset successful - All sessions invalidated"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid token, expired token, or weak password",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Password must be at least 8 characters",
                          "path": "/api/auth/password-reset/complete"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "429",
            description = "Too Many Requests - Rate limit exceeded (5 per 5 minutes)",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/password-reset/complete")
    @RateLimit(maxRequests = 5, windowSeconds = 300, scope = RateLimitScope.IP,
               message = "Too many password reset attempts. Please try again in 5 minutes.")
    public ResponseEntity<Void> completePasswordReset(
            @Parameter(description = "Reset token and new password", required = true)
            @Valid @RequestBody PasswordResetCompleteRequest request) {
        
        authFacade.completePasswordReset(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Request Email Verification",
        description = """
            Resend email verification link to user's email address.
            
            **Features:**
            - Sends verification email with token
            - Token expires after 24 hours
            - Always returns 204 (silent failure for security)
            
            **Security:** Public endpoint
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Request processed - Verification email sent if account exists"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid email format",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/email-verification/request")
    public ResponseEntity<Void> requestEmailVerification(
            @Parameter(description = "Email address to verify", required = true)
            @Valid @RequestBody RequestEmailVerificationRequest request) {
        
        authFacade.requestEmailVerification(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
        summary = "Verify Email With Token",
        description = """
            Complete email verification using token from verification email.
            
            **Features:**
            - Validates verification token
            - Marks email as verified
            - Invalidates token after use
            - Activates account if pending
            
            **Security:** Public endpoint but requires valid token
            """,
        tags = {"Authentication"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Email verified successfully - Account activated"
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad Request - Invalid or expired verification token",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    value = """
                        {
                          "timestamp": "2026-03-03T10:15:30Z",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Verification token has expired",
                          "path": "/api/auth/email-verification/verify"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/email-verification/verify")
    public ResponseEntity<Void> verifyEmailWithToken(
            @Parameter(description = "Verification token from email", required = true)
            @Valid @RequestBody VerifyEmailWithTokenRequest request) {
        
        authFacade.verifyEmailWithToken(request);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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
