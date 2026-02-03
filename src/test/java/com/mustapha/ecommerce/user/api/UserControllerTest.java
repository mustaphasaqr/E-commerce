package com.mustapha.ecommerce.user.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.application.facade.UserFacade;
import com.mustapha.ecommerce.user.dto.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser(username = "testuser", roles = {"EMPLOYEE"})
@DisplayName("UserController REST API Tests")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserFacade userFacade;

    @MockBean
    private JwtTokenGenerator jwtTokenGenerator;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    @SuppressWarnings("rawtypes")
    private RedisTemplate redisTemplate;

    private UserResponse mockUserResponse;

    private Authentication createAuthentication(String userId, String role) {
        return new UsernamePasswordAuthenticationToken(
            userId,
            null,
            List.of(new SimpleGrantedAuthority("ROLE_" + role))
        );
    }

    @BeforeEach
    void setUp() {
        // Mock RedisTemplate operations to prevent NullPointerException
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        
        mockUserResponse = new UserResponse();
        mockUserResponse.setId("USER-123");
        mockUserResponse.setUsername("johndoe");
        mockUserResponse.setEmail("john@example.com");
        mockUserResponse.setRole("CUSTOMER");
        mockUserResponse.setEmailVerified(true);
    }

    @Nested
    @DisplayName("GET /api/users/{userId} - Get User")
    class GetUserTests {

        @Test
        @DisplayName("Should return user details with 200 OK")
        void shouldGetUserSuccessfully() throws Exception {
            when(userFacade.getUserById("USER-123")).thenReturn(mockUserResponse);

            mockMvc.perform(get("/api/users/{userId}", "USER-123")
                    .with(authentication(createAuthentication("USER-123", "CUSTOMER")))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("USER-123"))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.emailVerified").value(true));

            verify(userFacade, times(1)).getUserById("USER-123");
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userFacade.getUserById("USER-999"))
                .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(get("/api/users/{userId}", "USER-999")
                    .with(authentication(createAuthentication("USER-999", "CUSTOMER")))
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/users/username/{username} - Get User by Username")
    class GetUserByUsernameTests {

        @Test
        @DisplayName("Should return user details by username")
        void shouldGetUserByUsernameSuccessfully() throws Exception {
            when(userFacade.getUserByUsername("johndoe")).thenReturn(mockUserResponse);

            mockMvc.perform(get("/api/users/username/{username}", "johndoe")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.id").value("USER-123"));

            verify(userFacade, times(1)).getUserByUsername("johndoe");
        }

        @Test
        @DisplayName("Should return 404 when username not found")
        void shouldReturn404WhenUsernameNotFound() throws Exception {
            when(userFacade.getUserByUsername("unknown"))
                .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(get("/api/users/username/{username}", "unknown")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/users/me/email - Change Email")
    class ChangeEmailTests {

        @Test
        @DisplayName("Should change email successfully")
        void shouldChangeEmailSuccessfully() throws Exception {
            String changeEmailRequest = """
                {
                    "newEmail": "john.updated@example.com"
                }
                """;

            UserResponse updatedResponse = new UserResponse();
            updatedResponse.setId("USER-123");
            updatedResponse.setUsername("johndoe");
            updatedResponse.setEmail("john.updated@example.com");
            updatedResponse.setRole("CUSTOMER");
            updatedResponse.setEmailVerified(false);
            
            when(userFacade.changeEmail(any(), any()))
                .thenReturn(updatedResponse);

            mockMvc.perform(put("/api/users/me/email")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(changeEmailRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
        }

        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestIsInvalid() throws Exception {
            mockMvc.perform(put("/api/users/me/email")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{invalid json}"))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/users/{id}/deactivate - Deactivate User")
    class DeactivateUserTests {

        @Test
        @DisplayName("Should deactivate user successfully")
        void shouldDeactivateUserSuccessfully() throws Exception {
            when(userFacade.deactivateUser("USER-123")).thenReturn(mockUserResponse);

            mockMvc.perform(post("/api/users/{id}/deactivate", "USER-123")
                    .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("USER-123"));

            verify(userFacade, times(1)).deactivateUser("USER-123");
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            when(userFacade.deactivateUser("USER-999"))
                .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(post("/api/users/{id}/deactivate", "USER-999")
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }
    }
}
