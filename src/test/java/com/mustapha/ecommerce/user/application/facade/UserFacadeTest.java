package com.mustapha.ecommerce.user.application.facade;

import com.mustapha.ecommerce.user.application.command.*;
import com.mustapha.ecommerce.user.application.usecase.*;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserFacade Unit Tests")
class UserFacadeTest {

    @Mock
    private RegisterUserUseCase registerUserUseCase;
    @Mock
    private ActivateUserUseCase activateUserUseCase;
    @Mock
    private DeactivateUserUseCase deactivateUserUseCase;
    @Mock
    private BlockUserUseCase blockUserUseCase;
    @Mock
    private UnblockUserUseCase unblockUserUseCase;
    @Mock
    private DeleteUserUseCase deleteUserUseCase;
    @Mock
    private ChangeEmailUseCase changeEmailUseCase;
    @Mock
    private VerifyEmailUseCase verifyEmailUseCase;
    @Mock
    private ChangePasswordUseCase changePasswordUseCase;
    @Mock
    private GrantMarketingConsentUseCase grantMarketingConsentUseCase;
    @Mock
    private RevokeMarketingConsentUseCase revokeMarketingConsentUseCase;
    @Mock
    private GetUserByIdUseCase getUserByIdUseCase;
    @Mock
    private GetUserByEmailUseCase getUserByEmailUseCase;
    @Mock
    private GetUserByUsernameUseCase getUserByUsernameUseCase;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private com.mustapha.ecommerce.user.domain.service.CommonPasswordChecker commonPasswordChecker;
    @Mock
    private com.mustapha.ecommerce.user.domain.service.PasswordBreachChecker passwordBreachChecker;
    @Mock
    private com.mustapha.ecommerce.user.domain.repository.UserRepository userRepository;

    @InjectMocks
    private UserFacade userFacade;

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
    @DisplayName("Register User")
    class RegisterUserTests {

