package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.VerifyEmailWithTokenCommand;
import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.auth.domain.repository.EmailVerificationTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
@DisplayName("VerifyEmailWithTokenUseCase Tests")
class VerifyEmailWithTokenUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationTokenRepository tokenRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private VerifyEmailWithTokenUseCase verifyEmailWithTokenUseCase;

    private User testUser;
    private EmailVerificationToken token;
    private VerifyEmailWithTokenCommand command;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        token = EmailVerificationToken.create(
            testUser.getId().getValue().toString(),
            testUser.getEmail().getValue()
        );

        command = new VerifyEmailWithTokenCommand(token.getToken());
    }

    @Test
    @DisplayName("Should verify email successfully")
    void shouldVerifyEmail() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenReturn(token);

        verifyEmailWithTokenUseCase.execute(command);

        verify(tokenRepository, times(1)).findByToken(anyString());
        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(tokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(tokenRepository, times(1)).delete(anyString());
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    @DisplayName("Should throw when token not found")
    void shouldThrowWhenTokenNotFound() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailWithTokenUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Invalid or expired verification token");

        verify(userRepository, never()).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(tokenRepository.findByToken(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> verifyEmailWithTokenUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }
}
