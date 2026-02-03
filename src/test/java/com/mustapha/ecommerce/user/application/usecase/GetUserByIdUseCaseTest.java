package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.GetUserByIdQuery;
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
@DisplayName("GetUserByIdUseCase Tests")
class GetUserByIdUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GetUserByIdUseCase getUserByIdUseCase;

    private User testUser;
    private GetUserByIdQuery query;

    @BeforeEach
    void setUp() {
        testUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );

        query = new GetUserByIdQuery(testUser.getId());
    }

    @Test
    @DisplayName("Should get user by ID successfully")
    void shouldGetUserById() {
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.of(testUser));

        User result = getUserByIdUseCase.execute(query);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testUser.getId());
        verify(userRepository, times(1)).findById(any(UserId.class));
    }

    @Test
    @DisplayName("Should throw when user not found")
    void shouldThrowWhenUserNotFound() {
        when(userRepository.findById(any(UserId.class))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> getUserByIdUseCase.execute(query))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User not found");

        verify(userRepository, times(1)).findById(any(UserId.class));
    }
}
