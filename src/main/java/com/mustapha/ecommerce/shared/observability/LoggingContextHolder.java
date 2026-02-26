package com.mustapha.ecommerce.shared.observability;

import org.slf4j.MDC;

/**
 * Centralized utility for managing logging context via SLF4J's MDC (Mapped Diagnostic Context).
 * 
 * <p>MDC provides a thread-local storage mechanism for adding contextual information to logs.
 * This class wraps MDC operations with a clean API and standardized key names.
 * 
 * <p>Why use MDC?
 * <ul>
 *   <li>Automatically includes context in ALL log statements without passing parameters</li>
 *   <li>Enables filtering and searching logs by user, session, or IP address</li>
 *   <li>Supports distributed tracing and debugging</li>
 *   <li>Integrates seamlessly with structured logging (JSON output)</li>
 * </ul>
 * 
 * <p>Usage Pattern:
 * <pre>{@code
 * // In JwtAuthenticationFilter after successful authentication:
 * LoggingContextHolder.setUserId(user.getId());
 * LoggingContextHolder.setSessionId(sessionId);
 * 
 * // In any service/controller:
 * logger.info("Processing order"); // Includes userId, sessionId automatically
 * 
 * // At request completion (usually in filter):
 * LoggingContextHolder.clear();
 * }</pre>
 * 
 * <p>Thread Safety:
 * MDC uses ThreadLocal storage, so each thread has isolated context.
 * However, MUST call clear() at request completion to prevent memory leaks.
 */
public final class LoggingContextHolder {
    
    /**
     * MDC key for user ID.
     * Populated after successful authentication to track which user performed actions.
     */
    private static final String MDC_USER_ID_KEY = "userId";
    
    /**
     * MDC key for session ID.
     * Useful for correlating multiple requests from the same authenticated session.
     */
    private static final String MDC_SESSION_ID_KEY = "sessionId";
    
    /**
     * MDC key for client IP address.
     * Critical for security auditing, rate limiting analysis, and fraud detection.
     */
    private static final String MDC_IP_ADDRESS_KEY = "ipAddress";
    
    /**
     * MDC key for HTTP method (GET, POST, etc.).
     * Helpful for filtering logs by operation type.
     */
    private static final String MDC_HTTP_METHOD_KEY = "httpMethod";
    
    /**
     * MDC key for request path.
     * Enables filtering logs by endpoint (e.g., all /api/orders/* requests).
     */
    private static final String MDC_REQUEST_PATH_KEY = "requestPath";
    
    /**
     * MDC key for user role/authority.
     * Useful for auditing admin actions vs regular user actions.
     */
    private static final String MDC_USER_ROLE_KEY = "userRole";
    
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private LoggingContextHolder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
    
    /**
     * Sets the authenticated user ID in logging context.
     * Call this immediately after successful authentication.
     * 
     * @param userId the unique identifier of the authenticated user (never null)
     */
    public static void setUserId(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            MDC.put(MDC_USER_ID_KEY, userId);
        }
    }
    
    /**
     * Sets the session ID in logging context.
     * Useful for correlating multiple requests from the same session.
     * 
     * @param sessionId the unique session identifier (can be null)
     */
    public static void setSessionId(String sessionId) {
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            MDC.put(MDC_SESSION_ID_KEY, sessionId);
        }
    }
    
    /**
     * Sets the client IP address in logging context.
     * Critical for security auditing and rate limit analysis.
     * 
     * <p>NOTE: Should extract real client IP from X-Forwarded-For header
     * when behind proxies/load balancers, not just request.getRemoteAddr().
     * 
     * @param ipAddress the client's IP address (can be null)
     */
    public static void setIpAddress(String ipAddress) {
        if (ipAddress != null && !ipAddress.trim().isEmpty()) {
            MDC.put(MDC_IP_ADDRESS_KEY, ipAddress);
        }
    }
    
    /**
     * Sets the HTTP method in logging context.
     * 
     * @param httpMethod the HTTP method (GET, POST, PUT, DELETE, etc.)
     */
    public static void setHttpMethod(String httpMethod) {
        if (httpMethod != null && !httpMethod.trim().isEmpty()) {
            MDC.put(MDC_HTTP_METHOD_KEY, httpMethod);
        }
    }
    
    /**
     * Sets the request path in logging context.
     * 
     * @param requestPath the request URI/path (e.g., /api/orders/123)
     */
    public static void setRequestPath(String requestPath) {
        if (requestPath != null && !requestPath.trim().isEmpty()) {
            MDC.put(MDC_REQUEST_PATH_KEY, requestPath);
        }
    }
    
    /**
     * Sets the user's role in logging context.
     * Useful for auditing privileged operations.
     * 
     * @param userRole the user's primary role (e.g., ADMIN, USER, SELLER)
     */
    public static void setUserRole(String userRole) {
        if (userRole != null && !userRole.trim().isEmpty()) {
            MDC.put(MDC_USER_ROLE_KEY, userRole);
        }
    }
    
    /**
     * Retrieves the current user ID from logging context.
     * 
     * @return the user ID or null if not set
     */
    public static String getUserId() {
        return MDC.get(MDC_USER_ID_KEY);
    }
    
    /**
     * Retrieves the current session ID from logging context.
     * 
     * @return the session ID or null if not set
     */
    public static String getSessionId() {
        return MDC.get(MDC_SESSION_ID_KEY);
    }
    
    /**
     * Retrieves the current IP address from logging context.
     * 
     * @return the IP address or null if not set
     */
    public static String getIpAddress() {
        return MDC.get(MDC_IP_ADDRESS_KEY);
    }
    
    /**
     * Clears ALL logging context for the current thread.
     * 
     * <p>CRITICAL: Must be called at the end of each request to prevent:
     * <ul>
     *   <li>Memory leaks (ThreadLocal storage not released)</li>
     *   <li>Context pollution (values from previous request bleeding into next request)</li>
     *   <li>Security issues (user ID from one request appearing in another user's logs)</li>
     * </ul>
     * 
     * <p>Typically called in filter's finally block:
     * <pre>{@code
     * try {
     *     LoggingContextHolder.setUserId(userId);
     *     // ... process request
     * } finally {
     *     LoggingContextHolder.clear();
     * }
     * }</pre>
     */
    public static void clear() {
        MDC.clear();
    }
    
    /**
     * Removes only the user ID from logging context.
     * Use when user logs out but request processing continues.
     */
    public static void removeUserId() {
        MDC.remove(MDC_USER_ID_KEY);
    }
    
    /**
     * Removes only the session ID from logging context.
     */
    public static void removeSessionId() {
        MDC.remove(MDC_SESSION_ID_KEY);
    }
    
    /**
     * Removes only the IP address from logging context.
     */
    public static void removeIpAddress() {
        MDC.remove(MDC_IP_ADDRESS_KEY);
    }
}
