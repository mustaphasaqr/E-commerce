package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.BlockUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BlockUserUseCase Tests")
class BlockUserUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private BlockUserUseCase blockUserUseCase;

    private User testUser;
    private BlockUserCommand command;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
        testUser.clearDomainEvents();

        command = new BlockUserCommand(
            testUser.getId(),
            "Suspicious activity detected"
        );
    }

    @Nested
    @DisplayName("Successful Blocking")
    class SuccessfulBlockingTests {

        @Test
        @DisplayName("Should block user successfully")
        void shouldBlockUser() {
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = blockUserUseCase.execute(command);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testUser.getId());

            verify(userRepository, times(1)).findById(any(UserId.class));
            verify(userRepository, times(1)).save(any(User.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
        }

        @Test
        @DisplayName("Should publish domain events and clear them")
        void shouldPublishAndClearEvents() {
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = blockUserUseCase.execute(command);

            assertThat(result.getDomainEvents()).isEmpty();
            verify(eventPublisher, atLeastOnce()).publish(any());
        }
    }

    @Nested
    @DisplayName("Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> blockUserUseCase.execute(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

            verify(userRepository, times(1)).findById(any(UserId.class));
            verify(userRepository, never()).save(any(User.class));
            verify(eventPublisher, never()).publish(any());
        }
    }
}
