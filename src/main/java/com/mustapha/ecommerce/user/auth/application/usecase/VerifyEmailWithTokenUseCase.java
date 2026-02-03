package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.auth.application.command.VerifyEmailWithTokenCommand;
import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.auth.domain.repository.EmailVerificationTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.model.valueobject.UserId;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VerifyEmailWithTokenUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final DomainEventPublisher eventPublisher;

    public VerifyEmailWithTokenUseCase(UserRepository userRepository,
                                      EmailVerificationTokenRepository tokenRepository,
                                      @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(VerifyEmailWithTokenCommand command) {
        EmailVerificationToken token = tokenRepository.findByToken(command.getToken())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        token.use();

        User user = userRepository.findById(UserId.of(UUID.fromString(token.getUserId())))
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.verifyEmail();
        
        User savedUser = userRepository.save(user);
        tokenRepository.save(token);

        savedUser.getDomainEvents().forEach(eventPublisher::publish);
        savedUser.clearDomainEvents();
        
        tokenRepository.delete(token.getToken());
    }
}
