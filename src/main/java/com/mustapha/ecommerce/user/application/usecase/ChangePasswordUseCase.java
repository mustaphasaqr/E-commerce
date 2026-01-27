package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.ChangePasswordCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.Password;
import com.mustapha.ecommerce.user.domain.model.valueobject.PasswordHasher;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ChangePasswordUseCase {
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final DomainEventPublisher eventPublisher;

    public ChangePasswordUseCase(UserRepository userRepository, PasswordHasher passwordHasher, DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public User execute(ChangePasswordCommand command) {
        User user = userRepository.findById(command.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Change password (domain verifies current password internally)
        user.changePassword(
            command.getCurrentPasswordPlainText(),
            Password.fromPlainText(command.getNewPasswordPlainText(), passwordHasher),
            passwordHasher
        );
        
        User savedUser = userRepository.save(user);
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        return savedUser;
    }
}