        @Test
        @DisplayName("Should register user and return UserResponse")
        void shouldRegisterUser() {
            RegisterUserRequest request = new RegisterUserRequest();
            request.setEmail("john@example.com");
            request.setPassword("SecurePass123!");
            request.setUsername("johndoe");
            request.setTermsAccepted(true);

            when(passwordHasher.hash(anyString())).thenReturn("$2a$10$hashedpassword");
            when(registerUserUseCase.execute(any(RegisterUserCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.registerUser(request);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("johndoe");
            assertThat(response.getEmail()).isEqualTo("john@example.com");
            verify(registerUserUseCase, times(1)).execute(any(RegisterUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Activate User")
    class ActivateUserTests {

        @Test
        @DisplayName("Should activate user by ID")
        void shouldActivateUser() {
            String userId = mockUser.getId().toString();
            when(activateUserUseCase.execute(any(ActivateUserCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.activateUser(userId);

            assertThat(response).isNotNull();
            verify(activateUserUseCase, times(1)).execute(any(ActivateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Deactivate User")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user by ID")
        void shouldDeactivateUser() {
            String userId = mockUser.getId().toString();
            when(deactivateUserUseCase.execute(any(DeactivateUserCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.deactivateUser(userId);

            assertThat(response).isNotNull();
            verify(deactivateUserUseCase, times(1)).execute(any(DeactivateUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Block User")
    class BlockUserTests {

        @Test
        @DisplayName("Should block user with reason")
        void shouldBlockUser() {
            String userId = mockUser.getId().toString();
            when(blockUserUseCase.execute(any(BlockUserCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.blockUser(userId, "Violating terms");

            assertThat(response).isNotNull();
            verify(blockUserUseCase, times(1)).execute(any(BlockUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Unblock User")
    class UnblockUserTests {

        @Test
        @DisplayName("Should unblock user")
        void shouldUnblockUser() {
            String userId = mockUser.getId().toString();
            when(unblockUserUseCase.execute(any(UnblockUserCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.unblockUser(userId);

            assertThat(response).isNotNull();
            verify(unblockUserUseCase, times(1)).execute(any(UnblockUserCommand.class));
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

            UserResponse response = userFacade.deleteUser(userId, "GDPR request");

            assertThat(response).isNotNull();
            verify(deleteUserUseCase, times(1)).execute(any(DeleteUserCommand.class));
        }
    }

    @Nested
    @DisplayName("Change Email")
    class ChangeEmailTests {

        @Test
        @DisplayName("Should change user email")
        void shouldChangeEmail() {
            String userId = mockUser.getId().toString();
            ChangeEmailRequest request = new ChangeEmailRequest();
            request.setNewEmail("newemail@example.com");

            when(changeEmailUseCase.execute(any(ChangeEmailCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.changeEmail(userId, request);

            assertThat(response).isNotNull();
            verify(changeEmailUseCase, times(1)).execute(any(ChangeEmailCommand.class));
        }
    }

    @Nested
    @DisplayName("Verify Email")
    class VerifyEmailTests {

        @Test
        @DisplayName("Should verify user email")
        void shouldVerifyEmail() {
            String userId = mockUser.getId().toString();
            when(verifyEmailUseCase.execute(any(VerifyEmailCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.verifyEmail(userId);

            assertThat(response).isNotNull();
            verify(verifyEmailUseCase, times(1)).execute(any(VerifyEmailCommand.class));
        }
    }

    @Nested
    @DisplayName("Change Password")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should change user password")
        void shouldChangePassword() {
            String userId = mockUser.getId().toString();
            ChangePasswordRequest request = new ChangePasswordRequest();
            request.setCurrentPassword("OldPass123!");
            request.setNewPassword("NewPass123!");

            when(changePasswordUseCase.execute(any(ChangePasswordCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.changePassword(userId, request);

            assertThat(response).isNotNull();
            verify(changePasswordUseCase, times(1)).execute(any(ChangePasswordCommand.class));
        }
    }

    @Nested
    @DisplayName("Marketing Consent")
    class MarketingConsentTests {

        @Test
        @DisplayName("Should grant marketing consent")
        void shouldGrantMarketingConsent() {
            String userId = mockUser.getId().toString();
            when(grantMarketingConsentUseCase.execute(any(GrantMarketingConsentCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.grantMarketingConsent(userId);

            assertThat(response).isNotNull();
            verify(grantMarketingConsentUseCase, times(1)).execute(any(GrantMarketingConsentCommand.class));
        }

        @Test
        @DisplayName("Should revoke marketing consent")
        void shouldRevokeMarketingConsent() {
            String userId = mockUser.getId().toString();
            when(revokeMarketingConsentUseCase.execute(any(RevokeMarketingConsentCommand.class))).thenReturn(mockUser);

            UserResponse response = userFacade.revokeMarketingConsent(userId);

            assertThat(response).isNotNull();
            verify(revokeMarketingConsentUseCase, times(1)).execute(any(RevokeMarketingConsentCommand.class));
        }
    }

    @Nested
    @DisplayName("Get User Queries")
    class GetUserTests {

        @Test
        @DisplayName("Should get user by ID")
        void shouldGetUserById() {
            String userId = mockUser.getId().toString();
            when(getUserByIdUseCase.execute(any(GetUserByIdQuery.class))).thenReturn(mockUser);

            UserResponse response = userFacade.getUserById(userId);

            assertThat(response).isNotNull();
            assertThat(response.getUsername()).isEqualTo("johndoe");
            verify(getUserByIdUseCase, times(1)).execute(any(GetUserByIdQuery.class));
        }

        @Test
        @DisplayName("Should get user by email")
        void shouldGetUserByEmail() {
            when(getUserByEmailUseCase.execute(any(GetUserByEmailQuery.class))).thenReturn(mockUser);

            UserResponse response = userFacade.getUserByEmail("john@example.com");

            assertThat(response).isNotNull();
            verify(getUserByEmailUseCase, times(1)).execute(any(GetUserByEmailQuery.class));
        }

        @Test
        @DisplayName("Should get user by username")
        void shouldGetUserByUsername() {
            when(getUserByUsernameUseCase.execute(any(GetUserByUsernameQuery.class))).thenReturn(mockUser);

            UserResponse response = userFacade.getUserByUsername("johndoe");

            assertThat(response).isNotNull();
            verify(getUserByUsernameUseCase, times(1)).execute(any(GetUserByUsernameQuery.class));
        }
    }
}
