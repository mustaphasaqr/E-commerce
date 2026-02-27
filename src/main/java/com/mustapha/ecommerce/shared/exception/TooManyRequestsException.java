package com.mustapha.ecommerce.shared.exception;

/**
 * Exception thrown when rate limit is exceeded.
 * HTTP Status: 429 Too Many Requests
 */
public class TooManyRequestsException extends BusinessException {
    
    private final ErrorCode errorCode;
    
    public TooManyRequestsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public TooManyRequestsException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
