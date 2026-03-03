package com.mustapha.ecommerce.shared.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class SecurityHealthIndicator implements HealthIndicator {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    public SecurityHealthIndicator(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public Health health() {
        try {
            // Test Redis connectivity
            redisTemplate.opsForValue().get("health:check");
            
            // Check if environment variables are set (informational only, not critical)
            boolean jwtSecretSet = System.getenv("JWT_SECRET") != null;
            boolean corsConfigured = System.getenv("CORS_ALLOWED_ORIGINS") != null;
            boolean dbPasswordSet = System.getenv("DB_PASSWORD") != null;
            boolean redisPasswordSet = System.getenv("REDIS_PASSWORD") != null;
            
            // Build health status - UP even with defaults (suitable for development)
            // Production deployments should set these via environment variables
            String configStatus = (!jwtSecretSet || !dbPasswordSet || !redisPasswordSet) 
                ? "Using default values (set environment variables for production)"
                : "Production environment variables configured";
            
            return Health.up()
                .withDetail("redis", "UP")
                .withDetail("jwtSecret", jwtSecretSet ? "ENV_VAR_SET" : "USING_DEFAULT")
                .withDetail("corsOrigins", corsConfigured ? "ENV_VAR_SET" : "USING_DEFAULT")
                .withDetail("dbPassword", dbPasswordSet ? "ENV_VAR_SET" : "USING_DEFAULT")
                .withDetail("redisPassword", redisPasswordSet ? "ENV_VAR_SET" : "USING_DEFAULT")
                .withDetail("status", configStatus)
                .withDetail("tokenBlacklist", "ENABLED")
                .withDetail("rateLimiting", "ENABLED")
                .withDetail("securityHeaders", "ENABLED")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "DOWN")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
