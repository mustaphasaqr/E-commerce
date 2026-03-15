package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.ActivateUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activate User Use Case
 * Responsibility: Orchestrate user activation
 * Pattern: Use Case (Application Service)
 */
@Component
public class ActivateUserUseCase {

    private static final String ADMIN_TERMS_OVERRIDE_VERSION = "ADMIN_OVERRIDE";
    
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public ActivateUserUseCase(UserRepository userRepository, @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(ActivateUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Admin activation path (used from OWNER dashboard) should be able to move
        // PENDING users to ACTIVE even when verification/terms are still pending.
        if (command.getActivationNote() != null && !command.getActivationNote().isBlank()) {
            if (!user.isTermsAccepted()) {
                user.acceptTerms(ADMIN_TERMS_OVERRIDE_VERSION);
            }
            if (!user.isEmailVerified()) {
                user.verifyEmail();
            }
        }
        
        user.activate(command.getActivationNote());
        
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        
        return savedUser;
    }
}
