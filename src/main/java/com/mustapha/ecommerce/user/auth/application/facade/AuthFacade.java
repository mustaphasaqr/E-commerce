package com.mustapha.ecommerce.user.auth.application.facade;

import com.mustapha.ecommerce.user.auth.application.command.*;
import com.mustapha.ecommerce.user.auth.application.usecase.*;
import com.mustapha.ecommerce.user.auth.domain.model.valueobject.Credentials;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.dto.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Auth Facade - Translation Layer for Authentication
 * 
 * Responsibilities:
 * 1. Accept API DTOs (LoginRequest, etc.)
 * 2. Convert primitives → value objects → Commands
 * 3. Delegate to Auth Use Cases
 * 4. Convert results → API DTOs (LoginResponse, TokenResponse)
 * 
 * Pattern: Facade, Anti-Corruption Layer
 */
@Service
public class AuthFacade {

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final PasswordResetRequestUseCase passwordResetRequestUseCase;
    private final PasswordResetCompleteUseCase passwordResetCompleteUseCase;
    private final LogoutAllDevicesUseCase logoutAllDevicesUseCase;
    private final PasswordHasher passwordHasher;

    public AuthFacade(LoginUseCase loginUseCase,
                     LogoutUseCase logoutUseCase,
                     RefreshTokenUseCase refreshTokenUseCase,
                     PasswordResetRequestUseCase passwordResetRequestUseCase,
                     PasswordResetCompleteUseCase passwordResetCompleteUseCase,
                     LogoutAllDevicesUseCase logoutAllDevicesUseCase,
                     PasswordHasher passwordHasher) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.passwordResetRequestUseCase = passwordResetRequestUseCase;
        this.passwordResetCompleteUseCase = passwordResetCompleteUseCase;
        this.logoutAllDevicesUseCase = logoutAllDevicesUseCase;
        this.passwordHasher = passwordHasher;
    }

    /**
     * Login
     * 
     * @param request Login credentials
     * @param ipAddress Client IP address (from HTTP request)
     * @param userAgent Client user agent (from HTTP headers)
     * @return LoginResponse with tokens and user info
     */
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        LoginCommand command = new LoginCommand(
            Credentials.of(request.getEmail(), request.getPassword()),
            ipAddress,
            userAgent
        );
        
        LoginUseCase.LoginResult result = loginUseCase.execute(command);
        
        // Build response
        UserResponse userResponse = UserResponse.fromDomain(result.getUser());
        
        return new LoginResponse(
            "JWT_ACCESS_TOKEN_PLACEHOLDER", // TODO: Generate JWT in infrastructure
            result.getRefreshToken(),
            result.getSessionId(),
            3600, // 1 hour
            userResponse
        );
    }

    /**
     * Logout
     */
    public void logout(String userId, String sessionId) {
        LogoutCommand command = new LogoutCommand(
            UserId.of(UUID.fromString(userId)),
            sessionId
        );
        logoutUseCase.execute(command);
    }

    /**
     * Refresh Token
     */
    public TokenResponse refreshToken(String userId, RefreshTokenRequest request, 
                                      String ipAddress, String userAgent) {
        RefreshTokenCommand command = new RefreshTokenCommand(
            UserId.of(UUID.fromString(userId)),
            request.getRefreshToken(),
            ipAddress,
            userAgent
        );
        
        RefreshTokenUseCase.RefreshResult result = refreshTokenUseCase.execute(command);
        
        return new TokenResponse(
            "JWT_ACCESS_TOKEN_PLACEHOLDER", // TODO: Generate JWT
            result.getRefreshToken(),
            3600 // 1 hour
        );
    }

    /**
     * Password Reset Request
     */
    public void requestPasswordReset(PasswordResetRequestRequest request) {
        PasswordResetRequestCommand command = new PasswordResetRequestCommand(
            Email.of(request.getEmail())
        );
        passwordResetRequestUseCase.execute(command);
    }

    /**
     * Password Reset Complete
     */
    public void completePasswordReset(PasswordResetCompleteRequest request) {
        PasswordResetCompleteCommand command = new PasswordResetCompleteCommand(
            request.getToken(),
            Password.fromPlainText(request.getNewPassword(), passwordHasher)
        );
        passwordResetCompleteUseCase.execute(command);
    }

    /**
     * Logout All Devices (except current)
     */
    public void logoutAllDevices(String userId, String currentSessionId) {
        LogoutAllDevicesCommand command = new LogoutAllDevicesCommand(
            UserId.of(UUID.fromString(userId)),
            currentSessionId
        );
        logoutAllDevicesUseCase.execute(command);
    }
}
