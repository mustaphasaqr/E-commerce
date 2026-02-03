package com.mustapha.ecommerce.user.admin.application.facade;

import com.mustapha.ecommerce.user.admin.application.command.*;
import com.mustapha.ecommerce.user.admin.application.usecase.*;
import com.mustapha.ecommerce.user.application.command.*;
import com.mustapha.ecommerce.user.application.usecase.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminFacade Unit Tests")
class AdminFacadeTest {

    @Mock
    private BlockUserUseCase blockUserUseCase;
    @Mock
    private UnblockUserUseCase unblockUserUseCase;
    @Mock
    private ActivateUserUseCase activateUserUseCase;
    @Mock
    private DeactivateUserUseCase deactivateUserUseCase;
    @Mock
    private DeleteUserUseCase deleteUserUseCase;
    @Mock
    private GetAllUsersUseCase getAllUsersUseCase;
    @Mock
    private SearchUsersUseCase searchUsersUseCase;
    @Mock
    private ChangeUserRoleUseCase changeUserRoleUseCase;

    @InjectMocks
    private AdminFacade adminFacade;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
    }

    @Nested
    @DisplayName("Block User")
    class BlockUserTests {

        @Test
        @DisplayName("Should block user with reason")
        void shouldBlockUser() {
            String userId = mockUser.getId().toString();
            when(blockUserUseCase.execute(any(BlockUserCommand.class))).thenReturn(mockUser);

            User result = adminFacade.blockUser(userId, "Violating terms");

            assertThat(result).isNotNull();
            assertThat(result.getUsername().getValue()).isEqualTo("johndoe");
            verify(blockUserUseCase, times(1)).execute(any(BlockUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Unblock User")
    class UnblockUserTests {

        @Test
        @DisplayName("Should unblock user with reason")
        void shouldUnblockUser() {
            String userId = mockUser.getId().toString();
            when(unblockUserUseCase.execute(any(UnblockUserCommand.class))).thenReturn(mockUser);

            User result = adminFacade.unblockUser(userId, "Appeal accepted");

            assertThat(result).isNotNull();
            verify(unblockUserUseCase, times(1)).execute(any(UnblockUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Activate User")
    class ActivateUserTests {

        @Test
        @DisplayName("Should activate user with activation note")
        void shouldActivateUser() {
            String userId = mockUser.getId().toString();
            when(activateUserUseCase.execute(any(ActivateUserCommand.class))).thenReturn(mockUser);

            User result = adminFacade.activateUser(userId, "Manual activation");

            assertThat(result).isNotNull();
            verify(activateUserUseCase, times(1)).execute(any(ActivateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Deactivate User")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user with reason")
        void shouldDeactivateUser() {
            String userId = mockUser.getId().toString();
            when(deactivateUserUseCase.execute(any(DeactivateUserCommand.class))).thenReturn(mockUser);

            User result = adminFacade.deactivateUser(userId, "Account suspended");

            assertThat(result).isNotNull();
            verify(deactivateUserUseCase, times(1)).execute(any(DeactivateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user with reason")
        void shouldDeleteUser() {
            String userId = mockUser.getId().toString();
            when(deleteUserUseCase.execute(any(DeleteUserCommand.class))).thenReturn(mockUser);

            User result = adminFacade.deleteUser(userId, "GDPR request");

            assertThat(result).isNotNull();
            verify(deleteUserUseCase, times(1)).execute(any(DeleteUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Get All Users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return paginated list of users")
        void shouldGetAllUsers() {
            User user2 = User.create(
                Username.of("janedoe"),
                Email.of("jane@example.com"),
                Password.fromHashed("$2a$10$hashedpassword"),
                Role.EMPLOYEE
            );

            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser, user2), PageRequest.of(0, 20), 2);
            when(getAllUsersUseCase.execute(any(GetAllUsersCommand.class))).thenReturn(userPage);

            Page<User> result = adminFacade.getAllUsers(PageRequest.of(0, 20));

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(getAllUsersUseCase, times(1)).execute(any(GetAllUsersCommand.class));
        }
    }

    @Nested
    @DisplayName("Search Users")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users with criteria")
        void shouldSearchUsers() {
            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser), PageRequest.of(0, 20), 1);
            when(searchUsersUseCase.execute(any(SearchUsersCommand.class))).thenReturn(userPage);

            Page<User> result = adminFacade.searchUsers(
                "john@example.com",
                "johndoe",
                User.UserStatus.PENDING,
                Role.CUSTOMER,
                PageRequest.of(0, 20)
            );

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getUsername().getValue()).isEqualTo("johndoe");
            verify(searchUsersUseCase, times(1)).execute(any(SearchUsersCommand.class));
        }

        @Test
        @DisplayName("Should search users with null criteria")
        void shouldSearchUsersWithNullCriteria() {
            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser), PageRequest.of(0, 20), 1);
            when(searchUsersUseCase.execute(any(SearchUsersCommand.class))).thenReturn(userPage);

            Page<User> result = adminFacade.searchUsers(null, null, null, null, PageRequest.of(0, 20));

            assertThat(result).isNotNull();
            verify(searchUsersUseCase, times(1)).execute(any(SearchUsersCommand.class));
        }
    }

    @Nested
    @DisplayName("Change User Role")
    class ChangeUserRoleTests {

        @Test
        @DisplayName("Should change user role")
        void shouldChangeUserRole() {
            String userId = mockUser.getId().toString();
            when(changeUserRoleUseCase.execute(any(ChangeUserRoleCommand.class))).thenReturn(mockUser);

            User result = adminFacade.changeUserRole(userId, Role.EMPLOYEE, "admin");

            assertThat(result).isNotNull();
            verify(changeUserRoleUseCase, times(1)).execute(any(ChangeUserRoleCommand.class));
        }
    }
}
