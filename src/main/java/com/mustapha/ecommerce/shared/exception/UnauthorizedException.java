package com.mustapha.ecommerce.shared.exception;

/**
 * Exception thrown when authentication is required but not provided or invalid.
 * 
 * <p>Use this for 401 UNAUTHORIZED scenarios where:
 * <ul>
 *   <li>No authentication credentials provided (no JWT token)</li>
 *   <li>Invalid authentication credentials (malformed JWT, wrong signature)</li>
 *   <li>Expired token (token was valid but has expired)</li>
 *   <li>Blacklisted token (user logged out)</li>
 * </ul>
 * 
 * <p>Difference from ForbiddenException (403):
 * <ul>
 *   <li>401 Unauthorized: User is NOT authenticated (missing or invalid credentials)</li>
 *   <li>403 Forbidden: User IS authenticated but lacks permission</li>
 * </ul>
 * 
 * <p>Common Use Cases:
 * <ul>
 *   <li>Missing Authorization header</li>
 *   <li>Invalid JWT signature</li>
 *   <li>Expired access token</li>
 *   <li>Token revoked (on blacklist)</li>
 * </ul>
 */
public class UnauthorizedException extends BusinessException {
    
    private final ErrorCode errorCode;
    
    public UnauthorizedException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public UnauthorizedException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
