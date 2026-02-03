package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.RegisterUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.application.port.EmailService;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterUserUseCase Tests")
class RegisterUserUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private DomainEventPublisher eventPublisher;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private RegisterUserUseCase registerUserUseCase;

    private RegisterUserCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = new RegisterUserCommand(
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Username.of("johndoe"),
            Role.CUSTOMER,
            true
        );
    }

    @Nested
    @DisplayName("Successful Registration")
    class SuccessfulRegistrationTests {

        @Test
        @DisplayName("Should register user when email and username are unique")
        void shouldRegisterUser() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = registerUserUseCase.execute(validCommand);

            assertThat(result).isNotNull();
            assertThat(result.getUsername().getValue()).isEqualTo("johndoe");
            assertThat(result.getEmail().getValue()).isEqualTo("john@example.com");
            assertThat(result.getRole()).isEqualTo(Role.CUSTOMER);

            verify(userRepository, times(1)).existsByEmail(any(Email.class));
            verify(userRepository, times(1)).existsByUsername(any(Username.class));
            verify(userRepository, times(1)).save(any(User.class));
            verify(eventPublisher, atLeastOnce()).publish(any());
            verify(emailService, times(1)).sendWelcomeEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("Should accept terms when termsAccepted is true")
        void shouldAcceptTerms() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = registerUserUseCase.execute(validCommand);

            assertThat(result).isNotNull();
            verify(eventPublisher, atLeastOnce()).publish(any());
        }
    }

    @Nested
    @DisplayName("Validation Failures")
    class ValidationFailureTests {

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(true);

            assertThatThrownBy(() -> registerUserUseCase.execute(validCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email")
                .hasMessageContaining("already exists");

            verify(userRepository, times(1)).existsByEmail(any(Email.class));
            verify(userRepository, never()).existsByUsername(any(Username.class));
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
        }

        @Test
        @DisplayName("Should throw exception when username already exists")
        void shouldThrowWhenUsernameExists() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(userRepository.existsByUsername(any(Username.class))).thenReturn(true);

            assertThatThrownBy(() -> registerUserUseCase.execute(validCommand))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username")
                .hasMessageContaining("already exists");

            verify(userRepository, times(1)).existsByEmail(any(Email.class));
            verify(userRepository, times(1)).existsByUsername(any(Username.class));
            verify(userRepository, never()).save(any(User.class));
            verify(emailService, never()).sendWelcomeEmail(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("Event Publishing")
    class EventPublishingTests {

        @Test
        @DisplayName("Should publish domain events and clear them after save")
        void shouldPublishAndClearEvents() {
            when(userRepository.existsByEmail(any(Email.class))).thenReturn(false);
            when(userRepository.existsByUsername(any(Username.class))).thenReturn(false);
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = registerUserUseCase.execute(validCommand);

            assertThat(result.getDomainEvents()).isEmpty();
            verify(eventPublisher, atLeastOnce()).publish(any());
        }
    }
}
