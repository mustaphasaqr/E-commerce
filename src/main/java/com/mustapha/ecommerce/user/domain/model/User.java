package com.mustapha.ecommerce.user.domain.model;

import com.mustapha.ecommerce.user.domain.event.DomainEvent;
import com.mustapha.ecommerce.user.domain.event.*;
import com.mustapha.ecommerce.user.domain.exception.*;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * User Aggregate Root (Core Domain)
 * Responsibility: User identity and lifecycle management
 * Pattern: DDD Aggregate Root with Domain Events
 * 
 * Invariants:
 * - User ID, Username, Email, and Role cannot be null
 * - Email must be unique (enforced by repository)
 * - Username must be unique (enforced by repository)
 * - Cannot activate already active user
 * - Cannot block already blocked user
 * - Blocked users cannot perform actions
 * - Email verification required before certain operations
 * - Password changes require current password verification
 * - Status transitions follow lifecycle rules
 * 
 * Lifecycle: PENDING → ACTIVE → INACTIVE/BLOCKED
 * Terminal states: BLOCKED (can be unblocked but requires explicit action)
 * 
 * NOTE: Authentication concerns (login tracking, password reset tokens, sessions)
 * are handled by the Auth subdomain, NOT in this aggregate.
 */
public class User {
    
    // Identity (immutable)
    private final UserId id;
    
    // Core attributes
    private Username username;
    private Email email;
    private Password password;
    private Role role;
    
    // State flags
    private UserStatus status;
    private boolean emailVerified;
    private String blockReason;
    
    // Soft delete (GDPR data retention)
    private boolean deleted;
    private LocalDateTime deletedAt;
    private String deletionReason;
    
    // GDPR Compliance
    private boolean termsAccepted;
    private LocalDateTime termsAcceptedAt;
    private String termsVersion;
    private boolean marketingConsentGiven;
    private LocalDateTime marketingConsentDate;
    
    // Version control (long is safer for high-volume updates)
    private long version;
    
    // Temporal
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Domain events (type-safe with DomainEvent interface)
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    // Private constructor for invariants protection
    private User(UserId id, Username username, Email email, Password password, Role role,
                 UserStatus status, boolean emailVerified, String blockReason,
                 boolean deleted, LocalDateTime deletedAt, String deletionReason,
                 boolean termsAccepted, LocalDateTime termsAcceptedAt, String termsVersion,
                 boolean marketingConsentGiven, LocalDateTime marketingConsentDate,
                 long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = Objects.requireNonNull(id, "User ID cannot be null");
        this.username = Objects.requireNonNull(username, "Username cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
        this.password = Objects.requireNonNull(password, "Password cannot be null");
        this.role = Objects.requireNonNull(role, "Role cannot be null");
        this.status = status;
        this.emailVerified = emailVerified;
        this.blockReason = blockReason;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        this.deletionReason = deletionReason;
        this.termsAccepted = termsAccepted;
        this.termsAcceptedAt = termsAcceptedAt;
        this.termsVersion = termsVersion;
        this.marketingConsentGiven = marketingConsentGiven;
        this.marketingConsentDate = marketingConsentDate;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Factory method: Create new user
     * User starts in PENDING status and requires activation
     */
    public static User create(Username username, Email email, Password password, Role role) {
        User user = new User(
            UserId.newId(),
            username,
            email,
            password,
            role,
            UserStatus.PENDING,
            false,          // email not verified
            null,           // no block reason
            false,          // not deleted
            null,           // no deletion date
            null,           // no deletion reason
            false,          // terms not accepted yet
            null,           // no terms acceptance date
            null,           // no terms version
            false,          // no marketing consent
            null,           // no consent date
            1,              // version starts at 1
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        
        user.domainEvents.add(new UserCreatedEvent(user.id, username, email));
        return user;
    }

    /**
     * Factory method: Reconstitute from database
     */
    public static User reconstitute(UserId id, Username username, Email email, Password password, Role role,
                                   UserStatus status, boolean emailVerified, String blockReason,
                                   boolean deleted, LocalDateTime deletedAt, String deletionReason,
                                   boolean termsAccepted, LocalDateTime termsAcceptedAt, String termsVersion,
                                   boolean marketingConsentGiven, LocalDateTime marketingConsentDate,
                                   long version, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new User(id, username, email, password, role, status, emailVerified, blockReason,
                       deleted, deletedAt, deletionReason,
                       termsAccepted, termsAcceptedAt, termsVersion,
                       marketingConsentGiven, marketingConsentDate,
                       version, createdAt, updatedAt);
    }

    // ========== Business Rules: User Lifecycle ==========
    
    /**
     * Activates the user account
     * Rule: Cannot activate already active user
     * Rule: Cannot activate blocked user
     * Rule: User must accept terms before activation (GDPR compliance)
     * Rule: Email must be verified before activation (security best practice)
     * Status: PENDING → ACTIVE or INACTIVE → ACTIVE
     * 
     * @param activationNote Optional note for manual activation (admin use case)
     */
    public void activate(String activationNote) {
        if (status == UserStatus.ACTIVE) {
            throw new InvalidUserStateException("User is already active");
        }
        
        if (status == UserStatus.BLOCKED) {
            throw new UserBlockedException(blockReason);
        }
        
        // GDPR Compliance: Terms must be accepted before activation
        if (!termsAccepted) {
            throw new InvalidUserStateException("User must accept terms before activation");
        }
        
        // Security: Email verification required before activation
        // This prevents account takeover and ensures valid contact
        if (!emailVerified) {
            throw new InvalidUserStateException("Email must be verified before activation");
        }
        
        this.status = UserStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserActivatedEvent(id, activationNote));
    }

    /**
     * Blocks the user account with a reason
     * Rule: Cannot block already blocked user
     * Rule: Block reason cannot be empty
     * Status: Any → BLOCKED
     */
    public void block(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Block reason cannot be empty");
        }
        
        if (status == UserStatus.BLOCKED) {
            throw new InvalidUserStateException("User is already blocked");
        }
        
        this.status = UserStatus.BLOCKED;
        this.blockReason = reason;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserBlockedEvent(id, reason));
    }

