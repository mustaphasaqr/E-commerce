package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.application.command.ChangePasswordCommand;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ChangePassword Use Case Tests")
class ChangePasswordUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordHasher passwordHasher;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private TokenBlacklistService tokenBlacklistService;

    private ChangePasswordUseCase changePasswordUseCase;

    private User testUser;
    private ChangePasswordCommand command;

    @BeforeEach
    void setUp() {
        changePasswordUseCase = new ChangePasswordUseCase(userRepository, passwordHasher, eventPublisher, tokenBlacklistService);
        
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
        testUser.clearDomainEvents();

        command = new ChangePasswordCommand(
            testUser.getId(),
            "currentPassword123",
            "newPassword456!"
        );
    }

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePassword() {
        when(passwordHasher.matches(eq("currentPassword123"), anyString())).thenReturn(true);
        when(passwordHasher.hash(anyString())).thenReturn("$2a$10$newhashedpassword");
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = changePasswordUseCase.execute(command);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> changePasswordUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }
}
