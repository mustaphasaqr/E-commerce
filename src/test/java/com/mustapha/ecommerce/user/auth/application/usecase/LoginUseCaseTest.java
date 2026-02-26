package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LoginCommand;
import com.mustapha.ecommerce.user.auth.domain.exception.InvalidCredentialsException;
import com.mustapha.ecommerce.user.auth.domain.exception.RateLimitExceededException;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.model.valueobject.Credentials;
import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy;
import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy.RateLimitResult;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.auth.domain.service.AccountLockoutService;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("LoginUseCase Tests")
class LoginUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private LoginSessionRepository loginSessionRepository;
    @Mock
    private LoginRateLimitPolicy rateLimitPolicy;
    @Mock
    private AccountLockoutService accountLockoutService;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private LoginUseCase loginUseCase;

    private User testUser;
    private LoginCommand loginCommand;
    private RefreshToken mockRefreshToken;
    private LoginSession mockSession;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
        testUser.acceptTerms("v1.0");
        testUser.verifyEmail();
        testUser.activate("Admin activation");
        
        loginCommand = new LoginCommand(
            Credentials.of("john@example.com", "plainPassword123"),
            "192.168.1.1",
            "Mozilla/5.0"
        );

        mockRefreshToken = RefreshToken.create(testUser.getId().getValue().toString());
        mockSession = LoginSession.create(
            testUser.getId().getValue().toString(),
            "192.168.1.1",
            "Mozilla/5.0"
        );

        lenient().when(rateLimitPolicy.checkUserRateLimit(anyString())).thenReturn(RateLimitResult.allowed());
        lenient().when(rateLimitPolicy.checkIpRateLimit(anyString())).thenReturn(RateLimitResult.allowed());
        lenient().when(passwordHasher.matches(eq("plainPassword123"), anyString())).thenReturn(true);
        lenient().doNothing().when(accountLockoutService).checkAccountNotLocked(anyString());
        lenient().when(accountLockoutService.recordFailedAttempt(anyString())).thenReturn(false);
        lenient().doNothing().when(accountLockoutService).resetFailedAttempts(anyString());
    }

    @Nested
    @DisplayName("Successful Login")
    class SuccessfulLoginTests {

        @Test
        @DisplayName("Should login successfully with email")
        void shouldLoginWithEmail() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(mockSession);

            LoginUseCase.LoginResult result = loginUseCase.execute(loginCommand);

            assertThat(result).isNotNull();
            assertThat(result.getUser()).isEqualTo(testUser);
            assertThat(result.getRefreshToken()).isNotNull();
            assertThat(result.getSessionId()).isNotNull();

            verify(userRepository, times(1)).findByEmail(any(Email.class));
            verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
            verify(loginSessionRepository, times(1)).save(any(LoginSession.class));
            verify(eventPublisher, times(3)).publish(any());
            verify(rateLimitPolicy, times(1)).recordSuccessfulLogin(anyString(), anyString());
        }

        @Test
        @DisplayName("Should login successfully with username")
        void shouldLoginWithUsername() {
            LoginCommand usernameCommand = new LoginCommand(
                Credentials.of("johndoe", "plainPassword123"),
                "192.168.1.1",
                "Mozilla/5.0"
            );

            lenient().when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.of(testUser));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(mockSession);

            LoginUseCase.LoginResult result = loginUseCase.execute(usernameCommand);

            assertThat(result).isNotNull();
            assertThat(result.getUser()).isEqualTo(testUser);
            verify(userRepository, times(1)).findByUsername(any(Username.class));
            verify(userRepository, never()).findByEmail(any(Email.class));
        }
    }

    @Nested
    @DisplayName("Rate Limiting")
    class RateLimitingTests {

        @Test
        @DisplayName("Should throw when user rate limit exceeded")
        void shouldThrowWhenUserRateLimitExceeded() {
            RateLimitResult denial = RateLimitResult.denied("Too many attempts", null);
            when(rateLimitPolicy.checkUserRateLimit(anyString())).thenReturn(denial);

            assertThatThrownBy(() -> loginUseCase.execute(loginCommand))
                .isInstanceOf(RateLimitExceededException.class);

            verify(userRepository, never()).findByEmail(any());
        }

        @Test
        @DisplayName("Should throw when IP rate limit exceeded")
        void shouldThrowWhenIpRateLimitExceeded() {
            RateLimitResult denial = RateLimitResult.denied("Too many attempts from IP", null);
            when(rateLimitPolicy.checkIpRateLimit(anyString())).thenReturn(denial);

            assertThatThrownBy(() -> loginUseCase.execute(loginCommand))
                .isInstanceOf(RateLimitExceededException.class);

            verify(userRepository, never()).findByEmail(any());
        }
    }

    @Nested
    @DisplayName("Authentication Failures")
    class AuthenticationFailureTests {

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            lenient().when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> loginUseCase.execute(loginCommand))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(rateLimitPolicy, times(1)).recordFailedAttempt(anyString(), anyString());
            verify(refreshTokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw when password is incorrect")
        void shouldThrowWhenPasswordIncorrect() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));
            when(passwordHasher.matches(eq("plainPassword123"), anyString())).thenReturn(false);

            assertThatThrownBy(() -> loginUseCase.execute(loginCommand))
                .isInstanceOf(InvalidCredentialsException.class);

            verify(rateLimitPolicy, times(1)).recordFailedAttempt(anyString(), anyString());
            verify(refreshTokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Event Publishing")
    class EventPublishingTests {

        @Test
        @DisplayName("Should publish 3 events on successful login")
        void shouldPublishThreeEvents() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(mockRefreshToken);
            when(loginSessionRepository.save(any(LoginSession.class))).thenReturn(mockSession);

            loginUseCase.execute(loginCommand);

            verify(eventPublisher, times(3)).publish(any());
        }
    }
}
