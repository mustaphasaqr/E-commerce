package com.mustapha.ecommerce.user.auth.api;

import com.mustapha.ecommerce.user.auth.domain.exception.AuthDomainException;
import com.mustapha.ecommerce.user.auth.domain.exception.InvalidCredentialsException;
import com.mustapha.ecommerce.user.auth.domain.exception.RateLimitExceededException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;
import com.mustapha.ecommerce.user.auth.domain.exception.ExpiredTokenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.mustapha.ecommerce.user.auth")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AuthGlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AuthGlobalExceptionHandler.class);

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        logger.debug("Invalid credentials attempt: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ErrorResponse(401, "Unauthorized", "Invalid email/username or password", LocalDateTime.now())
        );
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceeded(RateLimitExceededException ex) {
        logger.warn("Rate limit exceeded: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
            new ErrorResponse(429, "Too Many Requests", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.debug("Illegal argument in auth: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleTokenAlreadyUsed(TokenAlreadyUsedException ex) {
        logger.debug("Token already used: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", "Invalid or expired reset token", LocalDateTime.now())
        );
    }

    @ExceptionHandler(ExpiredTokenException.class)
    public ResponseEntity<ErrorResponse> handleExpiredToken(ExpiredTokenException ex) {
        logger.debug("Expired token: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", "Invalid or expired reset token", LocalDateTime.now())
        );
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(IllegalStateException ex) {
        logger.debug("Illegal state in auth: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", ex.getMessage(), LocalDateTime.now())
        );
    }

    public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}
}
