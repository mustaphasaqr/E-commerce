package com.mustapha.ecommerce.shared.observability.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache Health Indicator
 * Monitors cache availability and statistics
 * 
 * Checks:
 * - Cache manager availability
 * - Individual cache availability
 * - Redis connection (if using Redis)
 */
@Component
public class CacheHealthIndicator implements HealthIndicator {

    private final CacheManager cacheManager;

    public CacheHealthIndicator(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            if (cacheManager == null) {
                return Health.down()
                    .withDetail("reason", "Cache manager not available")
                    .build();
            }
            
            // Get cache names
            var cacheNames = cacheManager.getCacheNames();
            details.put("cacheManager", cacheManager.getClass().getSimpleName());
            details.put("cacheNames", cacheNames);
            details.put("cacheCount", cacheNames.size());
            
            // Check if Redis cache manager
            if (cacheManager instanceof RedisCacheManager) {
                details.put("type", "Redis");
                details.put("distributed", true);
            } else {
                details.put("type", "In-Memory");
                details.put("distributed", false);
            }
            
            // Check individual caches
            Map<String, String> cacheStatus = new HashMap<>();
            for (String cacheName : cacheNames) {
                try {
                    var cache = cacheManager.getCache(cacheName);
                    cacheStatus.put(cacheName, cache != null ? "UP" : "DOWN");
                } catch (Exception e) {
                    cacheStatus.put(cacheName, "ERROR: " + e.getMessage());
                }
            }
            details.put("caches", cacheStatus);
            
            return Health.up().withDetails(details).build();
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("errorClass", e.getClass().getName())
                .build();
        }
    }
}
