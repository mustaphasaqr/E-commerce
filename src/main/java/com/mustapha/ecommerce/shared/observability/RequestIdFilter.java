package com.mustapha.ecommerce.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter that generates or extracts a unique request ID for correlation across logs and services.
 * 
 * <p>This filter executes very early in the filter chain to ensure all subsequent
 * processing and logging includes the request ID for traceability.
 * 
 * <p>Request ID Flow:
 * <ol>
 *   <li>Check for existing X-Request-ID header (from upstream service/proxy)</li>
 *   <li>Generate new UUID if no header present</li>
 *   <li>Add to MDC (Mapped Diagnostic Context) for logging</li>
 *   <li>Add to response header for client correlation</li>
 *   <li>Clear MDC after request completion to prevent memory leaks</li>
 * </ol>
 * 
 * <p>This enables:
 * <ul>
 *   <li>End-to-end request tracing across microservices</li>
 *   <li>Correlation of all logs for a single request</li>
 *   <li>Debugging distributed transactions</li>
 *   <li>Support ticket investigation (customer provides request ID)</li>
 * </ul>
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RequestIdFilter extends OncePerRequestFilter {
    
    /**
     * Standard header name for request ID propagation.
     * Compatible with common proxies, load balancers, and microservice frameworks.
     */
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    
    /**
     * MDC key for storing request ID.
     * This key is referenced in logback-spring.xml for automatic inclusion in logs.
     */
    private static final String MDC_REQUEST_ID_KEY = "requestId";
    
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // Extract existing request ID from header (from upstream service) or generate new one
            String requestId = extractOrGenerateRequestId(request);
            
            // Add to MDC for logging - all log statements in this thread will include requestId
            MDC.put(MDC_REQUEST_ID_KEY, requestId);
            
            // Add additional request context to MDC using LoggingContextHolder
            LoggingContextHolder.setIpAddress(extractClientIpAddress(request));
            LoggingContextHolder.setHttpMethod(request.getMethod());
            LoggingContextHolder.setRequestPath(request.getRequestURI());
            
            // Add to response header for client correlation and debugging
            response.setHeader(REQUEST_ID_HEADER, requestId);
            
            // Continue filter chain with request ID in context
            filterChain.doFilter(request, response);
            
        } finally {
            // Critical: Clear ALL MDC to prevent memory leaks and context pollution
            // MDC uses ThreadLocal, so must be cleaned up after request completes
            // This clears requestId, ipAddress, httpMethod, requestPath, and any other MDC values
            LoggingContextHolder.clear();
        }
    }
    
    /**
     * Extracts request ID from incoming request header or generates a new UUID.
     * 
     * <p>This supports distributed tracing by preserving request IDs across service boundaries.
     * If the client or upstream service provides X-Request-ID, we use it for continuity.
     * 
     * @param request the HTTP request potentially containing X-Request-ID header
     * @return existing request ID from header, or newly generated UUID
     */
    private String extractOrGenerateRequestId(HttpServletRequest request) {
        String existingRequestId = request.getHeader(REQUEST_ID_HEADER);
        
        if (existingRequestId != null && !existingRequestId.trim().isEmpty()) {
            // Use existing request ID for distributed tracing continuity
            return existingRequestId.trim();
        }
        
        // Generate new UUID for this request
        return UUID.randomUUID().toString();
    }
    
    /**
     * Extracts the real client IP address from the request.
     * 
     * <p>When behind proxies/load balancers, the real client IP is typically in headers
     * like X-Forwarded-For, X-Real-IP, etc. This method checks these headers in order
     * of preference before falling back to the remote address.
     * 
     * <p>Security Note: X-Forwarded-For can be spoofed, so in production environments
     * with untrusted proxies, additional validation may be needed. However, for logging
     * purposes, this provides useful debugging information.
     * 
     * @param request the HTTP request
     * @return the client's IP address (best effort)
     */
    private String extractClientIpAddress(HttpServletRequest request) {
        // Check X-Forwarded-For header (comma-separated list if multiple proxies)
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.trim().isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        // Check X-Real-IP header (set by some proxies like nginx)
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.trim().isEmpty()) {
            return xRealIp.trim();
        }
        
        // Fallback to remote address (direct connection or last proxy)
        return request.getRemoteAddr();
    }
}
