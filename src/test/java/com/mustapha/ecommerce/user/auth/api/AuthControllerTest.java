package com.mustapha.ecommerce.user.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.shared.security.SecurityConfig;
import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.auth.application.facade.AuthFacade;
import com.mustapha.ecommerce.user.dto.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("AuthController REST API Tests")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthFacade authFacade;

    @MockBean
    private JwtTokenGenerator jwtTokenGenerator;

    @MockBean
    private TokenBlacklistService tokenBlacklistService;

    @MockBean
    @SuppressWarnings("rawtypes")
    private RedisTemplate redisTemplate;

    private UserResponse mockUserResponse;
    private LoginResponse mockLoginResponse;

    @BeforeEach
    void setUp() {
        mockUserResponse = new UserResponse();
        mockUserResponse.setId("USER-123");
        mockUserResponse.setUsername("johndoe");
        mockUserResponse.setEmail("john@example.com");
        mockUserResponse.setRole("CUSTOMER");

        mockLoginResponse = new LoginResponse();
        mockLoginResponse.setAccessToken("jwt-token-12345");
        mockLoginResponse.setRefreshToken("refresh-token-12345");
        mockLoginResponse.setSessionId("session-123");
        mockLoginResponse.setExpiresIn(3600000);
        mockLoginResponse.setUser(mockUserResponse);
    }

    @Nested
    @DisplayName("POST /api/auth/login - Login User")
    class LoginTests {

        @Test
        @DisplayName("Should login user successfully")
        void shouldLoginUserSuccessfully() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("john@example.com");
            request.setPassword("SecurePass123!");

            when(authFacade.login(any(LoginRequest.class), anyString(), anyString()))
                .thenReturn(mockLoginResponse);

            mockMvc.perform(post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .header("X-Forwarded-For", "192.168.1.1")
                    .header("User-Agent", "Mozilla/5.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-token-12345"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-12345"))
                .andExpect(jsonPath("$.sessionId").value("session-123"))
                .andExpect(jsonPath("$.user.id").value("USER-123"))
                .andExpect(jsonPath("$.user.username").value("johndoe"));

            verify(authFacade, times(1)).login(any(LoginRequest.class), anyString(), anyString());
        }

        @Test
        @DisplayName("Should return 401 when credentials are invalid")
        void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
            LoginRequest request = new LoginRequest();
            request.setEmail("john@example.com");
            request.setPassword("WrongPassword");

            when(authFacade.login(any(LoginRequest.class), anyString(), anyString()))
                .thenThrow(new RuntimeException("Invalid credentials"));

            mockMvc.perform(post("/api/v1/auth/login")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout - Logout User")
    @WithMockUser
    class LogoutTests {

        @Test
        @DisplayName("Should logout user successfully")
        void shouldLogoutUserSuccessfully() throws Exception {
            doNothing().when(authFacade).logout(any(), any(), any());

            mockMvc.perform(post("/api/v1/auth/logout")
                    .with(csrf())
                    .header("Authorization", "Bearer mock-jwt-token"))
                .andExpect(status().isNoContent());

            verify(authFacade, times(1)).logout(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/refresh - Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() throws Exception {
            RefreshTokenRequest request = new RefreshTokenRequest();
            request.setRefreshToken("refresh-token-12345");

            TokenResponse tokenResponse = new TokenResponse();
            tokenResponse.setAccessToken("new-jwt-token");
            tokenResponse.setRefreshToken("new-refresh-token");
            tokenResponse.setExpiresIn(3600000);

            when(authFacade.refreshToken(any(RefreshTokenRequest.class), anyString(), anyString()))
                .thenReturn(tokenResponse);

            mockMvc.perform(post("/api/v1/auth/refresh")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-jwt-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

            verify(authFacade, times(1)).refreshToken(any(RefreshTokenRequest.class), anyString(), anyString());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout-all - Logout All Devices")
    @WithMockUser
    class LogoutAllDevicesTests {

        @Test
        @DisplayName("Should logout all devices successfully")
        void shouldLogoutAllDevicesSuccessfully() throws Exception {
            doNothing().when(authFacade).logoutAllDevices(any(), any());

            mockMvc.perform(post("/api/v1/auth/logout-all")
                    .with(csrf()))
                .andExpect(status().isNoContent());

            verify(authFacade, times(1)).logoutAllDevices(any(), any());
        }
    }

    @Nested
    @DisplayName("POST /api/auth/password-reset/request - Request Password Reset")
    class PasswordResetRequestTests {

        @Test
        @DisplayName("Should send password reset email successfully")
        void shouldSendPasswordResetEmailSuccessfully() throws Exception {
            PasswordResetRequestRequest request = new PasswordResetRequestRequest();
            request.setEmail("john@example.com");

            doNothing().when(authFacade).requestPasswordReset(any(PasswordResetRequestRequest.class));

            mockMvc.perform(post("/api/v1/auth/password-reset/request")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            verify(authFacade, times(1)).requestPasswordReset(any(PasswordResetRequestRequest.class));
        }
    }

    @Nested
    @DisplayName("POST /api/auth/password-reset/complete - Complete Password Reset")
    class PasswordResetCompleteTests {

        @Test
        @DisplayName("Should reset password successfully")
        void shouldResetPasswordSuccessfully() throws Exception {
            PasswordResetCompleteRequest request = new PasswordResetCompleteRequest();
            request.setToken("reset-token-123");
            request.setNewPassword("NewSecurePass123!");

            doNothing().when(authFacade).completePasswordReset(any(PasswordResetCompleteRequest.class));

            mockMvc.perform(post("/api/v1/auth/password-reset/complete")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

            verify(authFacade, times(1)).completePasswordReset(any(PasswordResetCompleteRequest.class));
        }

        @Test
        @DisplayName("Should return 400 when token is invalid")
        void shouldReturn400WhenTokenIsInvalid() throws Exception {
            PasswordResetCompleteRequest request = new PasswordResetCompleteRequest();
            request.setToken("invalid-token");
            request.setNewPassword("NewPass123!");

            doThrow(new RuntimeException("Invalid or expired token"))
                .when(authFacade).completePasswordReset(any(PasswordResetCompleteRequest.class));

            mockMvc.perform(post("/api/v1/auth/password-reset/complete")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        }
    }
}
