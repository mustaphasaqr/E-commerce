package com.mustapha.ecommerce.shared.observability.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Tracing Aspect
 * Responsibility: Distributed tracing
 * Pattern: AOP
 */
@Aspect
@Component
public class TracingAspect {

    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object trace(ProceedingJoinPoint joinPoint) throws Throwable {
        // Add tracing logic (e.g., OpenTelemetry)
        return joinPoint.proceed();
    }
}
