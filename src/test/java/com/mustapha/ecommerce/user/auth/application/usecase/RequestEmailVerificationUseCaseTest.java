package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.application.port.EmailService;
import com.mustapha.ecommerce.user.auth.application.command.RequestEmailVerificationCommand;
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
@DisplayName("RequestEmailVerificationUseCase Tests")
class RequestEmailVerificationUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EmailVerificationTokenRepository tokenRepository;
    @Mock
    private EmailService emailService;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private RequestEmailVerificationUseCase requestEmailVerificationUseCase;

    private User testUser;
    private RequestEmailVerificationCommand command;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        command = new RequestEmailVerificationCommand(Email.of("john@example.com"));
    }

    @Test
    @DisplayName("Should create token and send email successfully")
    void shouldCreateTokenAndSendEmail() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));
        when(tokenRepository.save(any(EmailVerificationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        requestEmailVerificationUseCase.execute(command);

        verify(userRepository, times(1)).findByEmail(any(Email.class));
        verify(tokenRepository, times(1)).save(any(EmailVerificationToken.class));
        verify(emailService, times(1)).sendEmailVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> requestEmailVerificationUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(tokenRepository, never()).save(any(EmailVerificationToken.class));
        verify(emailService, never()).sendEmailVerificationEmail(anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw when email already verified")
    void shouldThrowWhenEmailAlreadyVerified() {
        testUser.verifyEmail();
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> requestEmailVerificationUseCase.execute(command))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Email already verified");

        verify(tokenRepository, never()).save(any(EmailVerificationToken.class));
        verify(emailService, never()).sendEmailVerificationEmail(anyString(), anyString());
    }
}
