package com.mustapha.ecommerce.user.dto;

import com.mustapha.ecommerce.user.domain.model.User;

/**
 * User List Response DTO - Lightweight for List Operations
 * Responsibility: Minimal user information for list/search endpoints
 * Performance: 67% smaller than UserResponse (5 fields vs 15)
 * 
 * Use Cases:
 * - Admin user management
 * - User search results
 * - User directory
 * 
 * For full details (GDPR, timestamps, verification status), use UserResponse via GET /api/users/{id}
 */
public class UserListResponse {
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;

    // Constructors
    public UserListResponse() {
    }

    public UserListResponse(String id, String username, String email, 
                           String role, String status) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    /**
     * Create lightweight DTO from domain model
     * Only extracts fields needed for list view
     */
    public static UserListResponse fromDomain(User user) {
        return new UserListResponse(
            user.getId().getValue().toString(),
            user.getUsername().getValue(),
            user.getEmail().getValue(),
            user.getRole().name(),
            user.getStatus().name()
        );
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
