package com.mustapha.ecommerce.user.application.facade;

import com.mustapha.ecommerce.user.application.command.*;
import com.mustapha.ecommerce.user.application.usecase.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.service.CommonPasswordChecker;
import com.mustapha.ecommerce.user.domain.service.PasswordBreachChecker;
import com.mustapha.ecommerce.user.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * User Facade - Translation Layer between API and Application
 * 
 * Responsibilities:
 * 1. Accept API DTOs (Request with primitives)
 * 2. Convert primitives → value objects → Commands
 * 3. Delegate to Use Cases (no business logic here)
 * 4. Convert Domain → API DTOs (Response)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 */
@Service
public class UserFacade {

    private static final Logger logger = LoggerFactory.getLogger(UserFacade.class);

    private final RegisterUserUseCase registerUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final BlockUserUseCase blockUserUseCase;
    private final UnblockUserUseCase unblockUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final ChangeEmailUseCase changeEmailUseCase;
    private final VerifyEmailUseCase verifyEmailUseCase;
    private final ChangePasswordUseCase changePasswordUseCase;
    private final GrantMarketingConsentUseCase grantMarketingConsentUseCase;
    private final RevokeMarketingConsentUseCase revokeMarketingConsentUseCase;
    private final GetUserByIdUseCase getUserByIdUseCase;
    private final GetUserByEmailUseCase getUserByEmailUseCase;
    private final GetUserByUsernameUseCase getUserByUsernameUseCase;
    private final PasswordHasher passwordHasher;
    private final CommonPasswordChecker commonPasswordChecker;
    private final PasswordBreachChecker passwordBreachChecker;
    private final com.mustapha.ecommerce.user.domain.repository.UserRepository userRepository;

    @Value("${app.owner.one-time-signup.enabled:false}")
    private boolean ownerOneTimeSignupEnabled;

    @Value("${app.owner.one-time-signup.email:owner@mecommerce.com}")
    private String ownerOneTimeSignupEmail;

    @Value("${app.owner.one-time-signup.username:owner}")
    private String ownerOneTimeSignupUsername;

    @PostConstruct
    void syncOneTimeOwnerSignupFlagForUsernameValidation() {
        // Username value object is static and reads JVM properties/env directly.
        // Keep it in sync with Spring configuration toggle.
        if (ownerOneTimeSignupEnabled) {
            System.setProperty("app.owner.one-time-signup.enabled", "true");
        } else {
            System.clearProperty("app.owner.one-time-signup.enabled");
        }
    }

    public UserFacade(RegisterUserUseCase registerUserUseCase,
                     ActivateUserUseCase activateUserUseCase,
                     DeactivateUserUseCase deactivateUserUseCase,
                     BlockUserUseCase blockUserUseCase,
                     UnblockUserUseCase unblockUserUseCase,
                     DeleteUserUseCase deleteUserUseCase,
                     ChangeEmailUseCase changeEmailUseCase,
                     VerifyEmailUseCase verifyEmailUseCase,
                     ChangePasswordUseCase changePasswordUseCase,
                     GrantMarketingConsentUseCase grantMarketingConsentUseCase,
                     RevokeMarketingConsentUseCase revokeMarketingConsentUseCase,
                     GetUserByIdUseCase getUserByIdUseCase,
                     GetUserByEmailUseCase getUserByEmailUseCase,
                     GetUserByUsernameUseCase getUserByUsernameUseCase,
                     PasswordHasher passwordHasher,
                     CommonPasswordChecker commonPasswordChecker,
                     PasswordBreachChecker passwordBreachChecker,
                     com.mustapha.ecommerce.user.domain.repository.UserRepository userRepository) {
        this.registerUserUseCase = registerUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.blockUserUseCase = blockUserUseCase;
        this.unblockUserUseCase = unblockUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.changeEmailUseCase = changeEmailUseCase;
        this.verifyEmailUseCase = verifyEmailUseCase;
        this.changePasswordUseCase = changePasswordUseCase;
        this.grantMarketingConsentUseCase = grantMarketingConsentUseCase;
        this.revokeMarketingConsentUseCase = revokeMarketingConsentUseCase;
        this.getUserByIdUseCase = getUserByIdUseCase;
        this.getUserByEmailUseCase = getUserByEmailUseCase;
        this.getUserByUsernameUseCase = getUserByUsernameUseCase;
        this.passwordHasher = passwordHasher;
        this.commonPasswordChecker = commonPasswordChecker;
        this.passwordBreachChecker = passwordBreachChecker;
        this.userRepository = userRepository;
    }

