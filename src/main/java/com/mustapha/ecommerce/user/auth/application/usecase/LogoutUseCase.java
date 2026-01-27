package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.LogoutCommand;
import com.mustapha.ecommerce.user.auth.domain.model.LoginSession;
import com.mustapha.ecommerce.user.auth.domain.repository.LoginSessionRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class LogoutUseCase {
    private final LoginSessionRepository loginSessionRepository;
    private final DomainEventPublisher eventPublisher;

    public LogoutUseCase(LoginSessionRepository loginSessionRepository, DomainEventPublisher eventPublisher) {
        this.loginSessionRepository = loginSessionRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public void execute(LogoutCommand command) {
        LoginSession session = loginSessionRepository.findBySessionId(command.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Session not found"));
        session.invalidate();
        loginSessionRepository.save(session);
        
        // Publish event (LoginSession doesn't store events)
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.UserLoggedOutEvent(
            command.getUserId().getValue().toString(),
            command.getSessionId()
        ));
    }
}
