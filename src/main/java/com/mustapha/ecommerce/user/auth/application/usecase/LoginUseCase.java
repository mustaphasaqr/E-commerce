package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LoginCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.policy.LoginRateLimitPolicy;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Email;
import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.domain.model.valueobject.Username;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Login Use Case
 * Responsibility: Orchestrate authentication flow
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Check rate limiting (security)
 * 2. Find user by email/username
 * 3. Verify password
 * 4. Create refresh token
 * 5. Create login session
 * 6. Publish events
 */
@Component
public class LoginUseCase {
    
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final LoginRateLimitPolicy rateLimitPolicy;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher eventPublisher;

    public LoginUseCase(UserRepository userRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       LoginSessionRepository loginSessionRepository,
                       LoginRateLimitPolicy rateLimitPolicy,
                       PasswordHasher passwordHasher,
                       @Qualifier("authDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.rateLimitPolicy = rateLimitPolicy;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public LoginResult execute(LoginCommand command) {
        String identifier = command.getCredentials().getIdentifier();
        
        // Step 1: Check rate limiting
        rateLimitPolicy.checkUserRateLimit(identifier).throwIfDenied();
        rateLimitPolicy.checkIpRateLimit(command.getIpAddress()).throwIfDenied();
        
        // Step 2: Find user (try email first, then username)
        User user = userRepository.findByEmail(Email.of(identifier))
            .or(() -> userRepository.findByUsername(Username.of(identifier)))
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        
        // Step 3: Verify password
        if (!user.verifyPassword(command.getCredentials().getPlainPassword(), passwordHasher)) {
            rateLimitPolicy.recordFailedAttempt(identifier, command.getIpAddress());
            throw new IllegalArgumentException("Invalid credentials");
        }
        
        rateLimitPolicy.recordSuccessfulLogin(user.getId().getValue().toString(), command.getIpAddress());
        
        // Step 4: Create refresh token (30-day validity)
        RefreshToken refreshToken = RefreshToken.create(user.getId().getValue().toString());
        refreshTokenRepository.save(refreshToken);
        
        // Step 5: Create login session (24-hour validity)
        LoginSession session = LoginSession.create(
            user.getId().getValue().toString(),
            command.getIpAddress(),
            command.getUserAgent()
        );
        loginSessionRepository.save(session);
        
        // Step 6: Publish events (auth aggregates don't store events, publish directly)
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
