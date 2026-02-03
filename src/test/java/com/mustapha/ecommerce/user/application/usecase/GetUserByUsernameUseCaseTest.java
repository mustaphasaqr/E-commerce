package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByUsernameQuery;
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
@DisplayName("GetUserByUsernameUseCase Tests")
class GetUserByUsernameUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByUsernameUseCase getUserByUsernameUseCase;

    private User testUser;
    private GetUserByUsernameQuery query;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        query = new GetUserByUsernameQuery(Username.of("johndoe"));
    }

    @Test
    @DisplayName("Should get user by username successfully")
    void shouldGetUserByUsername() {
        when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.of(testUser));

        User result = getUserByUsernameUseCase.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo(testUser.getUsername());
        verify(userRepository, times(1)).findByUsername(any(Username.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUserByUsernameUseCase.execute(query))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findByUsername(any(Username.class));
    }
}
