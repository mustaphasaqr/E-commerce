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

import java.lang.reflect.Method;
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
        
        String key = buildRateLimitKey(joinPoint, rateLimit, request);

        try {
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
        } catch (TooManyRequestsException ex) {
            throw ex;
        } catch (Exception ex) {
            // Fail-open: authentication and core APIs must not return 500 due to cache/network outages.
            log.warn("Rate limit infrastructure unavailable for key {}. Allowing request. Cause: {}", key, ex.getMessage());
        }
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

    private String buildRateLimitKey(JoinPoint joinPoint, RateLimit rateLimit, HttpServletRequest request) {
        String identifier = switch (rateLimit.scope()) {
            case IP -> extractIpAddress(request);
            case USER -> extractUserId(request);
            case PARAMETER -> extractParameterValue(joinPoint, request, rateLimit.parameterName());
        };
        return RATE_LIMIT_PREFIX + rateLimit.scope() + ":" + identifier + ":" + request.getRequestURI();
    }

    private String extractParameterValue(JoinPoint joinPoint, HttpServletRequest request, String parameterName) {
        if (parameterName == null || parameterName.isEmpty()) {
            return "unknown";
        }

        // Prefer extracting from already-bound method arguments (safe, no stream side effects).
        String fromArgs = extractFromJoinPointArgs(joinPoint, parameterName);
        if (fromArgs != null && !fromArgs.isBlank()) {
            return fromArgs;
        }

        // Fallback to request parameter for query/form submissions.
        String param = request.getParameter(parameterName);
        return param != null ? param : "unknown";
    }

    private String extractFromJoinPointArgs(JoinPoint joinPoint, String parameterName) {
        String getterName = "get" + Character.toUpperCase(parameterName.charAt(0)) + parameterName.substring(1);

        for (Object arg : joinPoint.getArgs()) {
            if (arg == null || arg instanceof HttpServletRequest) {
                continue;
            }

            try {
                Method getter = arg.getClass().getMethod(getterName);
                Object value = getter.invoke(arg);
                if (value != null) {
                    return String.valueOf(value);
                }
            } catch (Exception ignored) {
                // Ignore and try next argument or fallback source.
            }
        }

        // Secondary fallback: inspect method parameter names if debug info is available.
        try {
            org.aspectj.lang.reflect.MethodSignature signature =
                (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            String[] names = signature.getParameterNames();
            Object[] values = joinPoint.getArgs();
            if (names != null && values != null) {
                for (int i = 0; i < Math.min(names.length, values.length); i++) {
                    if (parameterName.equals(names[i]) && values[i] != null) {
                        return String.valueOf(values[i]);
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore and return null
        }

        return null;
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

