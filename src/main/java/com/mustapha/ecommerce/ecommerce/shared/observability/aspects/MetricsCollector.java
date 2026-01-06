package com.mustapha.ecommerce.ecommerce.shared.observability.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Metrics Collector Aspect
 * Responsibility: Collect application metrics
 * Pattern: AOP
 */
@Aspect
@Component
public class MetricsCollector {

    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object collectMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            // Record metrics (e.g., to Prometheus)
            return result;
        } catch (Exception e) {
            // Record error metrics
            throw e;
        }
    }
}
