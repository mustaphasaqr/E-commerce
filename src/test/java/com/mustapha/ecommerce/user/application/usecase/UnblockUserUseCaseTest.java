package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.UnblockUserCommand;
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
@DisplayName("UnblockUser Use Case Tests")
class UnblockUserUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private UnblockUserUseCase unblockUserUseCase;

    private User testUser;
    private UnblockUserCommand command;

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
        testUser.block("Security violation");
        testUser.clearDomainEvents();

        command = new UnblockUserCommand(
            testUser.getId(),
            "Investigation completed"
        );
    }

    @Test
    @DisplayName("Should unblock user successfully")
    void shouldUnblockUser() {
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = unblockUserUseCase.execute(command);

        assertThat(result).isNotNull();
        verify(userRepository, times(1)).findById(any(UserId.class));
        verify(userRepository, times(1)).save(any(User.class));
        verify(eventPublisher, atLeastOnce()).publish(any());
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> unblockUserUseCase.execute(command))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, never()).save(any(User.class));
    }
}
