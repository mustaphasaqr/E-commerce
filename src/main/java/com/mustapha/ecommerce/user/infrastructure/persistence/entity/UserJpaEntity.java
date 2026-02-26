package com.mustapha.ecommerce.user.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Check;
import java.time.LocalDateTime;

/**
 * User JPA Entity
 * Pattern: JPA Entity (Infrastructure Layer)
 * 
 * Persistence model for User aggregate
 * Optimistic locking with @Version
 * 
 * Performance Optimization:
 * - Indexes on email, username for authentication lookups
 * - Index on status for admin queries (list blocked users, etc.)
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email", unique = true),
    @Index(name = "idx_user_username", columnList = "username", unique = true),
    @Index(name = "idx_user_status", columnList = "status")
})
@Check(name = "chk_username_min_length", constraints = "LENGTH(username) >= 3")
@Check(name = "chk_email_format", constraints = "email LIKE '%@%'")
public class UserJpaEntity {

    @Id
    private String id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 100)
    private String hashedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusType status;

    @Column(nullable = false)
    private boolean emailVerified;

    @Column(length = 500)
    private String blockReason;

    @Column(nullable = false)
    private boolean deleted;

    @Column
    private LocalDateTime deletedAt;

    @Column(length = 500)
    private String deletionReason;

    @Column(nullable = false)
    private boolean termsAccepted;

    @Column
    private LocalDateTime termsAcceptedAt;

    @Column(length = 20)
    private String termsVersion;

    @Column(nullable = false)
    private boolean marketingConsentGiven;

    @Column
    private LocalDateTime marketingConsentDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    public UserJpaEntity() {
    }

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

    public String getHashedPassword() {
        return hashedPassword;
    }

    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    public RoleType getRole() {
        return role;
    }

    public void setRole(RoleType role) {
        this.role = role;
    }

    public StatusType getStatus() {
        return status;
    }

    public void setStatus(StatusType status) {
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

    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(LocalDateTime deletedAt) {
        this.deletedAt = deletedAt;
    }

    public String getDeletionReason() {
        return deletionReason;
    }

    public void setDeletionReason(String deletionReason) {
        this.deletionReason = deletionReason;
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

    public boolean isMarketingConsentGiven() {
        return marketingConsentGiven;
    }

    public void setMarketingConsentGiven(boolean marketingConsentGiven) {
        this.marketingConsentGiven = marketingConsentGiven;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public enum RoleType {
        CUSTOMER,
        EMPLOYEE,
        OWNER
    }

    public enum StatusType {
        PENDING,
        ACTIVE,
        INACTIVE,
        BLOCKED
    }
}
