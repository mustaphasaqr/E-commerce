package com.mustapha.ecommerce.user.auth.application.facade;

import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.user.auth.application.command.*;
import com.mustapha.ecommerce.user.auth.application.usecase.*;
import com.mustapha.ecommerce.user.auth.domain.model.valueobject.Credentials;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
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

    /**
     * Auto-login after registration
     * @param userResponse Registered user info
     * @param plainPassword The password used for registration
     * @param ipAddress Client IP address
     * @param userAgent Client user agent
     * @return LoginResponse with tokens and user info
     */
    public LoginResponse loginAfterRegistration(UserResponse userResponse, String plainPassword, String ipAddress, String userAgent) {
        // Find the user by id (to get domain model)
        User user = userRepository.findById(com.mustapha.ecommerce.user.domain.model.valueobject.UserId.of(java.util.UUID.fromString(userResponse.getId())))
            .orElseThrow(() -> new IllegalArgumentException("User not found after registration"));

        // Create refresh token
        com.mustapha.ecommerce.user.auth.domain.model.RefreshToken refreshToken = com.mustapha.ecommerce.user.auth.domain.model.RefreshToken.create(user.getId().getValue().toString());
        refreshTokenRepository.save(refreshToken);

        // Create login session
        com.mustapha.ecommerce.user.auth.domain.model.LoginSession session = com.mustapha.ecommerce.user.auth.domain.model.LoginSession.create(
            user.getId().getValue().toString(),
            ipAddress,
            userAgent
        );
        loginSessionRepository.save(session);

        // Generate JWT access token
        String accessToken = jwtTokenGenerator.generateAccessToken(
            user.getId().getValue().toString(),
            user.getRole().name(),
            session.getSessionId()
        );

        return new LoginResponse(
            accessToken,
            refreshToken.getTokenValue(),
            session.getSessionId(),
            3600000, // 1 hour in ms
            userResponse
        );
    }

    private final LoginUseCase loginUseCase;
    private final LogoutUseCase logoutUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final PasswordResetRequestUseCase passwordResetRequestUseCase;
    private final PasswordResetCompleteUseCase passwordResetCompleteUseCase;
    private final LogoutAllDevicesUseCase logoutAllDevicesUseCase;
    private final RequestEmailVerificationUseCase requestEmailVerificationUseCase;
    private final VerifyEmailWithTokenUseCase verifyEmailWithTokenUseCase;
    private final PasswordHasher passwordHasher;
    private final JwtTokenGenerator jwtTokenGenerator;
    private final UserRepository userRepository;
    private final com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository refreshTokenRepository;
    private final com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository loginSessionRepository;

    public AuthFacade(LoginUseCase loginUseCase,
                     LogoutUseCase logoutUseCase,
                     RefreshTokenUseCase refreshTokenUseCase,
                     PasswordResetRequestUseCase passwordResetRequestUseCase,
                     PasswordResetCompleteUseCase passwordResetCompleteUseCase,
                     LogoutAllDevicesUseCase logoutAllDevicesUseCase,
                     RequestEmailVerificationUseCase requestEmailVerificationUseCase,
                     VerifyEmailWithTokenUseCase verifyEmailWithTokenUseCase,
                     PasswordHasher passwordHasher,
                     JwtTokenGenerator jwtTokenGenerator,
                     UserRepository userRepository,
                     com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository refreshTokenRepository,
                     com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository loginSessionRepository) {
        this.loginUseCase = loginUseCase;
        this.logoutUseCase = logoutUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
        this.passwordResetRequestUseCase = passwordResetRequestUseCase;
        this.passwordResetCompleteUseCase = passwordResetCompleteUseCase;
        this.logoutAllDevicesUseCase = logoutAllDevicesUseCase;
        this.requestEmailVerificationUseCase = requestEmailVerificationUseCase;
        this.verifyEmailWithTokenUseCase = verifyEmailWithTokenUseCase;
        this.passwordHasher = passwordHasher;
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
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
        
        // Generate JWT access token with sessionId
        String accessToken = jwtTokenGenerator.generateAccessToken(
            result.getUser().getId().getValue().toString(),
            result.getUser().getRole().name(),
            result.getSessionId()
        );
        
        return new LoginResponse(
            accessToken,
            result.getRefreshToken(),
            result.getSessionId(),
            3600000, // 1 hour in milliseconds
            userResponse
        );
    }

    /**
     * Logout
     */
    public void logout(String userId, String sessionId, String token) {
        LogoutCommand command = new LogoutCommand(
            UserId.of(UUID.fromString(userId)),
            sessionId,
            token
        );
        logoutUseCase.execute(command);
    }

    /**
     * Refresh Token
     */
    public TokenResponse refreshToken(RefreshTokenRequest request, 
                                      String ipAddress, String userAgent) {
        // Get userId from the refresh token itself
        com.mustapha.ecommerce.user.auth.domain.model.RefreshToken existingToken = 
            refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        
        String userId = existingToken.getUserId();
        
        RefreshTokenCommand command = new RefreshTokenCommand(
            UserId.of(UUID.fromString(userId)),
            request.getRefreshToken(),
            ipAddress,
            userAgent
        );
        
        RefreshTokenUseCase.RefreshResult result = refreshTokenUseCase.execute(command);
        
        // Fetch current user role (may have changed since token issued)
        User user = userRepository.findById(UserId.of(UUID.fromString(userId)))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Generate new JWT access token with current role and new sessionId
        String accessToken = jwtTokenGenerator.generateAccessToken(
            userId,
            user.getRole().name(),
            result.getSessionId()
        );
        
        return new TokenResponse(
            accessToken,
            result.getRefreshToken(),
            3600000 // 1 hour in milliseconds
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

    public void requestEmailVerification(RequestEmailVerificationRequest request) {
        RequestEmailVerificationCommand command = new RequestEmailVerificationCommand(
            Email.of(request.getEmail())
        );
        requestEmailVerificationUseCase.execute(command);
    }

    public void verifyEmailWithToken(VerifyEmailWithTokenRequest request) {
        VerifyEmailWithTokenCommand command = new VerifyEmailWithTokenCommand(request.getToken());
        verifyEmailWithTokenUseCase.execute(command);
    }
}
