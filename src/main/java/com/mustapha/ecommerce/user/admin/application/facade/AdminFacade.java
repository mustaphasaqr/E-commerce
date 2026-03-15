package com.mustapha.ecommerce.user.admin.application.facade;

import com.mustapha.ecommerce.user.admin.application.command.GetAllUsersCommand;
import com.mustapha.ecommerce.user.admin.application.command.SearchUsersCommand;
import com.mustapha.ecommerce.user.admin.application.command.ChangeUserRoleCommand;
import com.mustapha.ecommerce.user.admin.application.usecase.GetAllUsersUseCase;
import com.mustapha.ecommerce.user.admin.application.usecase.SearchUsersUseCase;
import com.mustapha.ecommerce.user.admin.application.usecase.ChangeUserRoleUseCase;
import com.mustapha.ecommerce.user.application.command.*;
import com.mustapha.ecommerce.user.application.usecase.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Role;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Admin Facade
 * Responsibility: Orchestrate admin operations on User aggregate
 * Pattern: Facade (simplifies admin use case interaction)
 * 
 * Scope: Admin operations requiring OWNER role
 * Purpose: Provides admin-specific interface for user management
 * 
 * Security: All endpoints using this facade should be protected with @PreAuthorize("hasRole('OWNER')")
 */
@Component
public class AdminFacade {

    private final BlockUserUseCase blockUserUseCase;
    private final UnblockUserUseCase unblockUserUseCase;
    private final ActivateUserUseCase activateUserUseCase;
    private final DeactivateUserUseCase deactivateUserUseCase;
    private final DeleteUserUseCase deleteUserUseCase;
    private final GetAllUsersUseCase getAllUsersUseCase;
    private final SearchUsersUseCase searchUsersUseCase;
    private final ChangeUserRoleUseCase changeUserRoleUseCase;

    public AdminFacade(BlockUserUseCase blockUserUseCase,
                      UnblockUserUseCase unblockUserUseCase,
                      ActivateUserUseCase activateUserUseCase,
                      DeactivateUserUseCase deactivateUserUseCase,
                      DeleteUserUseCase deleteUserUseCase,
                      GetAllUsersUseCase getAllUsersUseCase,
                      SearchUsersUseCase searchUsersUseCase,
                      ChangeUserRoleUseCase changeUserRoleUseCase) {
        this.blockUserUseCase = blockUserUseCase;
        this.unblockUserUseCase = unblockUserUseCase;
        this.activateUserUseCase = activateUserUseCase;
        this.deactivateUserUseCase = deactivateUserUseCase;
        this.deleteUserUseCase = deleteUserUseCase;
        this.getAllUsersUseCase = getAllUsersUseCase;
        this.searchUsersUseCase = searchUsersUseCase;
        this.changeUserRoleUseCase = changeUserRoleUseCase;
    }

    /**
     * Block user account (OWNER only)
     */
    public User blockUser(String userId, String reason) {
        BlockUserCommand command = new BlockUserCommand(UserId.of(userId), reason);
        return blockUserUseCase.execute(command);
    }

    public User unblockUser(String userId, String reason) {
        UnblockUserCommand command = new UnblockUserCommand(UserId.of(userId), reason);
        return unblockUserUseCase.execute(command);
    }

    public User activateUser(String userId, String activationNote) {
        ActivateUserCommand command = new ActivateUserCommand(UserId.of(userId), activationNote);
        return activateUserUseCase.execute(command);
    }

    public User deactivateUser(String userId, String reason) {
        DeactivateUserCommand command = new DeactivateUserCommand(UserId.of(userId), reason);
        return deactivateUserUseCase.execute(command);
    }

    /**
     * Delete user account (OWNER only - soft delete for GDPR)
     */
    public User deleteUser(String userId, String reason, String requestedByUserId) {
        DeleteUserCommand command = new DeleteUserCommand(
            UserId.of(userId),
            reason,
            requestedByUserId != null ? UserId.of(requestedByUserId) : null
        );
        return deleteUserUseCase.execute(command);
    }

    public Page<User> getAllUsers(Pageable pageable) {
        GetAllUsersCommand command = new GetAllUsersCommand(pageable);
        return getAllUsersUseCase.execute(command);
    }

    public Page<User> searchUsers(String email, String username, User.UserStatus status, Role role, Pageable pageable) {
        SearchUsersCommand command = new SearchUsersCommand(email, username, status, role, pageable);
        return searchUsersUseCase.execute(command);
    }

    public User changeUserRole(String userId, Role newRole, String changedBy) {
        ChangeUserRoleCommand command = new ChangeUserRoleCommand(userId, newRole, changedBy);
        return changeUserRoleUseCase.execute(command);
    }
}
