package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.DeleteUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.exception.InvalidUserStateException;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DeleteUserUseCase {
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;

    public DeleteUserUseCase(UserRepository userRepository, @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(DeleteUserCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole().isOwner()) {
            throw new InvalidUserStateException("Owner accounts cannot be deleted");
        }

        if (command.getRequestedByUserId() != null && command.getRequestedByUserId().equals(user.getId())) {
            throw new InvalidUserStateException("You cannot delete your own account");
        }

        user.delete(command.getReason());
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        return savedUser;
    }
}
