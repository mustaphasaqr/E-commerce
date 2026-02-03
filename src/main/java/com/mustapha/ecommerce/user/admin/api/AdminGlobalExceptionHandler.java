package com.mustapha.ecommerce.user.admin.api;

import com.mustapha.ecommerce.user.domain.exception.InvalidUserStateException;
import com.mustapha.ecommerce.user.domain.exception.UserBlockedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice(basePackages = "com.mustapha.ecommerce.user.admin")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class AdminGlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(AdminGlobalExceptionHandler.class);

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied in admin: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            new ErrorResponse(403, "Forbidden", "Access denied - requires OWNER role", LocalDateTime.now())
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        logger.debug("Illegal argument in admin: {}", ex.getMessage());
        
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(404, "Not found", ex.getMessage(), LocalDateTime.now())
            );
        }
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", ex.getMessage(), LocalDateTime.now())
        );
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        logger.debug("Runtime exception in admin: {}", ex.getMessage());
        
        if (ex.getMessage() != null && ex.getMessage().contains("not found")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                new ErrorResponse(404, "Not found", ex.getMessage(), LocalDateTime.now())
            );
        }
        
        logger.error("Unexpected runtime error in admin: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ErrorResponse(500, "Internal server error", "An unexpected error occurred", LocalDateTime.now())
        );
    }

    @ExceptionHandler(InvalidUserStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUserState(InvalidUserStateException ex) {
        logger.debug("Invalid user state in admin: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", ex.getMessage(), LocalDateTime.now())
        );
    }

    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlocked(UserBlockedException ex) {
        logger.debug("User is blocked in admin: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ErrorResponse(400, "Bad request", "User is blocked: " + ex.getMessage(), LocalDateTime.now())
        );
    }

    public record ErrorResponse(int status, String error, String message, LocalDateTime timestamp) {}
}
