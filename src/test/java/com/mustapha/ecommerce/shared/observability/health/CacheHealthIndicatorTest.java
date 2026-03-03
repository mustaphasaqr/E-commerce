package com.mustapha.ecommerce.shared.observability.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Cache Health Indicator Tests
 */
@DisplayName("Cache Health Indicator Tests")
class CacheHealthIndicatorTest {

    private CacheManager cacheManager;
    private CacheHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        cacheManager = new ConcurrentMapCacheManager("testCache", "products", "orders");
        healthIndicator = new CacheHealthIndicator(cacheManager);
    }

    @Test
    @DisplayName("Should return UP when cache manager is available")
    void shouldReturnUpWhenCacheManagerIsAvailable() {
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
    }

    @Test
    @DisplayName("Should include cache names in details")
    void shouldIncludeCacheNamesInDetails() {
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getDetails()).containsKey("cacheNames");
        assertThat(health.getDetails().get("cacheCount")).isEqualTo(3);
    }

    @Test
    @DisplayName("Should include cache type in details")
    void shouldIncludeCacheTypeInDetails() {
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getDetails()).containsKey("type");
        assertThat(health.getDetails().get("type")).isEqualTo("In-Memory");
        assertThat(health.getDetails().get("distributed")).isEqualTo(false);
    }

    @Test
    @DisplayName("Should check individual cache status")
    void shouldCheckIndividualCacheStatus() {
        // When
        Health health = healthIndicator.health();
        
        // Then
        assertThat(health.getDetails()).containsKey("caches");
        @SuppressWarnings("unchecked")
        java.util.Map<String, String> cacheStatus = 
            (java.util.Map<String, String>) health.getDetails().get("caches");
        assertThat(cacheStatus).containsKey("testCache");
        assertThat(cacheStatus.get("testCache")).isEqualTo("UP");
    }
}
