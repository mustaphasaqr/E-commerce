package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.PasswordResetCompleteCommand;
import com.mustapha.ecommerce.user.auth.domain.model.PasswordResetToken;
import com.mustapha.ecommerce.user.auth.domain.repository.PasswordResetTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Password Reset Complete Use Case
 * Responsibility: Complete password reset flow
 * Pattern: Use Case (Application Service)
 * 
 * Flow:
 * 1. Find and validate token
 * 2. Find user
 * 3. Change password (domain operation)
 * 4. Mark token as used
 * 5. Publish events
 */
@Component
public class PasswordResetCompleteUseCase {
    
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final DomainEventPublisher eventPublisher;

    public PasswordResetCompleteUseCase(UserRepository userRepository,
                                        PasswordResetTokenRepository tokenRepository,
                                        DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
    }
    
    @Transactional
    public void execute(PasswordResetCompleteCommand command) {
        // Step 1: Find and validate token
        PasswordResetToken token = tokenRepository.findByToken(command.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset token"));
        
        token.use(); // Validates (throws if expired or already used) and marks as used
        
        // Step 2: Find user
        User user = userRepository.findById(UserId.of(UUID.fromString(token.getUserId())))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Step 3: Reset password (without current password verification - this is password reset!)
        user.resetPassword(command.getNewPassword());
        
        // Step 4: Save both
        tokenRepository.save(token);
        userRepository.save(user);
        
        // Step 5: Publish events (PasswordResetToken doesn't store events, publish directly)
        eventPublisher.publish(new com.mustapha.ecommerce.user.auth.domain.event.PasswordResetCompletedEvent(
            user.getId().getValue().toString()
        ));
        
        // Publish user domain events (password changed)
        user.getDomainEvents().forEach(eventPublisher::publish);
        user.clearDomainEvents();
    }
}
