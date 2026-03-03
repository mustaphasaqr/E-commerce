package com.mustapha.ecommerce.shared.observability.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Tracing Aspect - Production-Ready
 * Responsibility: Distributed tracing for request correlation
 * Pattern: AOP (Aspect-Oriented Programming)
 * 
 * Features:
 * - Request correlation IDs
 * - Trace ID propagation across service boundaries
 * - Integration with logging (SLF4J MDC)
 * - Parent-child span tracking
 * 
 * Production Benefits:
 * - Trace requests across multiple services
 * - Debug production issues with correlation IDs
 * - Integration with distributed tracing systems (Jaeger, Zipkin)
 * - Automatically correlate logs across application layers
 * 
 * Future Enhancements:
 * - Integrate OpenTelemetry for full distributed tracing
 * - Add span attributes (user ID, tenant ID, etc.)
 * - Export traces to Jaeger/Zipkin
 */
@Aspect
@Component
public class TracingAspect {

    private static final Logger logger = LoggerFactory.getLogger(TracingAspect.class);
    private static final String TRACE_ID_KEY = "traceId";
    private static final String SPAN_ID_KEY = "spanId";
    private static final String PARENT_SPAN_ID_KEY = "parentSpanId";

    /**
     * Add tracing context to all application layer methods
     * 
     * Tracing hierarchy:
     * 1. Request enters via Controller → traceId created
     * 2. Controller calls Facade → new spanId (parent = controller span)
     * 3. Facade calls UseCase → new spanId (parent = facade span)
     * 4. UseCase calls Repository → new spanId (parent = usecase span)
     * 
     * All logs within a request will have the same traceId
     */
    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        // Get or create trace ID (should exist from RequestIdFilter)
        String traceId = getOrCreateTraceId();
        
        // Store parent span ID (for hierarchy)
        String parentSpanId = MDC.get(SPAN_ID_KEY);
        
        // Create new span ID for this method
        String spanId = UUID.randomUUID().toString().substring(0, 16); // Short span ID
        
        // Set tracing context
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SPAN_ID_KEY, spanId);
        if (parentSpanId != null) {
            MDC.put(PARENT_SPAN_ID_KEY, parentSpanId);
        }
        
        String methodName = joinPoint.getSignature().toShortString();
        
        logger.debug("[TRACE] Starting span: {} | traceId={} | spanId={} | parentSpanId={}",
            methodName, traceId, spanId, parentSpanId);
        
        try {
            // Execute the actual method
            Object result = joinPoint.proceed();
            
            logger.debug("[TRACE] Completed span: {} | traceId={} | spanId={}",
                methodName, traceId, spanId);
            
            return result;
            
        } catch (Exception e) {
            logger.debug("[TRACE] Failed span: {} | traceId={} | spanId={} | exception={}",
                methodName, traceId, spanId, e.getClass().getSimpleName());
            
            throw e;
            
        } finally {
            // Restore parent span context
            if (parentSpanId != null) {
                MDC.put(SPAN_ID_KEY, parentSpanId);
            } else {
                MDC.remove(SPAN_ID_KEY);
            }
            MDC.remove(PARENT_SPAN_ID_KEY);
            // Keep traceId for entire request
        }
    }

    /**
     * Get existing trace ID or create new one
     * Trace ID should be set by RequestIdFilter at HTTP request entry
     */
    private String getOrCreateTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            // Fallback: create trace ID if not set by filter
            traceId = UUID.randomUUID().toString();
            logger.warn("Trace ID not found in MDC, creating new one: {}", traceId);
        }
        return traceId;
    }
}
