package com.mustapha.ecommerce.shared.exception;

/**
 * Centralized Error Codes for the entire application.
 * 
 * Format: {DOMAIN}_{ERROR_TYPE}_{NUMBER}
 * - DOMAIN: Module (AUTH, USER, PROD, ORDER, etc.)
 * - ERROR_TYPE: Category (VALIDATION, NOT_FOUND, FORBIDDEN, etc.)
 * - NUMBER: Sequential number within category
 * 
 * Benefits:
 * 1. Client can handle errors programmatically (if errorCode === 'AUTH_INVALID_001')
 * 2. Easy to search logs (search for "AUTH_INVALID_001")
 * 3. Internationalization-ready (map error codes to translated messages)
 * 4. API documentation clarity
 * 5. Backward compatibility (can change message, keep code)
 * 
 * Categories:
 * - VALIDATION: Input validation errors (400)
 * - NOT_FOUND: Resource not found (404)
 * - FORBIDDEN: Authorization failures (403)
 * - UNAUTHORIZED: Authentication failures (401)
 * - CONFLICT: Business rule violations (409)
 * - RATE_LIMIT: Too many requests (429)
 * - INTERNAL: Server errors (500)
 */
public enum ErrorCode {
    
    // ========== AUTHENTICATION ERRORS (AUTH_xxx) ==========
    
    /**
     * Invalid email/username or password during login.
     * Status: 401 UNAUTHORIZED
     */
    AUTH_INVALID_CREDENTIALS("AUTH_INVALID_001", "Invalid email/username or password"),
    
    /**
     * JWT token is invalid, expired, or malformed.
     * Status: 401 UNAUTHORIZED
     */
    AUTH_INVALID_TOKEN("AUTH_INVALID_002", "Invalid or expired authentication token"),
    
    /**
     * Refresh token is invalid, expired, or already used.
     * Status: 401 UNAUTHORIZED
     */
    AUTH_INVALID_REFRESH_TOKEN("AUTH_INVALID_003", "Invalid or expired refresh token"),
    
    /**
     * Refresh token has already been used (rotation detected).
     * Security: Possible token theft, invalidate all user sessions.
     * Status: 401 UNAUTHORIZED
     */
    AUTH_TOKEN_ALREADY_USED("AUTH_INVALID_004", "Refresh token has already been used"),
    
    /**
     * JWT token is blacklisted (user logged out).
     * Status: 401 UNAUTHORIZED
     */
    AUTH_TOKEN_BLACKLISTED("AUTH_INVALID_005", "Authentication token has been revoked"),
    
    /**
     * Password reset token is invalid or expired.
     * Status: 400 BAD_REQUEST
     */
    AUTH_INVALID_RESET_TOKEN("AUTH_INVALID_006", "Invalid or expired password reset token"),
    
    /**
     * Email verification token is invalid or expired.
     * Status: 400 BAD_REQUEST
     */
    AUTH_INVALID_VERIFICATION_TOKEN("AUTH_INVALID_007", "Invalid or expired verification token"),
    
    /**
     * Rate limit exceeded for authentication endpoints.
     * Status: 429 TOO_MANY_REQUESTS
     */
    AUTH_RATE_LIMIT_EXCEEDED("AUTH_RATE_001", "Too many authentication attempts. Please try again later"),
    
    /**
     * Account is locked due to too many failed login attempts.
     * Status: 403 FORBIDDEN
     */
    AUTH_ACCOUNT_LOCKED("AUTH_LOCK_001", "Account is temporarily locked due to multiple failed login attempts"),
    
    /**
     * Account is not verified (email not confirmed).
     * Status: 403 FORBIDDEN
     */
    AUTH_ACCOUNT_NOT_VERIFIED("AUTH_VERIFY_001", "Account email is not verified"),
    
    
    // ========== USER ERRORS (USER_xxx) ==========
    
    /**
     * User with given ID not found.
     * Status: 404 NOT_FOUND
     */
    USER_NOT_FOUND("USER_NOT_FOUND_001", "User not found"),
    
    /**
     * Email already exists (during registration).
     * Status: 409 CONFLICT
     */
    USER_EMAIL_EXISTS("USER_CONFLICT_001", "Email address is already registered"),
    
    /**
     * Username already exists (during registration).
     * Status: 409 CONFLICT
     */
    USER_USERNAME_EXISTS("USER_CONFLICT_002", "Username is already taken"),
    
    /**
     * Password does not meet strength requirements.
     * Status: 400 BAD_REQUEST
     */
    USER_WEAK_PASSWORD("USER_VALIDATION_001", "Password does not meet security requirements"),
    
    /**
     * Password is in common passwords list.
     * Status: 400 BAD_REQUEST
     */
    USER_COMMON_PASSWORD("USER_VALIDATION_002", "Password is too common and easily guessable"),
    
    /**
     * Password found in data breach database.
     * Status: 400 BAD_REQUEST
     */
    USER_BREACHED_PASSWORD("USER_VALIDATION_003", "Password has been exposed in a data breach"),
    
    /**
     * Invalid email format.
     * Status: 400 BAD_REQUEST
     */
    USER_INVALID_EMAIL("USER_VALIDATION_004", "Invalid email address format"),
    
