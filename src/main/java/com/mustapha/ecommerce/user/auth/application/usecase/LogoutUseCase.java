package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LogoutCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogoutUseCase {
    private final LoginSessionRepository loginSessionRepository;
    private final DomainEventPublisher eventPublisher;
    private final TokenBlacklistService tokenBlacklistService;
    private final long jwtExpirationMs;

    public LogoutUseCase(LoginSessionRepository loginSessionRepository, 
                        @Qualifier("authDomainEventPublisherAdapter") DomainEventPublisher eventPublisher,
                        TokenBlacklistService tokenBlacklistService,
                        @Value("${jwt.expiration-ms}") long jwtExpirationMs) {
        this.loginSessionRepository = loginSessionRepository;
        this.eventPublisher = eventPublisher;
        this.tokenBlacklistService = tokenBlacklistService;
        this.jwtExpirationMs = jwtExpirationMs;
    }
    
    @Transactional
    public void execute(LogoutCommand command) {
        LoginSession session = loginSessionRepository.findBySessionId(command.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        
        // Blacklist the JWT token to prevent reuse
        if (command.getToken() != null) {
            tokenBlacklistService.blacklistToken(command.getToken(), jwtExpirationMs);
        }
        
        session.invalidate();
        loginSessionRepository.save(session);
        
        // Publish event (LoginSession doesn't store events)
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.UserLoggedOutEvent(
            command.getUserId().getValue().toString(),
            command.getSessionId()
        ));
    }
}
