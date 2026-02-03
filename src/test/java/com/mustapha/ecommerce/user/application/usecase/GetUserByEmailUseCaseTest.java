package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByEmailQuery;
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
@DisplayName("GetUserByEmailUseCase Tests")
class GetUserByEmailUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByEmailUseCase getUserByEmailUseCase;

    private User testUser;
    private GetUserByEmailQuery query;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        query = new GetUserByEmailQuery(Email.of("john@example.com"));
    }

    @Test
    @DisplayName("Should get user by email successfully")
    void shouldGetUserByEmail() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(testUser));

        User result = getUserByEmailUseCase.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(testUser.getEmail());
        verify(userRepository, times(1)).findByEmail(any(Email.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUserByEmailUseCase.execute(query))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findByEmail(any(Email.class));
    }
}
