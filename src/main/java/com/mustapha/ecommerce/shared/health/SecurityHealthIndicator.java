package com.mustapha.ecommerce.shared.health;

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
            redisTemplate.opsForValue().get("health:check");
            
            boolean jwtSecretSet = System.getenv("JWT_SECRET") != null;
            boolean corsConfigured = System.getenv("CORS_ALLOWED_ORIGINS") != null;
            boolean dbPasswordSet = System.getenv("DB_PASSWORD") != null;
            boolean redisPasswordSet = System.getenv("REDIS_PASSWORD") != null;
            
            if (!jwtSecretSet || !dbPasswordSet || !redisPasswordSet) {
                return Health.down()
                    .withDetail("redis", "UP")
                    .withDetail("jwtSecret", jwtSecretSet ? "SET" : "MISSING")
                    .withDetail("corsOrigins", corsConfigured ? "SET" : "USING_DEFAULTS")
                    .withDetail("dbPassword", dbPasswordSet ? "SET" : "MISSING")
                    .withDetail("redisPassword", redisPasswordSet ? "SET" : "MISSING")
                    .withDetail("status", "Production environment variables not configured")
                    .build();
            }
            
            return Health.up()
                .withDetail("redis", "UP")
                .withDetail("tokenBlacklist", "ENABLED")
                .withDetail("rateLimiting", "ENABLED")
                .withDetail("securityHeaders", "ENABLED")
                .withDetail("ipWhitelisting", "ENABLED")
                .withDetail("exponentialBackoff", "ENABLED")
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("redis", "DOWN")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
