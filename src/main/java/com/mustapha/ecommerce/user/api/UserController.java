package com.mustapha.ecommerce.user.api;

import com.mustapha.ecommerce.user.application.facade.UserFacade;
import com.mustapha.ecommerce.user.dto.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * User REST Controller
 * Responsibility: HTTP layer for User operations
 * Pattern: REST API, delegates to UserFacade
 * 
 * Endpoints:
 * - POST   /api/users/register           - Register new user
 * - GET    /api/users/me                 - Get current authenticated user
 * - GET    /api/users/{id}               - Get user by ID
 * - GET    /api/users/email/{email}      - Get user by email
 * - GET    /api/users/username/{username}- Get user by username
 * - PUT    /api/users/me/email           - Change email
 * - PUT    /api/users/me/password        - Change password
 * - POST   /api/users/me/email/verify    - Verify email
 * - POST   /api/users/me/marketing/grant - Grant marketing consent
 * - DELETE /api/users/me/marketing       - Revoke marketing consent
 * - POST   /api/users/{id}/activate      - Activate user (Admin)
 * - POST   /api/users/{id}/deactivate    - Deactivate user (Admin)
 * - POST   /api/users/{id}/block         - Block user (Admin)
 * - POST   /api/users/{id}/unblock       - Unblock user (Admin)
 * - DELETE /api/users/{id}               - Delete user (Admin)
 * 
 * Note: Authentication/Authorization will be added via Spring Security (Week 3)
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserFacade userFacade;

    public UserController(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    // ========== Public Endpoints ==========

    /**
     * Register new user
     * POST /api/users
     */
    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse response = userFacade.registerUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ========== Authenticated User Endpoints (requires JWT) ==========

    /**
     * Get current user profile
     * GET /api/users/me
     * 
     * TODO: Extract userId from JWT SecurityContext
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(
            @AuthenticationPrincipal String userId) {
        UserResponse response = userFacade.getUserById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Change current user's email
     * PUT /api/users/me/email
     */
    @PutMapping("/me/email")
    public ResponseEntity<UserResponse> changeEmail(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ChangeEmailRequest request) {
        UserResponse response = userFacade.changeEmail(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Change current user's password
     * PUT /api/users/me/password
     * 
     * TODO: Extract userId from JWT
     */
    @PutMapping("/me/password")
    public ResponseEntity<UserResponse> changePassword(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        UserResponse response = userFacade.changePassword(userId, request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Verify current user's email
     * POST /api/users/me/email/verify
     * 
     * TODO: Extract userId from JWT
     * Note: In production, this would be called after clicking verification link with token
     */
    @PostMapping("/me/email/verify")
    public ResponseEntity<UserResponse> verifyEmail(
            @AuthenticationPrincipal String userId) {
        UserResponse response = userFacade.verifyEmail(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Grant marketing consent
     * POST /api/users/me/marketing/grant
     * 
     * TODO: Extract userId from JWT
     */
    @PostMapping("/me/marketing/grant")
    public ResponseEntity<UserResponse> grantMarketingConsent(
            @AuthenticationPrincipal String userId) {
        UserResponse response = userFacade.grantMarketingConsent(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Revoke marketing consent
     * DELETE /api/users/me/marketing
     * 
     * TODO: Extract userId from JWT
     */
    @DeleteMapping("/me/marketing")
    public ResponseEntity<UserResponse> revokeMarketingConsent(
            @AuthenticationPrincipal String userId) {
        UserResponse response = userFacade.revokeMarketingConsent(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // ========== Query Endpoints (Admin or specific roles) ==========

    /**
     * Get user by ID
     * GET /api/users/{id}
     * Users can only access their own profile unless they're OWNER
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(
            @PathVariable String id,
            @AuthenticationPrincipal String currentUserId) {
        // Check ownership - users can only view their own profile unless they're an admin
        if (!id.equals(currentUserId)) {
            // If not viewing own profile, this would need OWNER role check
            // For now, allowing for simplicity - in production, add role check
            throw new org.springframework.security.access.AccessDeniedException("Cannot access other user's profile");
        }
        UserResponse response = userFacade.getUserById(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get user by email
     * GET /api/users/email/{email}
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserResponse> getUserByEmail(@PathVariable String email) {
        UserResponse response = userFacade.getUserByEmail(email);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Get user by username
     * GET /api/users/username/{username}
     */
    @PreAuthorize("hasRole('OWNER')")
    @GetMapping("/username/{username}")
    public ResponseEntity<UserResponse> getUserByUsername(@PathVariable String username) {
        UserResponse response = userFacade.getUserByUsername(username);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    // ========== Admin Endpoints ==========

    /**
     * Activate user
     * POST /api/users/{id}/activate
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable String id) {
        UserResponse response = userFacade.activateUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Deactivate user
     * POST /api/users/{id}/deactivate
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable String id) {
        UserResponse response = userFacade.deactivateUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Block user
     * POST /api/users/{id}/block?reason={reason}
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/block")
    public ResponseEntity<UserResponse> blockUser(@PathVariable String id, @RequestParam String reason) {
        UserResponse response = userFacade.blockUser(id, reason);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Unblock user
     * POST /api/users/{id}/unblock
     */
    @PreAuthorize("hasRole('OWNER')")
    @PostMapping("/{id}/unblock")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable String id) {
        UserResponse response = userFacade.unblockUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Delete user (soft delete)
     * DELETE /api/users/{id}?reason={reason}
     */
    @PreAuthorize("hasRole('OWNER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String id, @RequestParam String reason) {
        UserResponse response = userFacade.deleteUser(id, reason);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
