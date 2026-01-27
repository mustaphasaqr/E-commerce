package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.PasswordResetRequestCommand;
import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import com.mustapha.ecommerce.user.application.port.EmailService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Password Reset Request Use Case
 * Responsibility: Initiate password reset flow
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Find user by email
 * 2. Create password reset token (24-hour validity)
 * 3. Send reset email
 * 4. Publish events
 */
@Component
public class PasswordResetRequestUseCase {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailService emailService;
    private final DomainEventPublisher eventPublisher;

    public PasswordResetRequestUseCase(UserRepository userRepository,
                                       PasswordResetTokenRepository tokenRepository,
                                       EmailService emailService,
                                       DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public void execute(PasswordResetRequestCommand command) {
        // Step 1: Find user (silently fail if not found - security best practice)
        User user = userRepository.findByEmail(command.getEmail()).orElse(null);
        
        if (user == null) {
            // Don't reveal if email exists or not
            return;
        }
        
        // Step 2: Delete any existing tokens for this user
        tokenRepository.deleteAllByUserId(user.getId());
        
        // Step 3: Create password reset token
        PasswordResetToken token = PasswordResetToken.create(
            user.getId().getValue().toString(),
            command.getEmail().getValue()
        );
        tokenRepository.save(token);
        
        // Step 4: Send reset email
        emailService.sendPasswordResetEmail(command.getEmail().getValue(), token.getToken());
        
        // Step 5: Publish event (PasswordResetToken doesn't store events, publish directly)
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.PasswordResetTokenCreatedEvent(
            token.getToken(),
            token.getUserId(),
            token.getEmail()
        ));
    }
}
