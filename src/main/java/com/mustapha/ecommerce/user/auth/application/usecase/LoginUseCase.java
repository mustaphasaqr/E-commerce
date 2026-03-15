package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LoginCommand;
import com.mustapha.ecommerce.user.auth.domain.exception.InvalidCredentialsException;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.auth.domain.service.AccountLockoutService;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login Use Case
 * Responsibility: Orchestrate authentication flow
 * Pattern: Use Case (Application Service)
 * 
 * Enhanced Security Flow:
 * 1. Check rate limiting (IP + user level)
 * 2. Check account lockout (after 5 failed attempts)
 * 3. Find user by email/username
 * 4. Verify password
 * 5. Reset lockout counter on success
 * 6. Create refresh token & session
 * 7. Publish events
 */
@Component
public class LoginUseCase {
    private static final Logger logger = LoggerFactory.getLogger(LoginUseCase.class);
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final LoginRateLimitPolicy rateLimitPolicy;
    private final AccountLockoutService accountLockoutService;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher eventPublisher;

    public LoginUseCase(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       LoginSessionRepository loginSessionRepository,
                       LoginRateLimitPolicy rateLimitPolicy,
                       AccountLockoutService accountLockoutService,
                       PasswordHasher passwordHasher,
                       @Qualifier("authDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.rateLimitPolicy = rateLimitPolicy;
        this.accountLockoutService = accountLockoutService;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public LoginResult execute(LoginCommand command) {
        String identifier = command.getCredentials().getIdentifier();
        
        // Step 1: Check rate limiting (IP-based)
        rateLimitPolicy.checkUserRateLimit(identifier).throwIfDenied();
        rateLimitPolicy.checkIpRateLimit(command.getIpAddress()).throwIfDenied();
        
        // Step 2: Check account lockout (account-based)
        // Throws AccountLockedException if locked
        accountLockoutService.checkAccountNotLocked(identifier);
        
        // Step 3: Find user (try email first, then username)
        User user = null;
        try {
            user = userRepository.findByEmail(Email.of(identifier)).orElse(null);
        } catch (IllegalArgumentException e) {
            // Not a valid email, try username
        }
        
        if (user == null) {
            try {
                user = userRepository.findByUsername(Username.of(identifier)).orElse(null);
            } catch (IllegalArgumentException e) {
                // Not a valid username either
            }
        }
        
        if (user == null) {
            // Record failed attempt (both rate limiting and account lockout)
            rateLimitPolicy.recordFailedAttempt(identifier, command.getIpAddress());
            accountLockoutService.recordFailedAttempt(identifier);
            throw new InvalidCredentialsException(identifier);
        }
        
        // Step 4: Verify password
        if (!user.verifyPassword(command.getCredentials().getPlainPassword(), passwordHasher)) {
            // Record failed attempt (both systems)
            rateLimitPolicy.recordFailedAttempt(identifier, command.getIpAddress());
            boolean accountLocked = accountLockoutService.recordFailedAttempt(identifier);
            
            // If account just got locked, throw more specific exception
            if (accountLocked) {
                accountLockoutService.checkAccountNotLocked(identifier); // Will throw AccountLockedException
            }
            
            throw new InvalidCredentialsException(identifier);
        }
        
        // Step 5: Successful login - reset counters
        rateLimitPolicy.recordSuccessfulLogin(user.getId().getValue().toString(), command.getIpAddress());
        accountLockoutService.resetFailedAttempts(identifier); // ⬅️ Reset lockout counter
        
        // Step 6: Create refresh token (30-day validity)
        RefreshToken refreshToken = RefreshToken.create(user.getId().getValue().toString());
        try {
            refreshTokenRepository.save(refreshToken);
        } catch (Exception e) {
            // Degrade gracefully so login does not fail when auth storage is temporarily unavailable.
            logger.error("Failed to persist refresh token for userId={}. Login continues with access token only. Cause: {}",
                user.getId().getValue(), e.getMessage());
        }
        
        // Step 5: Create login session (24-hour validity)
        LoginSession session = LoginSession.create(
            user.getId().getValue().toString(),
            command.getIpAddress(),
            command.getUserAgent()
        );
        try {
            loginSessionRepository.save(session);
        } catch (Exception e) {
            // Degrade gracefully so login does not fail when auth storage is temporarily unavailable.
            logger.error("Failed to persist login session for userId={}. Login continues with stateless access token. Cause: {}",
                user.getId().getValue(), e.getMessage());
        }
        
        // Step 6: Publish events (auth aggregates don't store events, publish directly)
        try {
            eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.RefreshTokenCreatedEvent(
                refreshToken.getTokenValue(),
                refreshToken.getUserId(),
                refreshToken.getExpiresAt()
            ));

            eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.SessionCreatedEvent(
                session.getSessionId(),
                session.getUserId(),
                session.getIpAddress(),
                session.getUserAgent(),
                session.getExpiresAt()
            ));

            eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.UserLoggedInEvent(
                user.getId().getValue().toString(),
                session.getSessionId(),
                command.getIpAddress(),
                command.getUserAgent()
            ));
        } catch (Exception e) {
            logger.warn("Failed to publish one or more login events for userId={}. Cause: {}",
                user.getId().getValue(), e.getMessage());
        }
        
        return new LoginResult(user, refreshToken.getTokenValue(), session.getSessionId());
    }
    
    /**
     * Login Result Value Object
     * Contains authenticated user, refresh token, and session ID
     */
    public static class LoginResult {
        private final User user;
        private final String refreshToken;
        private final String sessionId;
        
        public LoginResult(User user, String refreshToken, String sessionId) {
            this.user = user;
            this.refreshToken = refreshToken;
            this.sessionId = sessionId;
        }
        
        public User getUser() {
            return user;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
}
