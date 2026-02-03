package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.application.port.EmailService;
import com.mustapha.ecommerce.user.auth.application.command.PasswordResetRequestCommand;
import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetRequestUseCase Tests")
class PasswordResetRequestUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetRequestUseCase passwordResetRequestUseCase;

    private User testUser;
    private PasswordResetRequestCommand command;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        command = new PasswordResetRequestCommand(Email.of("john@example.com"));
    }

    @Nested
    @DisplayName("Successful Password Reset Request")
    class SuccessfulRequestTests {

        @Test
        @DisplayName("Should create token and send email when user exists")
        void shouldCreateTokenAndSendEmail() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

            passwordResetRequestUseCase.execute(command);

            verify(userRepository, times(1)).findByEmail(any(Email.class));
            verify(tokenRepository, times(1)).deleteAllByUserId(any(UserId.class));
            verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
            verify(emailService, times(1)).sendPasswordResetEmail(anyString(), anyString());
            verify(eventPublisher, times(1)).publish(any());
        }
    }

    @Nested
    @DisplayName("Security Behavior")
    class SecurityBehaviorTests {

        @Test
        @DisplayName("Should silently fail when user not found")
        void shouldSilentlyFailWhenUserNotFound() {
            when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

            passwordResetRequestUseCase.execute(command);

            verify(userRepository, times(1)).findByEmail(any(Email.class));
            verify(tokenRepository, never()).deleteAllByUserId(any());
            verify(tokenRepository, never()).save(any(PasswordResetToken.class));
            verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
            verify(eventPublisher, never()).publish(any());
        }
    }
}
