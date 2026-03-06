package com.mustapha.ecommerce.shared.observability;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * HTTP Metrics Filter
 * Records detailed HTTP metrics for all requests
 * 
 * Metrics Collected:
 * - http.server.requests: Request count, duration, status codes
 * - http.server.requests.active: Currently processing requests
 * - Tagged by: method, uri, status, outcome
 * 
 * Integration:
 * - Micrometer/Prometheus metrics
 * - Grafana dashboards
 * - Custom observability endpoints
 * 
 * Note: Only enabled when MeterRegistry bean is available
 * (disabled in @WebMvcTest contexts where metrics aren't loaded)
 */
@Component
@ConditionalOnBean(MeterRegistry.class)
public class HttpMetricsFilter extends OncePerRequestFilter {

    private final MeterRegistry meterRegistry;

    public HttpMetricsFilter(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                   FilterChain filterChain) throws ServletException, IOException {
        
        long startTime = System.nanoTime();
        String method = request.getMethod();
        String uri = sanitizeUri(request.getRequestURI());
        
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.nanoTime() - startTime;
            int status = response.getStatus();
            String outcome = determineOutcome(status);
            
            // Record metrics with tags
            Timer.builder("http.server.requests")
                    .description("HTTP server request duration")
                    .tag("method", method)
                    .tag("uri", uri)
                    .tag("status", String.valueOf(status))
                    .tag("outcome", outcome)
                    .register(meterRegistry)
                    .record(duration, TimeUnit.NANOSECONDS);
        }
    }

    /**
     * Sanitize URI to avoid metric explosion
     * Replaces path variables with generic placeholders
     */
    private String sanitizeUri(String uri) {
        // Remove IDs and UUIDs to prevent unique metric keys
        return uri
                .replaceAll("/\\d+", "/{id}")
                .replaceAll("/[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}", "/{uuid}");
    }

    /**
     * Determine outcome based on status code
     */
    private String determineOutcome(int status) {
        if (status >= 200 && status < 300) {
            return "SUCCESS";
        } else if (status >= 300 && status < 400) {
            return "REDIRECTION";
        } else if (status >= 400 && status < 500) {
            return "CLIENT_ERROR";
        } else if (status >= 500) {
            return "SERVER_ERROR";
        }
        return "UNKNOWN";
    }
}