    /**
     * Invalid username format.
     * Status: 400 BAD_REQUEST
     */
    USER_INVALID_USERNAME("USER_VALIDATION_005", "Invalid username format"),
    
    
    // ========== PRODUCT ERRORS (PROD_xxx) ==========
    
    /**
     * Product with given ID not found.
     * Status: 404 NOT_FOUND
     */
    PRODUCT_NOT_FOUND("PROD_NOT_FOUND_001", "Product not found"),
    
    /**
     * Product is out of stock.
     * Status: 409 CONFLICT
     */
    PRODUCT_OUT_OF_STOCK("PROD_CONFLICT_001", "Product is out of stock"),
    
    /**
     * Requested quantity exceeds available stock.
     * Status: 409 CONFLICT
     */
    PRODUCT_INSUFFICIENT_STOCK("PROD_CONFLICT_002", "Insufficient stock available"),
    
    /**
     * Product price is invalid (negative, zero, or too high).
     * Status: 400 BAD_REQUEST
     */
    PRODUCT_INVALID_PRICE("PROD_VALIDATION_001", "Invalid product price"),
    
    /**
     * Product quantity is invalid (negative or zero).
     * Status: 400 BAD_REQUEST
     */
    PRODUCT_INVALID_QUANTITY("PROD_VALIDATION_002", "Invalid product quantity"),
    
    /**
     * Product has been discontinued and is no longer available.
     * Status: 409 CONFLICT
     */
    PRODUCT_DISCONTINUED("PROD_CONFLICT_003", "Product has been discontinued"),
    
    
    // ========== ORDER ERRORS (ORDER_xxx) ==========
    
    /**
     * Order with given ID not found.
     * Status: 404 NOT_FOUND
     */
    ORDER_NOT_FOUND("ORDER_NOT_FOUND_001", "Order not found"),
    
    /**
     * User does not own this order (authorization failure).
     * Status: 403 FORBIDDEN
     */
    ORDER_NOT_OWNED("ORDER_FORBIDDEN_001", "You do not have permission to access this order"),
    
    /**
     * Order cannot be cancelled (already shipped, delivered, etc.).
     * Status: 409 CONFLICT
     */
    ORDER_CANNOT_CANCEL("ORDER_CONFLICT_001", "Order cannot be cancelled in its current state"),
    
    /**
     * Order is empty (no items).
     * Status: 400 BAD_REQUEST
     */
    ORDER_EMPTY("ORDER_VALIDATION_001", "Order must contain at least one item"),
    
    
    // ========== AUTHORIZATION ERRORS (AUTHZ_xxx) ==========
    
    /**
     * User does not have required role/permission.
     * Status: 403 FORBIDDEN
     */
    AUTHZ_INSUFFICIENT_PERMISSIONS("AUTHZ_FORBIDDEN_001", "Insufficient permissions to perform this action"),
    
    /**
     * User does not own the requested resource.
     * Status: 403 FORBIDDEN
     */
    AUTHZ_NOT_RESOURCE_OWNER("AUTHZ_FORBIDDEN_002", "You do not have permission to access this resource"),
    
    /**
     * IP address is not whitelisted for admin access.
     * Status: 403 FORBIDDEN
     */
    AUTHZ_IP_NOT_WHITELISTED("AUTHZ_FORBIDDEN_003", "Access denied from this IP address"),
    
    
    // ========== VALIDATION ERRORS (VAL_xxx) ==========
    
    /**
     * Generic validation error (for @Valid annotations).
     * Status: 400 BAD_REQUEST
     */
    VALIDATION_FAILED("VAL_ERROR_001", "Validation failed"),
    
    /**
     * Required field is missing.
     * Status: 400 BAD_REQUEST
     */
    VALIDATION_REQUIRED_FIELD("VAL_ERROR_002", "Required field is missing"),
    
    /**
     * Input contains malicious content (XSS, SQL injection attempt).
     * Status: 400 BAD_REQUEST
     */
    VALIDATION_MALICIOUS_INPUT("VAL_SECURITY_001", "Input contains invalid or malicious content"),
    
    
    // ========== RATE LIMITING ERRORS (RATE_xxx) ==========
    
    /**
     * General rate limit exceeded for API endpoints.
     * Status: 429 TOO_MANY_REQUESTS
     */
    RATE_LIMIT_EXCEEDED("RATE_LIMIT_001", "Too many requests. Please slow down and try again later"),
    
    
    // ========== INTERNAL ERRORS (INT_xxx) ==========
    
    /**
     * Generic internal server error.
     * Status: 500 INTERNAL_SERVER_ERROR
     */
    INTERNAL_SERVER_ERROR("INT_ERROR_001", "An unexpected error occurred"),
    
    /**
     * Database operation failed.
     * Status: 500 INTERNAL_SERVER_ERROR
     */
    INTERNAL_DATABASE_ERROR("INT_ERROR_002", "Database operation failed"),
    
    /**
     * External service (payment gateway, email service) unavailable.
     * Status: 503 SERVICE_UNAVAILABLE
     */
    INTERNAL_SERVICE_UNAVAILABLE("INT_ERROR_003", "External service temporarily unavailable");
    
    
    private final String code;
    private final String defaultMessage;
    
    ErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDefaultMessage() {
        return defaultMessage;
    }
    
    @Override
    public String toString() {
        return code + ": " + defaultMessage;
    }
}
