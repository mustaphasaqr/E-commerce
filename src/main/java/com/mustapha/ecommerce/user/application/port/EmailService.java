package com.mustapha.ecommerce.user.application.port;

/**
 * Email Service Port (Outbound Port)
 * Responsibility: Send emails to users
 * Pattern: Port (Hexagonal Architecture)
 * 
 * Implementation will be in Infrastructure layer
 * Examples: SMTP, SendGrid, AWS SES
 */
public interface EmailService {
    
    /**
     * Send password reset email with token
     * 
     * @param toEmail Recipient email address
     * @param resetToken Password reset token
     */
    void sendPasswordResetEmail(String toEmail, String resetToken);
    
    /**
     * Send email verification email
     * 
     * @param toEmail Recipient email address
     * @param verificationToken Email verification token
     */
    void sendEmailVerificationEmail(String toEmail, String verificationToken);
    
    /**
     * Send welcome email after successful registration
     * 
     * @param toEmail Recipient email address
     * @param username User's username
     */
    void sendWelcomeEmail(String toEmail, String username);
}
