package com.mustapha.ecommerce.user.admin.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.admin.application.facade.AdminFacade;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@WithMockUser(username = "admin", roles = {"OWNER"})
@DisplayName("AdminController REST API Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AdminFacade adminFacade;

    @MockBean
    private JwtTokenGenerator jwtTokenGenerator;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    @SuppressWarnings("rawtypes")
    private RedisTemplate redisTemplate;

    private User mockUser;

    @BeforeEach
    void setUp() {
        // Mock RedisTemplate operations to prevent NullPointerException
        when(redisTemplate.opsForValue()).thenReturn(mock(org.springframework.data.redis.core.ValueOperations.class));
        
        mockUser = User.create(
            Username.of("johndoe"),
            Email.of("john@example.com"),
            Password.fromHashed("$2a$10$hashedpassword"),
            Role.CUSTOMER
        );
    }

    @Nested
    @DisplayName("GET /api/admin/users - List All Users")
    class ListUsersTests {

        @Test
        @DisplayName("Should return paginated list of users")
        void shouldReturnPaginatedListOfUsers() throws Exception {
            User user2 = User.create(
                Username.of("janedoe"),
                Email.of("jane@example.com"),
                Password.fromHashed("$2a$10$hashedpassword"),
                Role.EMPLOYEE
            );

            Page<User> userPage = new PageImpl<>(Arrays.asList(mockUser, user2), PageRequest.of(0, 20), 2);
            when(adminFacade.getAllUsers(any(PageRequest.class))).thenReturn(userPage);

            mockMvc.perform(get("/api/v1/admin/users")
                    .param("page", "0")
                    .param("size", "20")
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].username").value("johndoe"))
                .andExpect(jsonPath("$.users[1].username").value("janedoe"))
                .andExpect(jsonPath("$.totalElements").value(2));

            verify(adminFacade, times(1)).getAllUsers(any(PageRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/{id}/role - Change User Role")
    class ChangeRoleTests {

        @Test
        @DisplayName("Should change user role successfully")
        void shouldChangeUserRoleSuccessfully() throws Exception {
            String requestBody = """
                {
                    "newRole": "EMPLOYEE"
                }
                """;

            when(adminFacade.changeUserRole(anyString(), any(Role.class), anyString()))
                .thenReturn(mockUser);

            mockMvc.perform(post("/api/v1/admin/users/{id}/role", mockUser.getId().toString())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));

            verify(adminFacade, times(1)).changeUserRole(anyString(), any(Role.class), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/{id}/block - Block User")
    class BlockUserTests {

        @Test
        @DisplayName("Should block user successfully")
        void shouldBlockUserSuccessfully() throws Exception {
            String requestBody = """
                {
                    "reason": "Violating terms of service"
                }
                """;

            when(adminFacade.blockUser(anyString(), anyString())).thenReturn(mockUser);

            mockMvc.perform(post("/api/v1/admin/users/{id}/block", mockUser.getId().toString())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

            verify(adminFacade, times(1)).blockUser(anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            String requestBody = """
                {
                    "reason": "Test"
                }
                """;

            when(adminFacade.blockUser(anyString(), anyString()))
                .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(post("/api/v1/admin/users/{id}/block", "USER-999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/{id}/unblock - Unblock User")
    class UnblockUserTests {

        @Test
        @DisplayName("Should unblock user successfully")
        void shouldUnblockUserSuccessfully() throws Exception {
            String requestBody = """
                {
                    "reason": "Appeal accepted"
                }
                """;

            when(adminFacade.unblockUser(anyString(), anyString())).thenReturn(mockUser);

            mockMvc.perform(post("/api/v1/admin/users/{id}/unblock", mockUser.getId().toString())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

            verify(adminFacade, times(1)).unblockUser(anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/users/{id} - Delete User")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully")
        void shouldDeleteUserSuccessfully() throws Exception {
            String requestBody = """
                {
                    "reason": "GDPR request"
                }
                """;

            when(adminFacade.deleteUser(anyString(), anyString(), anyString())).thenReturn(mockUser);

            mockMvc.perform(delete("/api/v1/admin/users/{id}", mockUser.getId().toString())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("johndoe"));

            verify(adminFacade, times(1)).deleteUser(anyString(), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        void shouldReturn404WhenUserNotFound() throws Exception {
            String requestBody = """
                {
                    "reason": "Test"
                }
                """;

            when(adminFacade.deleteUser(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("User not found"));

            mockMvc.perform(delete("/api/v1/admin/users/{id}", "USER-999")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("POST /api/admin/users/search - Search Users")
    class SearchUsersTests {

        @Test
        @DisplayName("Should search users with criteria")
        void shouldSearchUsersWithCriteria() throws Exception {
            String requestBody = """
                {
                    "username": "john",
                    "email": null,
                    "status": null,
                    "role": null,
                    "page": 0,
                    "size": 20
                }
                """;

            Page<User> searchResults = new PageImpl<>(Arrays.asList(mockUser), PageRequest.of(0, 20), 1);
            when(adminFacade.searchUsers(any(), any(), any(), any(), any(PageRequest.class)))
                .thenReturn(searchResults);

            mockMvc.perform(post("/api/v1/admin/users/search")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users").isArray())
                .andExpect(jsonPath("$.users[0].username").value("johndoe"))
                .andExpect(jsonPath("$.totalElements").value(1));

            verify(adminFacade, times(1)).searchUsers(any(), any(), any(), any(), any(PageRequest.class));
        }
    }
}
