package com.mustapha.ecommerce.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
public class ExponentialBackoffFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, String> redisTemplate;
    private final Environment environment;
    
    private static final String VIOLATION_COUNT_PREFIX = "rate:violations:";
    private static final String PENALTY_PREFIX = "rate:penalty:";
    private static final int BASE_PENALTY_SECONDS = 60;
    private static final int MAX_PENALTY_SECONDS = 3600;
    
    public ExponentialBackoffFilter(RedisTemplate<String, String> redisTemplate, Environment environment) {
        this.redisTemplate = redisTemplate;
        this.environment = environment;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        // Skip exponential backoff in test environment
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = extractIpAddress(request);
        String penaltyKey = PENALTY_PREFIX + clientIp;
        
        if (Boolean.TRUE.equals(redisTemplate.hasKey(penaltyKey))) {
            Long remainingTtl = redisTemplate.getExpire(penaltyKey, TimeUnit.SECONDS);
            response.setStatus(429);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Too many violations. Please try again in %d seconds.\",\"retryAfter\":%d,\"timestamp\":\"%s\"}",
                remainingTtl,
                remainingTtl,
                java.time.Instant.now()
            ));
            return;
        }
        
        filterChain.doFilter(request, response);
        
        if (response.getStatus() == 429) {
            recordViolation(clientIp);
        }
    }
    
    private void recordViolation(String clientIp) {
        String violationKey = VIOLATION_COUNT_PREFIX + clientIp;
        String penaltyKey = PENALTY_PREFIX + clientIp;
        
        Long violationCount = redisTemplate.opsForValue().increment(violationKey);
        if (violationCount == null) {
            violationCount = 1L;
        }
        
        if (violationCount == 1) {
            redisTemplate.expire(violationKey, 1, TimeUnit.HOURS);
        }
        
        if (violationCount >= 3) {
            int penaltySeconds = calculatePenalty(violationCount.intValue());
            redisTemplate.opsForValue().set(penaltyKey, String.valueOf(violationCount), penaltySeconds, TimeUnit.SECONDS);
        }
    }
    
    private int calculatePenalty(int violationCount) {
        int penalty = BASE_PENALTY_SECONDS * (int) Math.pow(2, violationCount - 3);
        return Math.min(penalty, MAX_PENALTY_SECONDS);
    }
    
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
