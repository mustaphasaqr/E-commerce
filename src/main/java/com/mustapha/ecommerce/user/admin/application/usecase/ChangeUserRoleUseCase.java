package com.mustapha.ecommerce.user.admin.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.admin.application.command.ChangeUserRoleCommand;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ChangeUserRoleUseCase {
    
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public ChangeUserRoleUseCase(UserRepository userRepository, 
                                 @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public User execute(ChangeUserRoleCommand command) {
        User user = userRepository.findById(UserId.of(UUID.fromString(command.getUserId())))
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.getUserId()));

        user.changeRole(command.getNewRole(), command.getChangedBy());
        
        User savedUser = userRepository.save(user);
        
        user.getDomainEvents().forEach(eventPublisher::publish);
        user.clearDomainEvents();
        
        return savedUser;
    }
}
