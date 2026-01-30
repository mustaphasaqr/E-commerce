package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.RefreshTokenCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.model.RefreshToken;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import com.mustapha.ecommerce.user.auth.domain.repository.RefreshTokenRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Refresh Token Use Case
 * Responsibility: Token rotation (security best practice)
 * 
 * Flow:
 * 1. Validate old refresh token
 * 2. Mark old token as used
 * 3. Create new refresh token
 * 4. Create new login session
 * 5. Publish events
 */
@Component
public class RefreshTokenUseCase {
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final DomainEventPublisher eventPublisher;

    public RefreshTokenUseCase(RefreshTokenRepository refreshTokenRepository,
                              LoginSessionRepository loginSessionRepository,
                              @Qualifier("authDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginSessionRepository = loginSessionRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public RefreshResult execute(RefreshTokenCommand command) {
        RefreshToken oldToken = refreshTokenRepository.findByToken(command.getRefreshToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));
        
        oldToken.use(); // Validates and marks as used
        refreshTokenRepository.save(oldToken);
        
        RefreshToken newToken = RefreshToken.create(command.getUserId().getValue().toString());
        refreshTokenRepository.save(newToken);
        
        LoginSession newSession = LoginSession.create(
            command.getUserId().getValue().toString(),
            command.getIpAddress(),
            command.getUserAgent()
        );
        loginSessionRepository.save(newSession);
        
        // Publish events (RefreshToken and LoginSession don't store events)
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.RefreshTokenUsedEvent(
            oldToken.getTokenValue(),
            oldToken.getUserId(),
            newSession.getSessionId()
        ));
        
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.RefreshTokenCreatedEvent(
            newToken.getTokenValue(),
            newToken.getUserId(),
            newToken.getExpiresAt()
        ));
        
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.SessionCreatedEvent(
            newSession.getSessionId(),
            newSession.getUserId(),
            newSession.getIpAddress(),
            newSession.getUserAgent(),
            newSession.getExpiresAt()
        ));
        
        return new RefreshResult(newToken.getTokenValue(), newSession.getSessionId());
    }
    
    public static class RefreshResult {
        private final String refreshToken;
        private final String sessionId;
        
        public RefreshResult(String refreshToken, String sessionId) {
            this.refreshToken = refreshToken;
            this.sessionId = sessionId;
        }
        
        public String getRefreshToken() {
            return refreshToken;
        }
        
        public String getSessionId() {
            return sessionId;
        }
    }
}
