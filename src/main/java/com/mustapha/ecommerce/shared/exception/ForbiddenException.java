package com.mustapha.ecommerce.shared.exception;

/**
 * Exception thrown when an authenticated user attempts to access a resource they don't have permission for.
 * 
 * <p>Use this for 403 FORBIDDEN scenarios where:
 * <ul>
 *   <li>User is authenticated (has valid JWT)</li>
 *   <li>User does NOT have permission to access the resource (wrong role, not owner, etc.)</li>
 * </ul>
 * 
 * <p>Difference from UnauthorizedException (401):
 * <ul>
 *   <li>401 Unauthorized: User is NOT authenticated (no JWT, invalid JWT, expired token)</li>
 *   <li>403 Forbidden: User IS authenticated but lacks permission</li>
 * </ul>
 * 
 * <p>Common Use Cases:
 * <ul>
 *   <li>User tries to delete another user's order</li>
 *   <li>Regular user tries to access admin-only endpoint</li>
 *   <li>User tries to modify a resource they don't own</li>
 * </ul>
 */
public class ForbiddenException extends BusinessException {
    
    private final ErrorCode errorCode;
    
    public ForbiddenException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public ForbiddenException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
