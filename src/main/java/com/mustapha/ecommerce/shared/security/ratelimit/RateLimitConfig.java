package com.mustapha.ecommerce.shared.security.ratelimit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Rate Limiting Configuration and Monitoring
 * 
 * Features:
 * - Externalized configuration via application.properties
 * - Metrics integration (Prometheus/Micrometer)
 * - Rate limit statistics and monitoring
 * - Periodic cleanup of expired keys
 * 
 * Configuration properties:
 * - rate-limiting.enabled: Enable/disable rate limiting (default: true)
 * - rate-limiting.global-limit: Global rate limit for all endpoints (default: 100/minute)
 * - rate-limiting.analytics-limit: Rate limit for analytics endpoints (default: 50/minute)
 * - rate-limiting.auth-limit: Rate limit for auth endpoints (default: 5/minute)
 */
@Configuration
@EnableScheduling
public class RateLimitConfig {

    private static final Logger log = LoggerFactory.getLogger(RateLimitConfig.class);

    private final MeterRegistry meterRegistry;
    private Counter rateLimitExceededCounter;
    private Counter rateLimitCheckedCounter;

    public RateLimitConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        initializeMetrics();
    }

    private void initializeMetrics() {
        rateLimitExceededCounter = Counter.builder("rate_limit_exceeded")
                .description("Number of requests that exceeded rate limit")
                .tag("type", "security")
                .register(meterRegistry);

        rateLimitCheckedCounter = Counter.builder("rate_limit_checked")
                .description("Total number of rate limit checks performed")
                .tag("type", "security")
                .register(meterRegistry);
    }

    /**
     * Rate Limiting Properties
     * Configure via application.properties:
     * 
     * rate-limiting.enabled=true
     * rate-limiting.global-limit=100
     * rate-limiting.analytics-limit=50
     * rate-limiting.auth-limit=5
     * rate-limiting.window-seconds=60
     */
    @Bean
    @ConfigurationProperties(prefix = "rate-limiting")
    public RateLimitProperties rateLimitProperties() {
        return new RateLimitProperties();
    }

    /**
     * Increment counter when rate limit is exceeded
     */
    public void recordRateLimitExceeded(String scope, String identifier) {
        rateLimitExceededCounter.increment();
        log.warn("Rate limit exceeded | scope={} | identifier={}", scope, identifier);
    }

    /**
     * Increment counter for each rate limit check
     */
    public void recordRateLimitCheck() {
        rateLimitCheckedCounter.increment();
    }

    /**
     * Periodic task to log rate limiting statistics
     * Runs every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void logRateLimitStatistics() {
        double exceeded = rateLimitExceededCounter.count();
        double checked = rateLimitCheckedCounter.count();
        double successRate = checked > 0 ? ((checked - exceeded) / checked) * 100 : 100.0;

        log.info("Rate Limiting Statistics | checked={} | exceeded={} | successRate={}%", 
                (long)checked, (long)exceeded, String.format("%.2f", successRate));
    }

    /**
     * Rate Limiting Configuration Properties
     */
    public static class RateLimitProperties {
        private boolean enabled = true;
        private int globalLimit = 100;
        private int analyticsLimit = 50;
        private int authLimit = 5;
        private int windowSeconds = 60;

        // Getters and Setters
        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getGlobalLimit() {
            return globalLimit;
        }

        public void setGlobalLimit(int globalLimit) {
            this.globalLimit = globalLimit;
        }

        public int getAnalyticsLimit() {
            return analyticsLimit;
        }

        public void setAnalyticsLimit(int analyticsLimit) {
            this.analyticsLimit = analyticsLimit;
        }

        public int getAuthLimit() {
            return authLimit;
        }

        public void setAuthLimit(int authLimit) {
            this.authLimit = authLimit;
        }

        public int getWindowSeconds() {
            return windowSeconds;
        }

        public void setWindowSeconds(int windowSeconds) {
            this.windowSeconds = windowSeconds;
        }
    }
}
