package com.mustapha.ecommerce.shared.security.ratelimit;

import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.exception.TooManyRequestsException;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * Production-ready Aspect for enforcing rate limits.
 * 
 * Extracts HttpServletRequest from method arguments or RequestContextHolder.
 * This makes it testable without thread-local dependencies.
 */
@Aspect
@Component
public class RateLimitAspect {

    private static final Logger log = LoggerFactory.getLogger(RateLimitAspect.class);
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final boolean rateLimitingEnabled;

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.rateLimitingEnabled = !environment.getProperty("rate-limiting.enabled", "true").equals("false");
    }

    @Before("@annotation(rateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, RateLimit rateLimit) {
        if (!rateLimitingEnabled) {
            log.debug("Rate limiting is disabled");
            return;
        }
        
        // Try to get request from method arguments first (testable), fall back to RequestContextHolder
        HttpServletRequest request = extractRequest(joinPoint);
        
        String key = buildRateLimitKey(rateLimit, request);
        
        Long count = redisTemplate.opsForValue().increment(key);
        
        if (count == null) {
            count = 1L;
        }
        
        // Set expiration on first request
        if (count == 1) {
            redisTemplate.expire(key, rateLimit.windowSeconds(), TimeUnit.SECONDS);
        }
        
        if (count > rateLimit.maxRequests()) {
            log.warn("Rate limit exceeded for key: {}", key);
            throw new TooManyRequestsException(
                ErrorCode.RATE_LIMIT_EXCEEDED,
                rateLimit.message()
            );
        }
        
        log.debug("Rate limit check passed: {} ({}/{})", key, count, rateLimit.maxRequests());
    }

    /**
     * Extracts HttpServletRequest from JoinPoint arguments or RequestContextHolder.
     * Production-ready: tries explicit parameter first, falls back to thread-local.
     */
    private HttpServletRequest extractRequest(JoinPoint joinPoint) {
        // First, try to find HttpServletRequest in method parameters
        for (Object arg : joinPoint.getArgs()) {
            if (arg instanceof HttpServletRequest) {
                return (HttpServletRequest) arg;
            }
        }
        
        // Fall back to RequestContextHolder (works in production, may not in tests)
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes != null) {
            return attributes.getRequest();
        }
        
        throw new IllegalStateException(
            "No HttpServletRequest found in method parameters or RequestContextHolder. " +
            "Ensure rate-limited methods are HTTP controllers or have HttpServletRequest parameter."
        );
    }

    private String buildRateLimitKey(RateLimit rateLimit, HttpServletRequest request) {
        String identifier = switch (rateLimit.scope()) {
            case IP -> extractIpAddress(request);
            case USER -> extractUserId(request);
            case PARAMETER -> extractParameterValue(request, rateLimit.parameterName());
        };
        return RATE_LIMIT_PREFIX + rateLimit.scope() + ":" + identifier + ":" + request.getRequestURI();
    }

    private String extractParameterValue(HttpServletRequest request, String parameterName) {
        if (parameterName == null || parameterName.isEmpty()) {
            return "unknown";
        }
        // Try to extract from JSON body (for POST)
        try {
            request.getInputStream().mark(0);
            String body = new String(request.getInputStream().readAllBytes());
            request.getInputStream().reset();
            // Very basic JSON extraction (assumes flat JSON)
            String search = "\"" + parameterName + "\":";
            int idx = body.indexOf(search);
            if (idx != -1) {
                int start = body.indexOf('"', idx + search.length());
                int end = body.indexOf('"', start + 1);
                if (start != -1 && end != -1) {
                    return body.substring(start + 1, end);
                }
            }
        } catch (Exception e) {
            // fallback
        }
        // Fallback to request param
        String param = request.getParameter(parameterName);
        return param != null ? param : "unknown";
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractUserId(HttpServletRequest request) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder
            .getContext().getAuthentication();
        
        if (authentication != null && authentication.isAuthenticated()) {
            return authentication.getName();
        }
        
        return "anonymous";
    }
}

