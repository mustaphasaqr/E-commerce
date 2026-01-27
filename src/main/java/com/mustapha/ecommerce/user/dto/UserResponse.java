package com.mustapha.ecommerce.user.dto;

import com.mustapha.ecommerce.user.domain.model.User;

import java.time.LocalDateTime;

/**
 * User Response DTO - Complete API Contract
 * Responsibility: Full user information for API consumers
 * 
 * Contains:
 * - User identification (id, username, email)
 * - User role and permissions
 * - Account status (status, emailVerified, deleted)
 * - GDPR compliance (termsAccepted, marketingConsent)
 * - Temporal data (createdAt, updatedAt)
 */
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String role;
    private String status;
    private boolean emailVerified;
    private String blockReason;
    private boolean deleted;
    private boolean termsAccepted;
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;
    private boolean marketingConsent;
    private LocalDateTime marketingConsentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public UserResponse() {
    }

    public UserResponse(String id, String username, String email, String role, String status,
                       boolean emailVerified, String blockReason, boolean deleted,
                       boolean termsAccepted, LocalDateTime termsAcceptedAt, String termsVersion,
                       boolean marketingConsent, LocalDateTime marketingConsentDate,
                       LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.status = status;
        this.emailVerified = emailVerified;
        this.blockReason = blockReason;
        this.deleted = deleted;
        this.termsAccepted = termsAccepted;
        this.termsAcceptedAt = termsAcceptedAt;
        this.termsVersion = termsVersion;
        this.marketingConsent = marketingConsent;
        this.marketingConsentDate = marketingConsentDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static UserResponse fromDomain(User user) {
        return new UserResponse(
            user.getId().getValue().toString(),
            user.getUsername().getValue(),
            user.getEmail().getValue(),
            user.getRole().name(),
            user.getStatus().name(),
            user.isEmailVerified(),
            user.getBlockReason(),
            user.isDeleted(),
            user.isTermsAccepted(),
            user.getTermsAcceptedAt(),
            user.getTermsVersion(),
            user.isMarketingConsentGiven(),
            user.getMarketingConsentDate(),
            user.getCreatedAt(),
            user.getLastModifiedAt()
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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getBlockReason() {
        return blockReason;
    }

    public void setBlockReason(String blockReason) {
        this.blockReason = blockReason;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isTermsAccepted() {
        return termsAccepted;
    }

    public void setTermsAccepted(boolean termsAccepted) {
        this.termsAccepted = termsAccepted;
    }

    public LocalDateTime getTermsAcceptedAt() {
        return termsAcceptedAt;
    }

    public void setTermsAcceptedAt(LocalDateTime termsAcceptedAt) {
        this.termsAcceptedAt = termsAcceptedAt;
    }

    public String getTermsVersion() {
        return termsVersion;
    }

    public void setTermsVersion(String termsVersion) {
        this.termsVersion = termsVersion;
    }

    public boolean isMarketingConsent() {
        return marketingConsent;
    }

    public void setMarketingConsent(boolean marketingConsent) {
        this.marketingConsent = marketingConsent;
    }

    public LocalDateTime getMarketingConsentDate() {
        return marketingConsentDate;
    }

    public void setMarketingConsentDate(LocalDateTime marketingConsentDate) {
        this.marketingConsentDate = marketingConsentDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
