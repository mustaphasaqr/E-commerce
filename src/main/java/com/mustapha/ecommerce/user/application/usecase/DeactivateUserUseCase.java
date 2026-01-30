package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.DeactivateUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeactivateUserUseCase {
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public DeactivateUserUseCase(UserRepository userRepository, @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(DeactivateUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.deactivate(command.getReason());
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        return savedUser;
    }
}
