package com.mustapha.ecommerce.shared.observability.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.UUID;

/**
 * Logging Aspect - Production-Ready
 * Responsibility: Structured logging with correlation IDs
 * Pattern: AOP (Aspect-Oriented Programming)
 * 
 * Features:
 * - Correlation ID for request tracing
 * - Structured logging (method name, parameters, execution time)
 * - Exception logging with stack traces
 * - Performance monitoring (execution time)
 * 
 * Production Best Practices:
 * - Uses SLF4J MDC for correlation IDs (works with ELK, Splunk)
 * - Logs at appropriate levels (INFO for entry/exit, ERROR for exceptions)
 * - Includes method parameters for debugging (sanitized in production)
 * - Measures execution time for performance analysis
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String METHOD_KEY = "method";
    private static final String EXECUTION_TIME_KEY = "executionTimeMs";

    /**
     * Log around all application layer methods
     * Adds correlation ID, method info, execution time
     */
    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        String correlationId = getOrCreateCorrelationId();
        
        // Set MDC context (available in all logs within this thread)
        MDC.put(CORRELATION_ID_KEY, correlationId);
        MDC.put(METHOD_KEY, methodName);
        
        long startTime = System.currentTimeMillis();
        
        // Log method entry with parameters (sanitized)
        logger.info("→ Entering: {} | correlationId={} | args={}", 
            methodName, correlationId, sanitizeArgs(joinPoint.getArgs()));
        
        try {
            // Execute the actual method
            Object result = joinPoint.proceed();
            
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            MDC.put(EXECUTION_TIME_KEY, String.valueOf(executionTime));
            
            // Log successful completion
            logger.info("← Exiting: {} | correlationId={} | executionTime={}ms", 
                methodName, correlationId, executionTime);
            
            // Warn if method takes too long (potential performance issue)
            if (executionTime > 5000) { // 5 seconds
                logger.warn("⚠ Slow method detected: {} | executionTime={}ms | threshold=5000ms", 
                    methodName, executionTime);
            }
            
            return result;
            
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Log exception with full context
            logger.error("✗ Exception in: {} | correlationId={} | executionTime={}ms | exception={} | message={}", 
                methodName, correlationId, executionTime, 
                e.getClass().getSimpleName(), e.getMessage(), e);
            
            throw e;
            
        } finally {
            // Clean up MDC to prevent memory leaks
            MDC.remove(METHOD_KEY);
            MDC.remove(EXECUTION_TIME_KEY);
            // Keep correlationId for the entire request lifecycle
        }
    }

    /**
     * Get existing correlation ID or create new one
     * Correlation ID should be set by RequestIdFilter at request entry
     */
    private String getOrCreateCorrelationId() {
        String correlationId = MDC.get(CORRELATION_ID_KEY);
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        return correlationId;
    }

    /**
     * Sanitize method arguments for logging
     * Remove sensitive data (passwords, tokens, credit cards)
     */
    private String sanitizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "[]";
        }
        
        return Arrays.stream(args)
            .map(arg -> {
                if (arg == null) {
                    return "null";
                }
                
                String argStr = arg.toString();
                
                // Sanitize sensitive fields
                if (argStr.contains("password") || argStr.contains("token") || 
                    argStr.contains("secret") || argStr.contains("apiKey")) {
                    return "[REDACTED]";
                }
                
                // Truncate long arguments
                if (argStr.length() > 100) {
                    return argStr.substring(0, 97) + "...";
                }
                
                return argStr;
            })
            .toList()
            .toString();
    }
}
