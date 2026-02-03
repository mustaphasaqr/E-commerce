package com.mustapha.ecommerce.user.auth.application.usecase;

import com.mustapha.ecommerce.user.application.port.DomainEventPublisher;
import com.mustapha.ecommerce.user.application.port.EmailService;
import com.mustapha.ecommerce.user.auth.application.command.RequestEmailVerificationCommand;
import com.mustapha.ecommerce.user.auth.domain.model.EmailVerificationToken;
import com.mustapha.ecommerce.user.auth.domain.repository.EmailVerificationTokenRepository;
import com.mustapha.ecommerce.user.domain.model.User;
import com.mustapha.ecommerce.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestEmailVerificationUseCase {

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final DomainEventPublisher eventPublisher;

    public RequestEmailVerificationUseCase(UserRepository userRepository,
                                          EmailVerificationTokenRepository tokenRepository,
                                          EmailService emailService,
                                          @Qualifier("userDomainEventPublisherAdapter") DomainEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void execute(RequestEmailVerificationCommand command) {
        User user = userRepository.findByEmail(command.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + command.getEmail().getValue()));

        if (user.isEmailVerified()) {
            throw new IllegalStateException("Email already verified");
        }

        EmailVerificationToken token = EmailVerificationToken.create(
            user.getId().getValue().toString(),
            user.getEmail().getValue()
        );

        tokenRepository.save(token);
        emailService.sendEmailVerificationEmail(command.getEmail().getValue(), token.getToken());
    }
}
