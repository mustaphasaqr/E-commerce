package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.ActivateUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Activate User Use Case
 * Responsibility: Orchestrate user activation
 * Pattern: Use Case (Application Service)
 */
@Component
public class ActivateUserUseCase {
    
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public ActivateUserUseCase(UserRepository userRepository, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(ActivateUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.activate(command.getActivationNote());
        
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        
        return savedUser;
    }
}
