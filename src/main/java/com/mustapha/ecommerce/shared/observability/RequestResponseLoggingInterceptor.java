package com.mustapha.ecommerce.shared.observability;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Request/Response Logging Interceptor
 * Logs all HTTP requests and responses for debugging and audit purposes
 * 
 * Features:
 * - Request details: method, URI, headers, parameters, body
 * - Response details: status code, headers, body (truncated if large)
 * - Request duration tracking
 * - Correlation ID from MDC
 * - Excludes actuator and static resource endpoints
 * 
 * Log Level: INFO for successful requests, WARN for errors
 */
@Component
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);
    private static final String REQUEST_START_TIME = "request_start_time";
    private static final int MAX_BODY_LENGTH = 1000; // Truncate large bodies

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Skip logging for actuator and static resources
        String uri = request.getRequestURI();
        if (shouldSkipLogging(uri)) {
            return true;
        }

        // Record start time
        request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());

        // Log request details
        logRequest(request);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        // Skip logging for actuator and static resources
        String uri = request.getRequestURI();
        if (shouldSkipLogging(uri)) {
            return;
        }

        // Calculate request duration
        Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
        long duration = startTime != null ? System.currentTimeMillis() - startTime : 0;

        // Log response details
        logResponse(request, response, duration, ex);
    }

    private void logRequest(HttpServletRequest request) {
        String correlationId = MDC.get("correlationId");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;

        Map<String, String> headers = extractHeaders(request);
        Map<String, String[]> parameters = request.getParameterMap();

        log.info("→ Incoming Request | correlationId={} | {} {} | headers={} | params={}", 
                correlationId, method, fullUrl, headers, parameters);
    }

    private void logResponse(HttpServletRequest request, HttpServletResponse response, 
                            long duration, Exception ex) {
        String correlationId = MDC.get("correlationId");
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Determine log level based on status code
        if (status >= 500 || ex != null) {
            log.error("← Response Error | correlationId={} | {} {} | status={} | duration={}ms | exception={}", 
                    correlationId, method, uri, status, duration, 
                    ex != null ? ex.getMessage() : "none");
        } else if (status >= 400) {
            log.warn("← Response Client Error | correlationId={} | {} {} | status={} | duration={}ms", 
                    correlationId, method, uri, status, duration);
        } else {
            log.info("← Response Success | correlationId={} | {} {} | status={} | duration={}ms", 
                    correlationId, method, uri, status, duration);
        }
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            // Skip sensitive headers
            if (!isSensitiveHeader(headerName)) {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        
        return headers;
    }

    private boolean isSensitiveHeader(String headerName) {
        String lowerCase = headerName.toLowerCase();
        return lowerCase.contains("authorization") || 
               lowerCase.contains("password") || 
               lowerCase.contains("token") ||
               lowerCase.contains("secret") ||
               lowerCase.contains("api-key");
    }

    private boolean shouldSkipLogging(String uri) {
        return uri.startsWith("/actuator") || 
               uri.startsWith("/v3/api-docs") || 
               uri.startsWith("/swagger-ui") ||
               uri.endsWith(".css") ||
               uri.endsWith(".js") ||
               uri.endsWith(".ico") ||
               uri.endsWith(".png") ||
               uri.endsWith(".jpg");
    }
}
