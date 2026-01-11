package com.mustapha.ecommerce.shared.observability.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Logging Aspect
 * Responsibility: Cross-cutting logging concern
 * Pattern: AOP
 */
@Aspect
@Component
public class LoggingAspect {

    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.mustapha.ecommerce..application..*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        logger.info("Entering method: {}", methodName);
        
        try {
            Object result = joinPoint.proceed();
            logger.info("Exiting method: {}", methodName);
            return result;
        } catch (Exception e) {
            logger.error("Exception in method: {}", methodName, e);
            throw e;
        }
    }
}
