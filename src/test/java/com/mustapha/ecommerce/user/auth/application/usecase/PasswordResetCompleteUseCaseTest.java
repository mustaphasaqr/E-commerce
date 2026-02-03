package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.PasswordResetCompleteCommand;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PasswordResetCompleteUseCase Tests")
class PasswordResetCompleteUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository tokenRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private PasswordResetCompleteUseCase passwordResetCompleteUseCase;

    private User testUser;
    private PasswordResetToken token;
    private PasswordResetCompleteCommand command;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        token = PasswordResetToken.create(
            testUser.getId().getValue().toString(),
            testUser.getEmail().getValue()
        );

        command = new PasswordResetCompleteCommand(
            token.getToken(),
            Password.fromHashed("$2a$10$newhashedpassword")
        );
    }

    @Nested
    @DisplayName("Successful Password Reset")
    class SuccessfulResetTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPassword() {
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
            when(tokenRepository.save(any(PasswordResetToken.class))).thenReturn(token);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            passwordResetCompleteUseCase.execute(command);

            verify(tokenRepository, times(1)).findByToken(anyString());
            verify(userRepository, times(1)).findById(any(UserId.class));
            verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
            verify(userRepository, times(1)).save(any(User.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }
    }

    @Nested
    @DisplayName("Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("Should throw when token not found")
        void shouldThrowWhenTokenNotFound() {
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetCompleteUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid or expired reset token");

            verify(userRepository, never()).findById(any(UserId.class));
            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> passwordResetCompleteUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

            verify(userRepository, never()).save(any(User.class));
        }
    }
}