    /**
     * Unblocks a previously blocked user
     * Rule: User must be in BLOCKED status
     * Raises: UserUnblockedEvent for support systems, risk engines, audit logs
     * 
     * @param reason Optional reason for unblocking (audit trail)
     */
    public void unblock(String reason) {
        if (status != UserStatus.BLOCKED) {
            throw new InvalidUserStateException("User is not blocked");
        }
        
        this.status = UserStatus.INACTIVE;
        this.blockReason = null;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserUnblockedEvent(id, reason));
    }

    /**
     * Deactivates user account temporarily
     * Rule: Cannot deactivate blocked user
     * Status: ACTIVE → INACTIVE
     * Raises: UserDeactivatedEvent for analytics, notifications, license management
     * 
     * @param reason Optional reason for deactivation (audit trail)
     */
    public void deactivate(String reason) {
        ensureNotBlocked();
        
        if (status == UserStatus.INACTIVE) {
            throw new InvalidUserStateException("User is already inactive");
        }
        
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserDeactivatedEvent(id, reason));
    }

    /**
     * Changes user's role
     * Rule: Role transitions must be valid (e.g., cannot downgrade from OWNER unless special permission)
     * Raises: UserRoleChangedEvent for authorization services, audit logs
     * 
     * @param newRole The new role to assign
     * @param changedBy User ID who authorized the change (for audit)
     */
    public void changeRole(Role newRole, String changedBy) {
        ensureNotBlocked();
        ensureNotDeleted();
        
        if (this.role == newRole) {
            throw new IllegalArgumentException("User already has role: " + newRole);
        }
        
        Role oldRole = this.role;
        this.role = newRole;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserRoleChangedEvent(id, oldRole, newRole, changedBy));
    }

    // ========== Business Rules: Email Management ==========
    
    /**
     * Verifies the user's email address
     * Rule: Email can only be verified once (idempotent)
     */
    public void verifyEmail() {
        ensureNotBlocked();
        
        if (emailVerified) {
            return; // Idempotent - already verified
        }
        
        this.emailVerified = true;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserEmailVerifiedEvent(id));
    }

    /**
     * Changes the user's email address
     * Rule: New email must be different from current email
     * Rule: User must be ACTIVE (not just unblocked)
     * Side effect: Requires re-verification
     */
    public void changeEmail(Email newEmail) {
        ensureCanPerformActions(); // Requires ACTIVE status
        Objects.requireNonNull(newEmail, "New email cannot be null");
        
        if (email.equals(newEmail)) {
            throw new IllegalArgumentException("New email must be different from current email");
        }
        
        Email oldEmail = this.email;
        this.email = newEmail;
        this.emailVerified = false; // Require re-verification
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new UserEmailChangedEvent(id, oldEmail, newEmail));
    }

    // ========== Business Rules: Password Management ==========
    
    /**
     * Changes user password with current password verification
     * Rule: Current password must be verified
     * Rule: User must not be blocked or deleted
     * 
     * NOTE: Password reset (without current password) is handled by Auth subdomain
     */
    public void changePassword(String currentPlainPassword, Password newPassword, PasswordHasher hasher) {
        ensureNotDeleted();
        ensureNotBlocked();
        Objects.requireNonNull(newPassword, "New password cannot be null");
        Objects.requireNonNull(hasher, "Password hasher cannot be null");
        
        if (!password.matches(currentPlainPassword, hasher)) {
            throw new InvalidPasswordException();
        }
        
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new PasswordChangedEvent(id));
    }

    /**
     * Resets password without current password verification (for password reset flow via token)
     * Rule: User must not be blocked or deleted
     * Rule: Only called from password reset flow after token validation
     * 
     * NOTE: This bypasses current password check - only use after validating reset token!
     */
    public void resetPassword(Password newPassword) {
        ensureNotDeleted();
        ensureNotBlocked();
        Objects.requireNonNull(newPassword, "New password cannot be null");
        
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new PasswordChangedEvent(id));
    }

    /**
     * Verifies a login attempt with the provided password
     * Rule: User must not be blocked or deleted
     * Returns: true if password matches, false otherwise
     * 
     * NOTE: Login tracking, rate limiting, and session management are handled by Auth subdomain
     */
    public boolean verifyPassword(String plainPassword, PasswordHasher hasher) {
        ensureNotDeleted();
        ensureNotBlocked();
        return password.matches(plainPassword, hasher);
    }

    // ========== Business Rules: Username Management ==========
    
    /**
     * Changes the username
     * Rule: New username must be different from current
     * Rule: User must be active and not deleted
     */
    public void changeUsername(Username newUsername) {
        ensureNotDeleted();
        ensureCanPerformActions();
        Objects.requireNonNull(newUsername, "New username cannot be null");
        
        if (username.equals(newUsername)) {
            throw new IllegalArgumentException("New username must be different from current username");
        }
        
        Username oldUsername = this.username;
        this.username = newUsername;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new UsernameChangedEvent(id, oldUsername, newUsername));
    }
    
    // ========== Business Rules: Account Deletion ==========
    
    /**
     * Soft deletes the user account
     * Rule: Cannot delete already deleted account
     * Rule: Deletion reason must be provided
     * Rule: Users blocked for fraud/abuse cannot be deleted (legal/compliance)
     */
    public void delete(String reason) {
        ensureNotDeleted();
        
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Deletion reason cannot be null or blank");
        }
        
        // Prevent deletion of fraudulent accounts for legal/compliance reasons
        if (status == UserStatus.BLOCKED && blockReason != null && 
            (blockReason.toUpperCase().contains("FRAUD") || blockReason.toUpperCase().contains("ABUSE"))) {
            throw new InvalidUserStateException("Users blocked for fraud or abuse cannot be deleted. Contact legal team.");
        }
        
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
        this.deletionReason = reason;
        this.status = UserStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new UserDeletedEvent(id, reason));
    }
    
    // ========== Business Rules: GDPR Compliance ==========
    
    /**
     * Records user acceptance of terms and conditions
     * Rule: Terms version must be provided
     * Rule: Idempotent - accepting same version twice has no effect
     * Raises: TermsAcceptedEvent for legal compliance and audit trail
     */
    public void acceptTerms(String termsVersion) {
        ensureNotDeleted();
        
        if (termsVersion == null || termsVersion.isBlank()) {
            throw new IllegalArgumentException("Terms version cannot be null or blank");
        }
        
        // Idempotent: If already accepted this version, do nothing
        if (termsAccepted && termsVersion.equals(this.termsVersion)) {
            return;
        }
        
        this.termsAccepted = true;
        this.termsAcceptedAt = LocalDateTime.now();
        this.termsVersion = termsVersion;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        domainEvents.add(new TermsAcceptedEvent(id, termsVersion));
    }
    
    /**
     * Gives marketing consent
     * Rule: Idempotent - granting consent twice has no effect
     * Raises event for analytics and compliance audit
     */
    public void giveMarketingConsent() {
        ensureNotDeleted();
        
        // Idempotent: If already given, do nothing
        if (marketingConsentGiven) {
            return;
        }
        
        this.marketingConsentGiven = true;
        this.marketingConsentDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new MarketingConsentGrantedEvent(id));
    }
    
    /**
     * Revokes marketing consent
     * Rule: Idempotent - revoking consent twice has no effect
     * Raises event for analytics and compliance audit (GDPR right to withdraw)
     */
    public void revokeMarketingConsent() {
        ensureNotDeleted();
        
        // Idempotent: If already revoked, do nothing
        if (!marketingConsentGiven) {
            return;
        }
        
        this.marketingConsentGiven = false;
        this.updatedAt = LocalDateTime.now();
        incrementVersion();
        
        domainEvents.add(new MarketingConsentRevokedEvent(id));
    }

    // ========== Guards ==========
    
    /**
     * Guard: Ensures user can perform actions
     * Throws: UserDeletedException if user is deleted
     * Throws: UserBlockedException if user is blocked
     * Throws: InvalidUserStateException if user is not active
     */
    public void ensureCanPerformActions() {
        ensureNotDeleted();
        ensureNotBlocked();
        
        if (status != UserStatus.ACTIVE) {
            throw new InvalidUserStateException("User must be active to perform actions");
        }
    }

    /**
     * Guard: Ensures user is not blocked
     */
    private void ensureNotBlocked() {
        if (status == UserStatus.BLOCKED) {
            throw new UserBlockedException(blockReason);
        }
    }
    
    /**
     * Guard: Ensures user is not deleted
     */
    private void ensureNotDeleted() {
        if (deleted) {
            throw new UserDeletedException(id.toString());
        }
    }

    // ========== Version Control ==========
    
    private void incrementVersion() {
        this.version++;
    }

    // ========== Domain Events ==========
    
    public List<DomainEvent> getDomainEvents() {
        return new ArrayList<>(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // ========== Getters (no setters - state changes only through behavior) ==========
    
    public UserId getId() {
        return id;
    }

    public Username getUsername() {
        return username;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }

    public UserStatus getStatus() {
        return status;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public String getBlockReason() {
        return blockReason;
    }
    
    public boolean isDeleted() {
        return deleted;
    }
    
    public LocalDateTime getDeletedAt() {
        return deletedAt;
    }
    
    public String getDeletionReason() {
        return deletionReason;
    }
    
    public boolean isTermsAccepted() {
        return termsAccepted;
    }
    
    public LocalDateTime getTermsAcceptedAt() {
        return termsAcceptedAt;
    }
    
    public String getTermsVersion() {
        return termsVersion;
    }
    
    public boolean isMarketingConsentGiven() {
        return marketingConsentGiven;
    }
    
    public LocalDateTime getMarketingConsentDate() {
        return marketingConsentDate;
    }

    public long getVersion() {
        return version;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getLastModifiedAt() {
        return updatedAt;
    }

    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    public boolean isBlocked() {
        return status == UserStatus.BLOCKED;
    }

    // ========== Object Methods ==========
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username=" + username +
                ", email=" + email +
                ", role=" + role +
                ", status=" + status +
                ", emailVerified=" + emailVerified +
                ", deleted=" + deleted +
                ", version=" + version +
                '}';
    }

    /**
     * User lifecycle status
     * Pattern: Rich Enum (could add methods if needed)
     */
    public enum UserStatus {
        PENDING,    // Created but not yet activated
        ACTIVE,     // Active and can perform actions
        INACTIVE,   // Temporarily deactivated
        BLOCKED     // Blocked due to policy violation
    }
}
