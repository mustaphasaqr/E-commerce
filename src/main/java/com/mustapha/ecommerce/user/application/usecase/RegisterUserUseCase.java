package com.mustapha.ecommerce.user.application.usecase;

import com.mustapha.ecommerce.user.application.command.RegisterUserCommand;
import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.application.port.EmailService;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Register User Use Case
 * Responsibility: Orchestrate user registration (NOT business rules)
 * Pattern: Use Case (Application Service)
 * 
 * Clean 3-Step Pattern:
 * 1. Validate email/username uniqueness (application concern)
 * 2. Create User aggregate (User.create factory)
 * 3. Save & publish events
 */
@Component
public class RegisterUserUseCase {
    
    private final UserRepository userRepository;
    private final DomainEventPublisher eventPublisher;
    private final EmailService emailService;

    public RegisterUserUseCase(UserRepository userRepository,
                               DomainEventPublisher eventPublisher,
                               EmailService emailService) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
        this.emailService = emailService;
    }
    
    @Transactional
    public User execute(RegisterUserCommand command) {
        // Step 1: Validate uniqueness (application-level concern)
        if (userRepository.existsByEmail(command.getEmail())) {
            throw new IllegalArgumentException("Email " + command.getEmail().getValue() + " already exists");
        }
        
        if (userRepository.existsByUsername(command.getUsername())) {
            throw new IllegalArgumentException("Username " + command.getUsername().getValue() + " already exists");
        }
        
        // Step 2: Create user aggregate (emits UserCreatedEvent)
        User user = User.create(
            command.getUsername(),
            command.getEmail(),
            command.getPassword(),
            command.getRole()
        );
        
        // Accept terms if required
        if (command.isTermsAccepted()) {
            user.acceptTerms("v1.0"); // TODO: Get version from config
        }
        
        // Step 3: Save & publish events
        User savedUser = userRepository.save(user);
        
        // Publish domain events
        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        
        // Send welcome email (infrastructure side effect)
        emailService.sendWelcomeEmail(
            savedUser.getEmail().getValue(),
            savedUser.getUsername().getValue()
        );
        
        return savedUser;
    }
}
