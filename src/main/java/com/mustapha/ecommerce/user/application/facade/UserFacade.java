package com.mustapha.ecommerce.user.application.facade;

import com.mustapha.ecommerce.user.application.command.*;
import com.mustapha.ecommerce.user.application.usecase.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.dto.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
                     PasswordHasher passwordHasher) {
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
    }

    /**
     * Register User
     */
    public UserResponse registerUser(RegisterUserRequest request) {
        RegisterUserCommand command = new RegisterUserCommand(
            Email.of(request.getEmail()),
            Password.fromPlainText(request.getPassword(), passwordHasher),
            Username.of(request.getUsername()),
            Role.CUSTOMER, // Default role
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
     */
    public UserResponse changePassword(String userId, ChangePasswordRequest request) {
        ChangePasswordCommand command = new ChangePasswordCommand(
            UserId.of(UUID.fromString(userId)),
            request.getCurrentPassword(),
            request.getNewPassword()
        );
        User user = changePasswordUseCase.execute(command);
        return UserResponse.fromDomain(user);
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
        GetUserByUsernameQuery query = new GetUserByUsernameQuery(Username.of(username));
        User user = getUserByUsernameUseCase.execute(query);
        return UserResponse.fromDomain(user);
    }
}
