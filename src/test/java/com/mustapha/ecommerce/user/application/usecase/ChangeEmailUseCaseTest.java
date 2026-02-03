package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.ChangeEmailCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangeEmail Use Case Tests")
class ChangeEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private ChangeEmailUseCase changeEmailUseCase;

    private User testUser;
    private ChangeEmailCommand command;

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
        testUser.activate("Admin approval");
        testUser.clearDomainEvents();

        command = new ChangeEmailCommand(
            testUser.getId(),
            Email.of("newemail@example.com")
        );
    }

    @Test
    @DisplayName("Should change email successfully when new email is unique")
    void shouldChangeEmail() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = changeEmailUseCase.execute(command);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    @DisplayName("Should throw when email already exists")
    void shouldThrowWhenEmailExists() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

        assertThatThrownBy(() -> changeEmailUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email already exists");

        verify(userRepository, times(1)).existsByEmail(any(Email.class));
        verify(userRepository, never()).findById(any(UserId.class));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> changeEmailUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }
}
