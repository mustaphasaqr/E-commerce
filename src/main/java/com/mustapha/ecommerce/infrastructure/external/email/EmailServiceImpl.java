package com.mustapha.ecommerce.infrastructure.external.email;

import com.mustapha.ecommerce.user.application.port.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Email Service Implementation
 * Responsibility: Send transactional emails (welcome, password reset, verification)
 * Pattern: Adapter (Domain Port → SMTP/Email Provider)
 * 
 * Scope: SHARED across User and Auth subdomains
 * Used by: User (welcome emails), Auth (password reset, verification)
 * 
 * TODO (Week 3): Implement actual email sending
 * Options:
 * - Spring Mail (SMTP)
 * - SendGrid API
 * - AWS SES
 * - Azure Communication Services
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendWelcomeEmail(String email, String username) {
        // TODO: Implement actual email sending
        logger.info("📧 [MOCK] Sending welcome email to: {} (username: {})", email, username);
        
        // Production implementation:
        // - Load email template (Thymeleaf/Freemarker)
        // - Substitute variables (username, verification link)
        // - Send via SMTP or email provider API
        // - Handle failures (retry logic, dead letter queue)
    }

    @Override
    public void sendPasswordResetEmail(String email, String resetToken) {
        // TODO: Implement actual email sending
        logger.info("📧 [MOCK] Sending password reset email to: {} (token: {}...)", email, resetToken.substring(0, 8));
        
        // Production implementation:
        // - Generate reset link: https://domain.com/reset-password?token={resetToken}
        // - Token expires in 24 hours
        // - Include security warning
    }

    @Override
    public void sendEmailVerificationEmail(String email, String verificationToken) {
        // TODO: Implement actual email sending
        logger.info("📧 [MOCK] Sending email verification to: {} (token: {}...)", email, verificationToken.substring(0, 8));
        
        // Production implementation:
        // - Generate verification link: https://domain.com/verify-email?token={verificationToken}
        // - Token expires in 48 hours
    }
}
