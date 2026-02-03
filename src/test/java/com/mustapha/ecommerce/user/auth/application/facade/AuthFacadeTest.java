package com.mustapha.ecommerce.user.auth.application.facade;

import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.user.auth.application.command.*;
import com.mustapha.ecommerce.user.auth.application.usecase.*;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthFacade Unit Tests")
class AuthFacadeTest {

    @Mock
    private LoginUseCase loginUseCase;
    @Mock
    private LogoutUseCase logoutUseCase;
    @Mock
    private RefreshTokenUseCase refreshTokenUseCase;
    @Mock
    private PasswordResetRequestUseCase passwordResetRequestUseCase;
    @Mock
    private PasswordResetCompleteUseCase passwordResetCompleteUseCase;
    @Mock
    private LogoutAllDevicesUseCase logoutAllDevicesUseCase;
    @Mock
    private RequestEmailVerificationUseCase requestEmailVerificationUseCase;
    @Mock
    private VerifyEmailWithTokenUseCase verifyEmailWithTokenUseCase;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private JwtTokenGenerator jwtTokenGenerator;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthFacade authFacade;

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
    @DisplayName("Login")
    class LoginTests {

        @Test
        @DisplayName("Should login user and return LoginResponse with tokens")
        void shouldLoginUser() {
            LoginRequest request = new LoginRequest();
            request.setEmail("john@example.com");
            request.setPassword("SecurePass123!");

            LoginUseCase.LoginResult loginResult = new LoginUseCase.LoginResult(
                mockUser,
                "refresh-token-123",
                "session-123"
            );

            when(loginUseCase.execute(any(LoginCommand.class))).thenReturn(loginResult);
            when(jwtTokenGenerator.generateAccessToken(anyString(), anyString(), anyString()))
                .thenReturn("jwt-access-token");

            LoginResponse response = authFacade.login(request, "192.168.1.1", "Mozilla/5.0");

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("jwt-access-token");
            assertThat(response.getRefreshToken()).isEqualTo("refresh-token-123");
            assertThat(response.getSessionId()).isEqualTo("session-123");
            assertThat(response.getUser()).isNotNull();
            assertThat(response.getUser().getUsername()).isEqualTo("johndoe");

            verify(loginUseCase, times(1)).execute(any(LoginCommand.class));
            verify(jwtTokenGenerator, times(1)).generateAccessToken(anyString(), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("Should logout user successfully")
        void shouldLogoutUser() {
            String userId = mockUser.getId().toString();
            doNothing().when(logoutUseCase).execute(any(LogoutCommand.class));

            authFacade.logout(userId, "session-123", "mock-jwt-token");

            verify(logoutUseCase, times(1)).execute(any(LogoutCommand.class));
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token and return new tokens")
        void shouldRefreshToken() {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("old-refresh-token");

            String userId = mockUser.getId().toString();
            RefreshToken existingToken = RefreshToken.create(userId);

            RefreshTokenUseCase.RefreshResult refreshResult = new RefreshTokenUseCase.RefreshResult(
                "new-refresh-token",
                "new-session-123"
            );

            when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(existingToken));
            when(refreshTokenUseCase.execute(any(RefreshTokenCommand.class))).thenReturn(refreshResult);
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(mockUser));
            when(jwtTokenGenerator.generateAccessToken(anyString(), anyString(), anyString()))
                .thenReturn("new-jwt-token");

            TokenResponse response = authFacade.refreshToken(request, "192.168.1.1", "Mozilla/5.0");

            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new-jwt-token");
            assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");

            verify(refreshTokenUseCase, times(1)).execute(any(RefreshTokenCommand.class));
        }
    }

    @Nested
    @DisplayName("Password Reset Request")
    class PasswordResetRequestTests {

        @Test
        @DisplayName("Should request password reset")
        void shouldRequestPasswordReset() {
            PasswordResetRequestRequest request = new PasswordResetRequestRequest();
            request.setEmail("john@example.com");

            doNothing().when(passwordResetRequestUseCase).execute(any(PasswordResetRequestCommand.class));

            authFacade.requestPasswordReset(request);

            verify(passwordResetRequestUseCase, times(1)).execute(any(PasswordResetRequestCommand.class));
        }
    }

    @Nested
    @DisplayName("Password Reset Complete")
    class PasswordResetCompleteTests {

        @Test
        @DisplayName("Should complete password reset")
        void shouldCompletePasswordReset() {
            PasswordResetCompleteRequest request = new PasswordResetCompleteRequest();
            request.setToken("reset-token-123");
            request.setNewPassword("NewPass123!");

            when(passwordHasher.hash(anyString())).thenReturn("$2a$10$newhashedpassword");
            doNothing().when(passwordResetCompleteUseCase).execute(any(PasswordResetCompleteCommand.class));

            authFacade.completePasswordReset(request);

            verify(passwordResetCompleteUseCase, times(1)).execute(any(PasswordResetCompleteCommand.class));
        }
    }

    @Nested
    @DisplayName("Logout All Devices")
    class LogoutAllDevicesTests {

        @Test
        @DisplayName("Should logout all devices except current")
        void shouldLogoutAllDevices() {
            String userId = mockUser.getId().toString();
            doNothing().when(logoutAllDevicesUseCase).execute(any(LogoutAllDevicesCommand.class));

            authFacade.logoutAllDevices(userId, "current-session-123");

            verify(logoutAllDevicesUseCase, times(1)).execute(any(LogoutAllDevicesCommand.class));
        }
    }

    @Nested
    @DisplayName("Request Email Verification")
    class RequestEmailVerificationTests {

        @Test
        @DisplayName("Should request email verification")
        void shouldRequestEmailVerification() {
            RequestEmailVerificationRequest request = new RequestEmailVerificationRequest();
            request.setEmail("john@example.com");

            doNothing().when(requestEmailVerificationUseCase).execute(any(RequestEmailVerificationCommand.class));

            authFacade.requestEmailVerification(request);

            verify(requestEmailVerificationUseCase, times(1)).execute(any(RequestEmailVerificationCommand.class));
        }
    }

    @Nested
    @DisplayName("Verify Email With Token")
    class VerifyEmailWithTokenTests {

        @Test
        @DisplayName("Should verify email with token")
        void shouldVerifyEmailWithToken() {
            VerifyEmailWithTokenRequest request = new VerifyEmailWithTokenRequest();
            request.setToken("verification-token-123");

            doNothing().when(verifyEmailWithTokenUseCase).execute(any(VerifyEmailWithTokenCommand.class));

            authFacade.verifyEmailWithToken(request);

            verify(verifyEmailWithTokenUseCase, times(1)).execute(any(VerifyEmailWithTokenCommand.class));
        }
    }
}