    /**
     * Register User
     * Enhanced with password security checks:
     * 1. Common password check (prevents "password123", etc.)
     * 2. Breach check (via HaveIBeenPwned API)
     */
    public UserResponse registerUser(RegisterUserRequest request) {
        // Enhanced password validation BEFORE hashing
        // Note: Password.from PlainText already validates basic strength (length, chars, etc.)
        validatePasswordSecurity(request.getPassword());

        Role targetRole = Role.CUSTOMER;
        if (ownerOneTimeSignupEnabled
            && request.getEmail() != null
            && request.getUsername() != null
            && ownerOneTimeSignupEmail.equalsIgnoreCase(request.getEmail().trim())
            && ownerOneTimeSignupUsername.equalsIgnoreCase(request.getUsername().trim())) {
            targetRole = Role.OWNER;
            logger.warn("⚠️ One-time OWNER signup path used for email={}. Disable app.owner.one-time-signup.enabled after account creation.", request.getEmail());
        }
        
        RegisterUserCommand command = new RegisterUserCommand(
            Email.of(request.getEmail()),
            Password.fromPlainText(request.getPassword(), passwordHasher),
            Username.of(request.getUsername()),
            targetRole,
            request.getTermsAccepted()
        );
        User user = registerUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Activate User
     */
    public UserResponse activateUser(String userId) {
        ActivateUserCommand command = new ActivateUserCommand(UserId.of(UUID.fromString(userId)), null);
        User user = activateUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Deactivate User
     */
    public UserResponse deactivateUser(String userId) {
        DeactivateUserCommand command = new DeactivateUserCommand(UserId.of(UUID.fromString(userId)), null);
        User user = deactivateUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Block User
     */
    public UserResponse blockUser(String userId, String reason) {
        BlockUserCommand command = new BlockUserCommand(UserId.of(UUID.fromString(userId)), reason);
        User user = blockUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Unblock User
     */
    public UserResponse unblockUser(String userId) {
        UnblockUserCommand command = new UnblockUserCommand(UserId.of(UUID.fromString(userId)), null);
        User user = unblockUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Delete User
     */
    public UserResponse deleteUser(String userId, String reason) {
        DeleteUserCommand command = new DeleteUserCommand(UserId.of(UUID.fromString(userId)), reason);
        User user = deleteUserUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Change Email
     */
    public UserResponse changeEmail(String userId, ChangeEmailRequest request) {
        ChangeEmailCommand command = new ChangeEmailCommand(
            UserId.of(UUID.fromString(userId)),
            Email.of(request.getNewEmail())
        );
        User user = changeEmailUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Verify Email
     */
    public UserResponse verifyEmail(String userId) {
        VerifyEmailCommand command = new VerifyEmailCommand(UserId.of(UUID.fromString(userId)));
        User user = verifyEmailUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Change Password
     * Enhanced with password security checks for new password.
     */
    public UserResponse changePassword(String userId, ChangePasswordRequest request) {
        // Validate new password security BEFORE attempting change
        validatePasswordSecurity(request.getNewPassword());
        
        ChangePasswordCommand command = new ChangePasswordCommand(
            UserId.of(UUID.fromString(userId)),
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        User user = changePasswordUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }
    
    /**
     * Validates password against common passwords and data breaches.
     * 
     * Security Layers:
     * 1. Common password check (offline, instant)
     * 2. Breach check (online via HIBP API, may fail gracefully)
     * 
     * @param plainPassword Plain text password
     * @throws IllegalArgumentException if password is common or breached
     */
    private void validatePasswordSecurity(String plainPassword) {
        // Layer 1: Common password check (always fails if common)
        try {
            commonPasswordChecker.validateNotCommon(plainPassword);
        } catch (IllegalArgumentException e) {
            logger.warn("Password rejected: common password detected");
            throw e; // Re-throw to reject password
        }
        
        // Layer 2: Breach check (fails gracefully if API unavailable)
        try {
            passwordBreachChecker.validateNotBreached(plainPassword);
        } catch (IllegalArgumentException e) {
            logger.warn("Password rejected: found in data breach");
            throw e; // Re-throw to reject password
        } catch (Exception e) {
            // API error: Log but don't block user (graceful degradation)
            logger.error("HIBP API error, allowing password: {}", e.getMessage());
            // Don't throw - allow registration to proceed
        }
    }

    /**
     * Grant Marketing Consent
     */
    public UserResponse grantMarketingConsent(String userId) {
        GrantMarketingConsentCommand command = new GrantMarketingConsentCommand(UserId.of(UUID.fromString(userId)));
        User user = grantMarketingConsentUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Revoke Marketing Consent
     */
    public UserResponse revokeMarketingConsent(String userId) {
        RevokeMarketingConsentCommand command = new RevokeMarketingConsentCommand(UserId.of(UUID.fromString(userId)));
        User user = revokeMarketingConsentUseCase.execute(command);
        return UserResponse.fromDomain(user);
    }

    /**
     * Get User by ID
     */
    public UserResponse getUserById(String userId) {
        GetUserByIdQuery query = new GetUserByIdQuery(UserId.of(UUID.fromString(userId)));
        User user = getUserByIdUseCase.execute(query);
        return UserResponse.fromDomain(user);
    }

    /**
     * Get User by Email
     */
    public UserResponse getUserByEmail(String email) {
        GetUserByEmailQuery query = new GetUserByEmailQuery(Email.of(email));
        User user = getUserByEmailUseCase.execute(query);
        return UserResponse.fromDomain(user);
    }

    /**
     * Get User by Username
     */
    public UserResponse getUserByUsername(String username) {
        GetUserByUsernameQuery query = new GetUserByUsernameQuery(Username.reconstitute(username));
        User user = getUserByUsernameUseCase.execute(query);
        return UserResponse.fromDomain(user);
    }

    /**
     * List All Users - Lightweight DTO for list view
     * Performance: 67% smaller payload than full UserResponse
     * Use for: Admin user management, user directory
     */
    @Transactional(readOnly = true)
    public Page<UserListResponse> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
            .map(UserListResponse::fromDomain);
    }
}
