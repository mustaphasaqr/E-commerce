package com.mustapha.ecommerce.shared.exception;

import com.mustapha.ecommerce.order.domain.exception.InvalidOrderItemException;
import com.mustapha.ecommerce.order.domain.exception.InvalidOrderStateException;
import com.mustapha.ecommerce.order.domain.exception.OrderModificationNotAllowedException;
import com.mustapha.ecommerce.order.infrastructure.exception.OrderNotFoundException;
import com.mustapha.ecommerce.product.domain.exception.InsufficientStockException;
import com.mustapha.ecommerce.product.domain.exception.InvalidProductStateException;
import com.mustapha.ecommerce.product.domain.exception.ProductAlreadyActiveException;
import com.mustapha.ecommerce.product.domain.exception.ProductAlreadyInactiveException;
import com.mustapha.ecommerce.product.domain.exception.ProductDiscontinuedException;
import com.mustapha.ecommerce.product.domain.exception.ProductInUseException;
import com.mustapha.ecommerce.user.auth.domain.exception.InvalidCredentialsException;
import com.mustapha.ecommerce.user.auth.domain.exception.RateLimitExceededException;
import com.mustapha.ecommerce.user.auth.domain.exception.TokenAlreadyUsedException;
import com.mustapha.ecommerce.user.auth.domain.service.AccountLockedException;
import com.mustapha.ecommerce.user.domain.exception.UserBlockedException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global Exception Handler with Error Codes
 * 
 * Provides centralized exception handling for the entire application.
 * Returns standardized ErrorResponse with machine-readable error codes.
 * 
 * Key Features:
 * 1. Error codes for programmatic error handling
 * 2. Consistent error response structure
 * 3. Security-aware (no stack traces, sensitive info in production)
 * 4. Request path included for debugging
 * 5. Detailed validation errors
 * 6. Comprehensive logging
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles IllegalArgumentException (validation, business rule violations).
     * 
     * Common causes:
     * - Invalid email format (Email.of("invalid"))
     * - Invalid username format
     * - Password strength validation failures
     * - Resource not found ("User with id X not found")
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        logger.warn("Validation error: {} at {}", ex.getMessage(), request.getRequestURI());
        
        // Determine specific error based on message content
        ErrorCode errorCode;
        String message = ex.getMessage();
        HttpStatus status;
        
        if (message != null && message.toLowerCase().contains("not found")) {
            errorCode = ErrorCode.USER_NOT_FOUND;
            status = HttpStatus.NOT_FOUND;
        } else if (message != null && message.toLowerCase().contains("already exists")) {
            errorCode = ErrorCode.VALIDATION_FAILED;
            status = HttpStatus.CONFLICT;
            message = "A resource with these values already exists";
        } else if (message != null && message.toLowerCase().contains("invalid or expired reset token")) {
            errorCode = ErrorCode.VALIDATION_FAILED;
            status = HttpStatus.BAD_REQUEST;
            message = "Invalid or expired reset token";
        } else if (message != null && (message.toLowerCase().contains("currency") || 
                                        message.toLowerCase().contains("currencies"))) {
            errorCode = ErrorCode.VALIDATION_FAILED;
            status = HttpStatus.BAD_REQUEST;
            message = "Invalid request";
        } else if (message != null && message.toLowerCase().contains("password")) {
            errorCode = ErrorCode.USER_WEAK_PASSWORD;
            status = HttpStatus.BAD_REQUEST;
        } else if (message != null && message.toLowerCase().contains("email")) {
            errorCode = ErrorCode.USER_INVALID_EMAIL;
            status = HttpStatus.BAD_REQUEST;
        } else {
            errorCode = ErrorCode.VALIDATION_FAILED;
            status = HttpStatus.BAD_REQUEST;
        }
        
        ErrorResponse response = new ErrorResponse(errorCode, message, request.getRequestURI(), status.value());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handles ProductNotFoundException.
     * Returns 404 Not Found with generic message.
     */
    @ExceptionHandler({
        com.mustapha.ecommerce.product.infrastructure.exception.ProductNotFoundException.class,
        com.mustapha.ecommerce.order.application.exception.ProductNotFoundException.class
    })
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(
            RuntimeException ex, HttpServletRequest request) {
        
        logger.warn("Product not found at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.PRODUCT_NOT_FOUND,
            "Product not found",
            request.getRequestURI(),
            404
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Handles generic RuntimeException (fallback, should be rare).
     * 
     * Security: Logs full exception but returns generic message to client.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        // Log full stack trace for internal debugging
        logger.error("Unexpected runtime exception at {}: {}", 
            request.getRequestURI(), ex.getMessage(), ex);
        
        // Determine specific error based on message (temporary - create specific exceptions later)
        ErrorCode errorCode;
        HttpStatus status;
        String message = ex.getMessage();
        
        if (message != null && message.contains("Invalid credentials")) {
            errorCode = ErrorCode.AUTH_INVALID_CREDENTIALS;
            status = HttpStatus.UNAUTHORIZED;
        } else if (message != null && message.contains("Invalid or expired token") 
                   && request.getRequestURI().contains("/password-reset")) {
            // Password reset token validation errors return 400
            errorCode = ErrorCode.VALIDATION_FAILED;
            status = HttpStatus.BAD_REQUEST;
            message = "Invalid or expired reset token";
        } else if (message != null && message.contains("not found")) {
            errorCode = ErrorCode.USER_NOT_FOUND;
            status = HttpStatus.NOT_FOUND;
        } else {
            errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            message = "An unexpected error occurred"; // Don't leak internal details
        }
        
        ErrorResponse response = new ErrorResponse(errorCode, message, request.getRequestURI(), status.value());
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Handles IllegalStateException (business rules, state transitions).
     * 
     * Examples:
     * - Cannot cancel shipped order
     * - Token already used
     * - Session already invalidated
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
            IllegalStateException ex, HttpServletRequest request) {
        
        logger.warn("State violation: {} at {}", ex.getMessage(), request.getRequestURI());
        
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED; // Default
        String message = ex.getMessage();
        
        // You can add more specific mappings based on message content
        if (message != null && message.toLowerCase().contains("order")) {
            errorCode = ErrorCode.ORDER_CANNOT_CANCEL;
        }
        
        ErrorResponse response = new ErrorResponse(errorCode, message, request.getRequestURI(), 400);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles rate limit exceeded exceptions.
     * Returns 429 Too Many Requests with retry information.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(
            RateLimitExceededException ex, HttpServletRequest request) {
        logger.warn("Rate limit exceeded from IP: {} at {}", request.getRemoteAddr(), request.getRequestURI());
        // Calculate retry-after in seconds
        long retryAfterSeconds = 60; // default 1 minute
        if (ex.getLockedUntil() != null) {
            retryAfterSeconds = java.time.Duration.between(
                java.time.LocalDateTime.now(), 
                ex.getLockedUntil()
            ).getSeconds();
            retryAfterSeconds = Math.max(retryAfterSeconds, 0);
        }
        ErrorResponse response = new ErrorResponse(
            ErrorCode.AUTH_RATE_LIMIT_EXCEEDED,
            "Too many failed login attempts. Please try again later.",
            request.getRequestURI(),
            null,
            429,
            retryAfterSeconds
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", String.valueOf(retryAfterSeconds))
            .body(response);
    }

    /**
     * Handles token already used exception (refresh token rotation).
     * 
     * Security: This indicates potential token theft.
     * Consider invalidating all user sessions.
     */
    @ExceptionHandler(TokenAlreadyUsedException.class)
    public ResponseEntity<ErrorResponse> handleTokenAlreadyUsedException(
            TokenAlreadyUsedException ex, HttpServletRequest request) {
        
        // Security event: possible token theft
        logger.warn("SECURITY: Token reuse detected at {}: {}", 
            request.getRequestURI(), ex.getMessage());
        
        // Password reset tokens return 400 (validation error), authentication tokens return 401
        boolean isPasswordReset = request.getRequestURI().contains("/password-reset");
        boolean isEmailVerification = request.getRequestURI().contains("/verify-email");
        
        if (isPasswordReset || isEmailVerification) {
            ErrorResponse response = new ErrorResponse(
                ErrorCode.VALIDATION_FAILED,
                "Invalid or expired reset token",
                request.getRequestURI(),
                400
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else {
            // Generic message for security (don't reveal token rotation mechanism)
            ErrorResponse response = new ErrorResponse(
                ErrorCode.AUTH_TOKEN_ALREADY_USED,
                "Invalid or expired token",
                request.getRequestURI(),
                401
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    /**
     * Handles invalid credentials exception (login failures).
     * Returns generic message to prevent user enumeration attacks.
     */
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException ex, HttpServletRequest request) {
        
        logger.info("Failed login attempt from IP: {} at {}", 
            request.getRemoteAddr(), request.getRequestURI());
        
        // Generic message: don't reveal if email exists or password is wrong
        ErrorResponse response = new ErrorResponse(
            ErrorCode.AUTH_INVALID_CREDENTIALS,
            request.getRequestURI(),
            401
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Handles validation errors from @Valid annotations.
     * Returns field-level error details for better UX.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        logger.warn("Validation failed at {}: {} errors", 
            request.getRequestURI(), ex.getBindingResult().getErrorCount());
        
        // Collect all field errors
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(
                error -> error.getField(),
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (existing, replacement) -> existing // Keep first error if duplicate field
            ));
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED,
            "Validation error",
            request.getRequestURI(),
            fieldErrors,
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Handles Spring Security access denied exceptions.
     * User is authenticated but lacks required permission/role.
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            org.springframework.security.access.AccessDeniedException ex, 
            HttpServletRequest request) {
        
        logger.warn("Access denied at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.AUTHZ_INSUFFICIENT_PERMISSIONS,
            request.getRequestURI(),
            403
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handles unauthorized access attempts (user not authenticated).
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException ex, HttpServletRequest request) {
        
        logger.warn("Unauthorized access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI(),
            401
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }
    
    /**
     * Handles forbidden access (user authenticated but lacks permission).
     * Returns 403 Forbidden.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException ex, HttpServletRequest request) {
        
        logger.warn("Forbidden access attempt at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI(),
            403
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }
    
    /**
     * Handles account lockout exceptions (too many failed login attempts).
     * Returns 403 Forbidden with remaining lockout time in details.
     */
    @ExceptionHandler(AccountLockedException.class)
    public ResponseEntity<ErrorResponse> handleAccountLockedException(
            AccountLockedException ex, HttpServletRequest request) {
        
        logger.warn("Account lockout at {}: {}", request.getRequestURI(), ex.getMessage());
        
        // Include remaining lockout time in response details
        Map<String, String> details = new HashMap<>();
        details.put("remainingSeconds", String.valueOf(ex.getRemainingLockoutSeconds()));
        details.put("remainingMinutes", String.valueOf(ex.getRemainingLockoutMinutes()));
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.AUTH_ACCOUNT_LOCKED,
            "Too many failed login attempts. Please try again later.",
            request.getRequestURI(),
            details,
            429
        );
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }
    
    /**
     * Handles UserBlockedException when attempting operations on blocked users.
     * Returns 400 BAD_REQUEST when trying to modify a blocked user.
     */
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<ErrorResponse> handleUserBlockedException(
            UserBlockedException ex, HttpServletRequest request) {
        
        logger.warn("Operation attempted on blocked user at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED,
            "Operation not allowed: User is blocked",
            request.getRequestURI(),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles product domain business rule violations.
     * Returns 409 Conflict for business logic errors.
     */
    @ExceptionHandler({
        InsufficientStockException.class,
        InvalidProductStateException.class,
        ProductInUseException.class,
        ProductDiscontinuedException.class,
        ProductAlreadyActiveException.class,
        ProductAlreadyInactiveException.class
    })
    public ResponseEntity<ErrorResponse> handleProductDomainException(
            RuntimeException ex, HttpServletRequest request) {
        
        logger.warn("Product domain violation at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorCode errorCode;
        String message;
        
        if (ex instanceof InsufficientStockException) {
            errorCode = ErrorCode.PRODUCT_INSUFFICIENT_STOCK;
            message = "Insufficient stock";
        } else if (ex instanceof ProductDiscontinuedException) {
            errorCode = ErrorCode.PRODUCT_DISCONTINUED;
            message = errorCode.getDefaultMessage();
        } else {
            errorCode = ErrorCode.VALIDATION_FAILED;
            message = "Invalid product state";
        }
        
        ErrorResponse response = new ErrorResponse(
            errorCode,
            message,
            request.getRequestURI(),
            409
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handles order validation exceptions (invalid items, quantities, etc.).
     * Returns 400 Bad Request for validation errors.
     */
    @ExceptionHandler(InvalidOrderItemException.class)
    public ResponseEntity<ErrorResponse> handleInvalidOrderItemException(
            InvalidOrderItemException ex, HttpServletRequest request) {
        
        logger.warn("Order item validation error at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED,
            "Invalid order item",
            request.getRequestURI(),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles order domain business rule violations.
     * Returns 409 Conflict for state transition errors.
     */
    @ExceptionHandler({
        InvalidOrderStateException.class,
        OrderModificationNotAllowedException.class
    })
    public ResponseEntity<ErrorResponse> handleOrderDomainException(
            RuntimeException ex, HttpServletRequest request) {
        
        logger.warn("Order domain violation at {}: {}", request.getRequestURI(), ex.getMessage());
        
        String message;
        if (ex instanceof InvalidOrderStateException) {
            message = "Invalid order state";
        } else {
            message = "Order cannot be modified";
        }
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.ORDER_CANNOT_CANCEL,
            message,
            request.getRequestURI(),
            409
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handles OrderNotFoundException.
     * Returns 404 Not Found with generic message.
     */
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleOrderNotFoundException(
            OrderNotFoundException ex, HttpServletRequest request) {
        
        logger.warn("Order not found at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.ORDER_NOT_FOUND,
            "Order not found",
            request.getRequestURI(),
            404
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    /**
     * Handles database constraint violations (unique keys, foreign keys, etc.).
     * Returns 409 Conflict for constraint violations.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex, HttpServletRequest request) {
        
        logger.warn("Data integrity violation at {}: {}", request.getRequestURI(), ex.getMessage());
        
        String message = "Resource already exists or violates data constraint";
        ErrorCode errorCode = ErrorCode.VALIDATION_FAILED;
        
        // Check for specific constraint violations
        String exceptionMessage = ex.getMessage();
        if (exceptionMessage != null) {
            if (exceptionMessage.toLowerCase().contains("unique")) {
                message = "A resource with these values already exists";
            } else if (exceptionMessage.toLowerCase().contains("foreign key")) {
                message = "Cannot perform operation due to related data";
            }
        }
        
        ErrorResponse response = new ErrorResponse(
            errorCode,
            message,
            request.getRequestURI(),
            409
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }
    
    /**
     * Handles invalid JSON or malformed request body.
     * Returns 400 Bad Request when client sends unparseable JSON.
     */
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            org.springframework.http.converter.HttpMessageNotReadableException ex, 
            HttpServletRequest request) {
        
        logger.warn("Invalid JSON at {}: {}", request.getRequestURI(), ex.getMessage());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED,
            "Invalid request body format",
            request.getRequestURI(),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Handles missing required request parameters.
     * Returns 400 Bad Request when required @RequestParam is missing.
     */
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(
            org.springframework.web.bind.MissingServletRequestParameterException ex,
            HttpServletRequest request) {
        
        logger.warn("Missing required parameter '{}' at {}", ex.getParameterName(), request.getRequestURI());
        
        ErrorResponse response = new ErrorResponse(
            ErrorCode.VALIDATION_FAILED,
            "Missing required parameter: " + ex.getParameterName(),
            request.getRequestURI(),
            400
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    /**
     * Unwraps ServletException to handle the actual root cause.
     * Many Spring exceptions get wrapped in ServletException during request processing.
     */
    @ExceptionHandler(jakarta.servlet.ServletException.class)
    public ResponseEntity<ErrorResponse> handleServletException(
            jakarta.servlet.ServletException ex, HttpServletRequest request) {
        
        logger.debug("Unwrapping ServletException at {}", request.getRequestURI());
        
        // Find the root cause
        Throwable rootCause = ex.getCause();
        while (rootCause != null && rootCause.getCause() != null && rootCause.getCause() != rootCause) {
            rootCause = rootCause.getCause();
        }
        
        if (rootCause == null) {
            rootCause = ex;
        }
        
        // Delegate to appropriate handler based on root cause type
        if (rootCause instanceof InsufficientStockException ||
            rootCause instanceof InvalidProductStateException ||
            rootCause instanceof ProductInUseException ||
            rootCause instanceof ProductDiscontinuedException ||
            rootCause instanceof ProductAlreadyActiveException ||
            rootCause instanceof ProductAlreadyInactiveException) {
            return handleProductDomainException((RuntimeException) rootCause, request);
        }
        
        if (rootCause instanceof InvalidOrderItemException) {
            return handleInvalidOrderItemException((InvalidOrderItemException) rootCause, request);
        }
        
        if (rootCause instanceof InvalidOrderStateException ||
            rootCause instanceof OrderModificationNotAllowedException) {
            return handleOrderDomainException((RuntimeException) rootCause, request);
        }
        
        if (rootCause instanceof IllegalArgumentException) {
            return handleIllegalArgumentException((IllegalArgumentException) rootCause, request);
        }
        
        if (rootCause instanceof InvalidCredentialsException) {
            return handleInvalidCredentialsException((InvalidCredentialsException) rootCause, request);
        }
        
        if (rootCause instanceof AccountLockedException) {
            return handleAccountLockedException((AccountLockedException) rootCause, request);
        }
        
        if (rootCause instanceof UserBlockedException) {
            return handleUserBlockedException((UserBlockedException) rootCause, request);
        }
        
        if (rootCause instanceof RateLimitExceededException) {
            return handleRateLimitExceededException((RateLimitExceededException) rootCause, request);
        }
        
        // Fall back to generic exception handler
        return handleGenericException((Exception) rootCause, request);
    }
    
    /**
     * Catches all unhandled exceptions (last resort).
     * 
     * Security: Never expose stack trace or internal details to client.
     * Always log full exception for internal debugging.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        
        // Log full exception for debugging
        logger.error("Unhandled exception at {}: {}", 
            request.getRequestURI(), ex.getMessage(), ex);
        
        // Generic response (don't leak internal details)
        ErrorResponse response = new ErrorResponse(
            ErrorCode.INTERNAL_SERVER_ERROR,
            request.getRequestURI(),
            500
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
