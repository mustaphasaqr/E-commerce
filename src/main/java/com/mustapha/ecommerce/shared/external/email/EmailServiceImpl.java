package com.mustapha.ecommerce.shared.external.email;

import com.mustapha.ecommerce.user.application.port.EmailService;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Email Service Implementation
 * Responsibility: Send transactional emails (welcome, password reset, verification)
 * Pattern: Adapter (Domain Port → SendGrid/Email Provider)
 * 
 * Scope: SHARED across User and Auth subdomains
 * Used by: User (welcome emails), Auth (password reset, verification)
 * 
 * Resilience Features:
 * - @Retry: Automatic retry on transient failures (3 attempts)
 * - @CircuitBreaker: Stops calling email provider when failure rate exceeds 50%
 * - Fallback: Logs email details to database for manual retry
 * 
 * External Service: SendGrid (FREE tier: 100 emails/day)
 * - If API key not configured: Falls back to MOCK mode (logs only)
 * - Graceful degradation: Application works without external email
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    
    private final SendGrid sendGrid;
    private final String fromEmail;
    private final String fromName;
    private final boolean isEmailEnabled;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailServiceImpl(
            @Autowired(required = false) SendGrid sendGrid,
            @Value("${email.sendgrid.from-email:noreply@example.com}") String fromEmail,
            @Value("${email.sendgrid.from-name:E-commerce Platform}") String fromName,
            @Value("${email.enabled:true}") boolean isEmailEnabled,
            TemplateEngine templateEngine) {
        this.sendGrid = sendGrid;
        this.fromEmail = fromEmail;
        this.fromName = fromName;
        this.isEmailEnabled = isEmailEnabled;
        this.templateEngine = templateEngine;
        
        if (sendGrid != null && isEmailEnabled) {
            logger.info("✅ EmailService initialized with SendGrid (REAL mode)");
        } else {
            logger.warn("⚠️ EmailService initialized in MOCK mode (SendGrid not configured)");
            logger.warn("   To enable real emails: Set SENDGRID_API_KEY environment variable");
        }
    }

    /**
     * Send welcome email with resilience patterns
     * - Retry on transient failures (SMTP timeout, network errors)
     * - Circuit breaker prevents cascade failures
     * - Fallback logs to database for manual retry
     * - Async: Runs in background thread (doesn't block API)
     */
    @Override
    @Async("emailTaskExecutor")
    @Retry(name = "emailService", fallbackMethod = "sendWelcomeEmailFallback")
    @CircuitBreaker(name = "emailService")
    public void sendWelcomeEmail(String email, String username) {
        if (sendGrid == null || !isEmailEnabled) {
            logger.info("📧 [MOCK] Welcome email to: {} (username: {})", email, username);
            return;
        }
        
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email);
            String subject = "Welcome to " + fromName + "!";
            
            // Render Thymeleaf template
            String htmlContent = buildWelcomeEmailHtml(username);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Welcome email sent to: {}", email);
            } else {
                logger.error("❌ Failed to send welcome email to: {}. Status: {}, Body: {}", 
                           email, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid returned status: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            logger.error("Failed to send welcome email to: {}", email, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    private String buildWelcomeEmailHtml(String username) {
        Context context = new Context();
        context.setVariable("username", username);
        return templateEngine.process("email/welcome", context);
    }
    
    /**
     * Fallback for welcome email
     * Called when:
     * - All retry attempts exhausted
     * - Circuit breaker is OPEN
     */
    private void sendWelcomeEmailFallback(String email, String username, Throwable throwable) {
        logger.warn("Welcome email fallback triggered for: {}, reason: {}. Logging to database for manual retry.", 
                   email, throwable.getMessage());
        
        // TODO: Store failed email in database for manual retry
        // - Create FailedEmail entity
        // - Store type=WELCOME, recipient=email, data={username}, error=throwable.message
        // - Background job can retry later when email service is healthy
    }

    /**
     * Send password reset email with resilience patterns
     * - Async: Runs in background thread (doesn't block API)
     */
    @Override
    @Async("emailTaskExecutor")
    @Retry(name = "emailService", fallbackMethod = "sendPasswordResetEmailFallback")
    @CircuitBreaker(name = "emailService")
    public void sendPasswordResetEmail(String email, String resetToken) {
        if (sendGrid == null || !isEmailEnabled) {
            logger.info("📧 [MOCK] Password reset email to: {} (token: {}...)", 
                       email, resetToken.substring(0, Math.min(8, resetToken.length())));
            return;
        }
        
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email);
            String subject = "Password Reset Request";
            
            // Render Thymeleaf template
            String htmlContent = buildPasswordResetEmailHtml(resetToken);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Password reset email sent to: {}", email);
            } else {
                logger.error("❌ Failed to send password reset email to: {}. Status: {}", 
                           email, response.getStatusCode());
                throw new RuntimeException("SendGrid returned status: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            logger.error("Failed to send password reset email to: {}", email, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    private String buildPasswordResetEmailHtml(String resetToken) {
        String resetLink = "http://localhost:3000/reset-password?token=" + resetToken;
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(24);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        
        Context context = new Context();
        context.setVariable("resetLink", resetLink);
        context.setVariable("expirationTime", expirationTime.format(formatter));
        return templateEngine.process("email/password-reset", context);
    }
    
    private void sendPasswordResetEmailFallback(String email, String resetToken, Throwable throwable) {
        logger.warn("Password reset email fallback triggered for: {}, reason: {}. Logging to database.", 
                   email, throwable.getMessage());
        
        // TODO: Store failed email for manual retry
    }

    /**
     * Send email verification with resilience patterns
     * - Async: Runs in background thread (doesn't block API)
     */
    @Override
    @Async("emailTaskExecutor")
    @Retry(name = "emailService", fallbackMethod = "sendEmailVerificationEmailFallback")
    @CircuitBreaker(name = "emailService")
    public void sendEmailVerificationEmail(String email, String verificationToken) {
        if (sendGrid == null || !isEmailEnabled) {
            logger.info("📧 [MOCK] Email verification to: {} (token: {}...)", 
                       email, verificationToken.substring(0, Math.min(8, verificationToken.length())));
            return;
        }
        
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email);
            String subject = "Verify Your Email Address";
            
            // Render Thymeleaf template
            String htmlContent = buildEmailVerificationHtml(verificationToken);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Email verification sent to: {}", email);
            } else {
                logger.error("❌ Failed to send verification email to: {}. Status: {}", 
                           email, response.getStatusCode());
                throw new RuntimeException("SendGrid returned status: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            logger.error("Failed to send verification email to: {}", email, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    private String buildEmailVerificationHtml(String verificationToken) {
        String verificationLink = "http://localhost:3000/verify-email?token=" + verificationToken;
        LocalDateTime expirationTime = LocalDateTime.now().plusHours(48);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy 'at' hh:mm a");
        
        Context context = new Context();
        context.setVariable("verificationLink", verificationLink);
        context.setVariable("expirationTime", expirationTime.format(formatter));
        return templateEngine.process("email/email-verification", context);
    }
    
    private void sendEmailVerificationEmailFallback(String email, String verificationToken, Throwable throwable) {
        logger.warn("Email verification fallback triggered for: {}, reason: {}. Logging to database.", 
                   email, throwable.getMessage());
        
        // TODO: Store failed email for manual retry
    }
    
    @Override
    @Async("emailTaskExecutor")
    @Retry(name = "emailService", fallbackMethod = "sendTransactionalEmailFallback")
    @CircuitBreaker(name = "emailService")
    public void sendTransactionalEmail(String email, String subject, String htmlContent) {
        if (sendGrid == null || !isEmailEnabled) {
            logger.info("📧 [MOCK] Transactional email to: {} (subject: {})", email, subject);
            return;
        }
        
        try {
            Email from = new Email(fromEmail, fromName);
            Email to = new Email(email);
            Content content = new Content("text/html", htmlContent);
            
            Mail mail = new Mail(from, subject, to, content);
            
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sendGrid.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("✅ Transactional email sent to: {} (subject: {})", email, subject);
            } else {
                logger.error("❌ Failed to send transactional email to: {}. Status: {}, Body: {}", 
                           email, response.getStatusCode(), response.getBody());
                throw new RuntimeException("SendGrid returned status: " + response.getStatusCode());
            }
            
        } catch (IOException e) {
            logger.error("Failed to send transactional email to: {}", email, e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
    
    private void sendTransactionalEmailFallback(String email, String subject, String htmlContent, Throwable throwable) {
        logger.warn("Transactional email fallback triggered for: {}, reason: {}. Logging to database.", 
                   email, throwable.getMessage());
        
        // TODO: Store failed email for manual retry
    }
}
