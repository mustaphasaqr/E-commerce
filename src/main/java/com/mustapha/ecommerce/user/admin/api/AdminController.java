package com.mustapha.ecommerce.user.admin.api;

import com.mustapha.ecommerce.user.admin.application.facade.AdminFacade;
import com.mustapha.ecommerce.user.admin.dto.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Admin Controller
 * Responsibility: Admin operations on users (OWNER role only)
 * 
 * Security: All endpoints require OWNER role
 * Pattern: REST API with RBAC
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('OWNER')")
public class AdminController {

    private final AdminFacade adminFacade;

    public AdminController(AdminFacade adminFacade) {
        this.adminFacade = adminFacade;
    }

    @PostMapping("/{id}/block")
    public ResponseEntity<UserResponse> blockUser(@PathVariable String id, @RequestBody BlockUserRequest request) {
        User user = adminFacade.blockUser(id, request.reason());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PostMapping("/{id}/unblock")
    public ResponseEntity<UserResponse> unblockUser(@PathVariable String id, @RequestBody UnblockUserRequest request) {
        User user = adminFacade.unblockUser(id, request.reason());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PostMapping("/{id}/activate")
    public ResponseEntity<UserResponse> activateUser(@PathVariable String id, @RequestBody ActivateUserRequest request) {
        User user = adminFacade.activateUser(id, request.activationNote());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @PostMapping("/{id}/deactivate")
    public ResponseEntity<UserResponse> deactivateUser(@PathVariable String id, @RequestBody DeactivateUserRequest request) {
        User user = adminFacade.deactivateUser(id, request.reason());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse> deleteUser(@PathVariable String id, @RequestBody DeleteUserRequest request) {
        User user = adminFacade.deleteUser(id, request.reason());
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    @GetMapping
    public ResponseEntity<PaginatedUsersResponse> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Handle edge cases gracefully
        int safePage = Math.max(0, page); // Negative pages become 0
        int safeSize = size <= 0 ? 20 : size; // Zero or negative size defaults to 20
        
        Page<User> users = adminFacade.getAllUsers(PageRequest.of(safePage, safeSize));
        return ResponseEntity.ok(toPaginatedResponse(users));
    }

    @GetMapping("/search")
    public ResponseEntity<PaginatedUsersResponse> searchUsersGet(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        // Convert String parameters to enums
        User.UserStatus userStatus = status != null ? User.UserStatus.valueOf(status.toUpperCase()) : null;
        Role userRole = role != null ? Role.valueOf(role.toUpperCase()) : null;
        
        Page<User> users = adminFacade.searchUsers(email, username, userStatus, userRole, PageRequest.of(page, size));
        return ResponseEntity.ok(toPaginatedResponse(users));
    }

    @PostMapping("/search")
    public ResponseEntity<PaginatedUsersResponse> searchUsers(@RequestBody SearchUsersRequest request) {
        Page<User> users = adminFacade.searchUsers(
            request.email(),
            request.username(),
            request.status(),
            request.role(),
            PageRequest.of(request.page(), request.size())
        );
        return ResponseEntity.ok(toPaginatedResponse(users));
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<UserResponse> changeUserRole(
            @PathVariable String id, 
            @Valid @RequestBody ChangeUserRoleRequest request) {
        User user = adminFacade.changeUserRole(id, request.newRole(), "SYSTEM");
        return ResponseEntity.ok(UserResponse.fromDomain(user));
    }

    private PaginatedUsersResponse toPaginatedResponse(Page<User> page) {
        return new PaginatedUsersResponse(
            page.getContent().stream().map(UserResponse::fromDomain).toList(),
            page.getNumber(),
            page.getSize(),
            page.getTotalPages(),
            page.getTotalElements()
        );
    }
}
