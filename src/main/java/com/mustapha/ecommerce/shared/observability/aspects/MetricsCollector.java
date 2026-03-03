package com.mustapha.ecommerce.shared.observability.aspects;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Metrics Collector Aspect - Production-Ready
 * Responsibility: Collect performance metrics for all application methods
 * Pattern: AOP (Aspect-Oriented Programming)
 * 
 * Features:
 * - Method execution time tracking
 * - Success/failure counters
 * - Integration with Micrometer/Prometheus
 * - Automatic metric naming based on class/method
 * 
 * Production Benefits:
 * - Identify slow methods automatically
 * - Track error rates by method
 * - Create dashboards in Grafana based on these metrics
 * - No manual instrumentation needed
 */
@Aspect
@Component
public class MetricsCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricsCollector.class);
    private final MeterRegistry meterRegistry;

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    /**
     * Collect metrics for all application layer methods
     * 
     * Metrics exported:
     * - ecommerce.method.execution.time (Timer)
     * - ecommerce.method.execution.count (Counter)
     * - ecommerce.method.error.count (Counter)
     * 
     * Tags:
     * - class: Simple class name
     * - method: Method name
     * - result: success/failure
     */
    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object collectMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        // Extract method information
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethodName = className + "." + methodName;
        
        // Create timer for this method
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            // Execute the actual method
            Object result = joinPoint.proceed();
            
            // Record successful execution
            sample.stop(Timer.builder("ecommerce.method.execution.time")
                .description("Method execution time")
                .tag("class", className)
                .tag("method", methodName)
                .tag("result", "success")
                .register(meterRegistry));
            
            // Increment success counter
            meterRegistry.counter("ecommerce.method.execution.count",
                "class", className,
                "method", methodName,
                "result", "success"
            ).increment();
            
            return result;
            
        } catch (Exception e) {
            // Record failed execution
            sample.stop(Timer.builder("ecommerce.method.execution.time")
                .description("Method execution time")
                .tag("class", className)
                .tag("method", methodName)
                .tag("result", "failure")
                .tag("exception", e.getClass().getSimpleName())
                .register(meterRegistry));
            
            // Increment error counter
            meterRegistry.counter("ecommerce.method.error.count",
                "class", className,
                "method", methodName,
                "exception", e.getClass().getSimpleName()
            ).increment();
            
            logger.debug("Method execution failed: {} | exception={}", fullMethodName, e.getClass().getSimpleName());
            
            throw e;
        }
    }
}
