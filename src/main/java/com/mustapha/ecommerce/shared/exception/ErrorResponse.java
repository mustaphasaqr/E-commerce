package com.mustapha.ecommerce.shared.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

/**
 * Standardized error response structure for all API errors.
 * 
 * Benefits:
 * 1. Consistent error format across entire API
 * 2. Machine-readable error codes for client error handling
 * 3. Human-readable messages for development/debugging
 * 4. Timestamp for log correlation
 * 5. Optional details for field-level validation errors
 * 6. Request path for easier debugging
 * 
 * Example Response:
 * {
 *   "timestamp": "2026-02-22T10:30:15.123Z",
 *   "errorCode": "AUTH_INVALID_001",
 *   "message": "Invalid email/username or password",
 *   "path": "/api/auth/login",
 *   "details": {
 *     "ipAddress": "192.168.1.100",
 *     "attempts": "3"
 *   }
 * }
 * 
 * JsonInclude.ALWAYS ensures null fields are included (explicit nulls are clearer than omission).
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
public class ErrorResponse {
    
    /**
     * HTTP status code (e.g., 400, 401, 404, 409, 500).
     * Duplicates the HTTP response status for easier client-side handling.
     */
    private final int status;
    
    /**
     * ISO-8601 timestamp when error occurred.
     * Useful for correlating with server logs.
     */
    private final Instant timestamp;
    
    /**
     * Machine-readable error code (e.g., "AUTH_INVALID_001").
     * Client can use this for programmatic error handling.
     */
    private final String errorCode;
    
    /**
     * Human-readable error message.
     * Should be safe to display to end users (no stack traces, internal details).
     */
    private final String message;
    
    /**
     * Alias for message field (backward compatibility).
     * Some tests/clients expect "error" instead of "message".
     */
    private final String error;
    
    /**
     * Request path that caused the error (e.g., "/api/auth/login").
     * Helps with debugging when errors are reported.
     */
    private final String path;
    
    /**
     * Optional additional details (field validation errors, context).
     * Only included when relevant (e.g., validation errors).
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Map<String, String> details;
    
    /**
     * Full constructor with all fields.
     */
    public ErrorResponse(ErrorCode errorCode, String message, String path, Map<String, String> details, int status) {
        this.status = status;
        this.timestamp = Instant.now();
        this.errorCode = errorCode.getCode();
        this.message = message != null ? message : errorCode.getDefaultMessage();
        this.error = this.message;  // Alias for backward compatibility
        this.path = path;
        this.details = details;
    }
    
    /**
     * Constructor without custom message (uses default from ErrorCode).
     */
    public ErrorResponse(ErrorCode errorCode, String path, Map<String, String> details, int status) {
        this(errorCode, errorCode.getDefaultMessage(), path, details, status);
    }
    
    /**
     * Constructor without details map (simple errors).
     */
    public ErrorResponse(ErrorCode errorCode, String message, String path, int status) {
        this(errorCode, message, path, null, status);
    }
    
    /**
     * Minimal constructor (error code + path only).
     */
    public ErrorResponse(ErrorCode errorCode, String path, int status) {
        this(errorCode, errorCode.getDefaultMessage(), path, null, status);
    }
    
    // Legacy constructors without status (default to 500)
    public ErrorResponse(ErrorCode errorCode, String message, String path, Map<String, String> details) {
        this(errorCode, message, path, details, 500);
    }
    
    public ErrorResponse(ErrorCode errorCode, String path, Map<String, String> details) {
        this(errorCode, errorCode.getDefaultMessage(), path, details, 500);
    }
    
    public ErrorResponse(ErrorCode errorCode, String message, String path) {
        this(errorCode, message, path, null, 500);
    }
    
    public ErrorResponse(ErrorCode errorCode, String path) {
        this(errorCode, errorCode.getDefaultMessage(), path, null, 500);
    }
    
    // Getters
    
    public int getStatus() {
        return status;
    }
    
    public Instant getTimestamp() {
        return timestamp;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getError() {
        return error;
    }
    
    public String getPath() {
        return path;
    }
    
    public Map<String, String> getDetails() {
        return details;
    }
    
    @Override
    public String toString() {
        return "ErrorResponse{" +
                "status=" + status +
                ", timestamp=" + timestamp +
                ", errorCode='" + errorCode + '\'' +
                ", message='" + message + '\'' +
                ", error='" + error + '\'' +
                ", path='" + path + '\'' +
                ", details=" + details +
                '}';
    }
}
