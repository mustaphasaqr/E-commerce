package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.RevokeMarketingConsentCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class RevokeMarketingConsentUseCase {
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public RevokeMarketingConsentUseCase(UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(RevokeMarketingConsentCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.revokeMarketingConsent();
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        return savedUser;
    }
}
