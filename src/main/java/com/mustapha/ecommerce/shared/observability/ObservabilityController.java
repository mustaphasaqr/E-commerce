package com.mustapha.ecommerce.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Observability Controller
 * Provides comprehensive monitoring and metrics endpoints
 * 
 * Features:
 * - Business metrics summary
 * - System health metrics
 * - Performance metrics
 * - Cache statistics
 * - Circuit breaker status
 */
@RestController
@RequestMapping("/api/v1/observability")
public class ObservabilityController {

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;

    public ObservabilityController(MeterRegistry meterRegistry, 
                                   DataSource dataSource,
                                   RedisConnectionFactory redisConnectionFactory) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    /**
     * GET /api/observability/metrics/business
     * Returns summary of all business metrics
     */
    @GetMapping("/metrics/business")
    public ResponseEntity<Map<String, Object>> getBusinessMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Order metrics
        metrics.put("orders", Map.of(
            "created", getCounterValue("ecommerce.orders.created"),
            "completed", getCounterValue("ecommerce.orders.completed"),
            "cancelled", getCounterValue("ecommerce.orders.cancelled"),
            "failed", getCounterValue("ecommerce.orders.failed"),
            "averageProcessingTime", getTimerValue("ecommerce.orders.processing_time")
        ));
        
        // Payment metrics
        metrics.put("payments", Map.of(
            "successful", getCounterValue("ecommerce.payments.successful"),
            "failed", getCounterValue("ecommerce.payments.failed"),
            "timeout", getCounterValue("ecommerce.payments.timeout"),
            "averageProcessingTime", getTimerValue("ecommerce.payments.processing_time")
        ));
        
        // Shipping metrics
        metrics.put("shipping", Map.of(
            "created", getCounterValue("ecommerce.shipments.created"),
            "delivered", getCounterValue("ecommerce.shipments.delivered"),
            "failed", getCounterValue("ecommerce.shipments.failed")
        ));
        
        // Inventory metrics
        metrics.put("inventory", Map.of(
            "reservations", getCounterValue("ecommerce.inventory.reservations"),
            "reservationsFailed", getCounterValue("ecommerce.inventory.reservations.failed"),
            "restocks", getCounterValue("ecommerce.inventory.restocks")
        ));
        
        // Product metrics
        metrics.put("products", Map.of(
            "searches", getCounterValue("ecommerce.products.searches"),
            "viewed", getCounterValue("ecommerce.products.viewed"),
            "addedToCart", getCounterValue("ecommerce.products.added_to_cart"),
            "averageSearchTime", getTimerValue("ecommerce.products.search_time")
        ));
        
        // Review metrics
        metrics.put("reviews", Map.of(
            "submitted", getCounterValue("ecommerce.reviews.submitted"),
            "approved", getCounterValue("ecommerce.reviews.approved"),
            "rejected", getCounterValue("ecommerce.reviews.rejected")
        ));
        
        // Fraud metrics
        metrics.put("fraud", Map.of(
            "highRisk", getCounterValue("ecommerce.fraud.high_risk"),
            "mediumRisk", getCounterValue("ecommerce.fraud.medium_risk"),
            "lowRisk", getCounterValue("ecommerce.fraud.low_risk")
        ));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/observability/metrics/system
     * Returns system-level metrics (JVM, memory, threads)
     */
    @GetMapping("/metrics/system")
    public ResponseEntity<Map<String, Object>> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        Runtime runtime = Runtime.getRuntime();
        
        // Memory metrics
        Map<String, Object> memory = new HashMap<>();
        memory.put("totalMemoryMB", runtime.totalMemory() / (1024 * 1024));
        memory.put("freeMemoryMB", runtime.freeMemory() / (1024 * 1024));
        memory.put("usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024));
        memory.put("maxMemoryMB", runtime.maxMemory() / (1024 * 1024));
        memory.put("memoryUsagePercent", Math.round(((double)(runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100));
        metrics.put("memory", memory);
        
        // Thread metrics
        Map<String, Object> threads = new HashMap<>();
        threads.put("activeCount", Thread.activeCount());
        threads.put("peakCount", getGaugeValue("jvm.threads.peak"));
        threads.put("daemonCount", getGaugeValue("jvm.threads.daemon"));
        metrics.put("threads", threads);
        
        // CPU metrics
        Map<String, Object> cpu = new HashMap<>();
        cpu.put("processCpuUsage", getGaugeValue("process.cpu.usage"));
        cpu.put("systemCpuUsage", getGaugeValue("system.cpu.usage"));
        cpu.put("availableProcessors", runtime.availableProcessors());
        metrics.put("cpu", cpu);
        
        // Uptime
        metrics.put("uptimeMillis", getGaugeValue("process.uptime"));
        metrics.put("uptimeSeconds", Math.round(getGaugeValue("process.uptime") / 1000));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/observability/metrics/http
     * Returns HTTP request metrics
     */
    @GetMapping("/metrics/http")
    public ResponseEntity<Map<String, Object>> getHttpMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get all HTTP server request meters
        List<Map<String, Object>> httpMetrics = meterRegistry.getMeters().stream()
            .filter(meter -> meter.getId().getName().equals("http.server.requests"))
            .map(meter -> {
                Map<String, Object> metric = new HashMap<>();
                meter.getId().getTags().forEach(tag -> 
                    metric.put(tag.getKey(), tag.getValue())
                );
                
                if (meter instanceof Timer) {
                    Timer timer = (Timer) meter;
                    metric.put("count", timer.count());
                    metric.put("totalTimeMs", timer.totalTime(TimeUnit.MILLISECONDS));
                    metric.put("meanMs", timer.mean(TimeUnit.MILLISECONDS));
                    metric.put("maxMs", timer.max(TimeUnit.MILLISECONDS));
                }
                return metric;
            })
            .collect(Collectors.toList());
        
        metrics.put("requests", httpMetrics);
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/observability/metrics/database
     * Returns database connection pool metrics
     */
    @GetMapping("/metrics/database")
    public ResponseEntity<Map<String, Object>> getDatabaseMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // HikariCP connection pool metrics
        metrics.put("activeConnections", getGaugeValue("hikaricp.connections.active"));
        metrics.put("idleConnections", getGaugeValue("hikaricp.connections.idle"));
        metrics.put("totalConnections", getGaugeValue("hikaricp.connections"));
        metrics.put("pendingThreads", getGaugeValue("hikaricp.connections.pending"));
        metrics.put("connectionTimeout", getGaugeValue("hikaricp.connections.timeout"));
        metrics.put("connectionCreationTime", getTimerValue("hikaricp.connections.creation"));
        metrics.put("connectionAcquisitionTime", getTimerValue("hikaricp.connections.acquire"));
        metrics.put("connectionUsageTime", getTimerValue("hikaricp.connections.usage"));
        
        // Database health check
        metrics.put("healthy", checkDatabaseConnection());
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/observability/metrics/cache
     * Returns cache statistics (Redis)
     */
    @GetMapping("/metrics/cache")
    public ResponseEntity<Map<String, Object>> getCacheMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Cache metrics from Spring Cache
        List<Map<String, Object>> cacheStats = meterRegistry.getMeters().stream()
            .filter(meter -> meter.getId().getName().startsWith("cache."))
            .map(meter -> {
                Map<String, Object> stat = new HashMap<>();
                stat.put("name", meter.getId().getName());
                meter.getId().getTags().forEach(tag -> 
                    stat.put(tag.getKey(), tag.getValue())
                );
                
                if (meter instanceof Counter) {
                    stat.put("count", ((Counter) meter).count());
                }
                
                return stat;
            })
            .collect(Collectors.toList());
        
        metrics.put("cacheStatistics", cacheStats);
        metrics.put("redisHealthy", checkRedisConnection());
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/observability/health/summary
     * Returns comprehensive health summary
     */
    @GetMapping("/health/summary")
    public ResponseEntity<Map<String, Object>> getHealthSummary() {
        Map<String, Object> health = new HashMap<>();
        
        boolean databaseHealthy = checkDatabaseConnection();
        boolean redisHealthy = checkRedisConnection();
        
        health.put("status", (databaseHealthy && redisHealthy) ? "UP" : "DEGRADED");
        health.put("timestamp", System.currentTimeMillis());
        
        Map<String, Object> components = new HashMap<>();
        components.put("database", databaseHealthy ? "UP" : "DOWN");
        components.put("redis", redisHealthy ? "UP" : "DOWN");
        components.put("diskSpace", "UP"); // Assume UP if app is running
        components.put("jvm", "UP");
        
        health.put("components", components);
        health.put("uptimeSeconds", Math.round(getGaugeValue("process.uptime") / 1000));
        
        return ResponseEntity.ok(health);
    }

    /**
     * GET /api/observability/metrics/performance
     * Returns performance metrics (response times, throughput)
     */
    @GetMapping("/metrics/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // HTTP request performance
        double totalRequests = getCounterValue("http.server.requests");
        double uptimeSeconds = getGaugeValue("process.uptime") / 1000;
        double requestsPerSecond = uptimeSeconds > 0 ? totalRequests / uptimeSeconds : 0;
        
        metrics.put("totalRequests", totalRequests);
        metrics.put("requestsPerSecond", Math.round(requestsPerSecond * 100.0) / 100.0);
        
        // Response times
        metrics.put("averageResponseTimeMs", getTimerValue("http.server.requests"));
        metrics.put("orderProcessingTimeMs", getTimerValue("ecommerce.orders.processing_time"));
        metrics.put("paymentProcessingTimeMs", getTimerValue("ecommerce.payments.processing_time"));
        metrics.put("searchQueryTimeMs", getTimerValue("ecommerce.products.search_time"));
        
        // Garbage collection
        Map<String, Object> gc = new HashMap<>();
        gc.put("pauseTimeMs", getTimerValue("jvm.gc.pause"));
        gc.put("memoryAllocatedBytes", getCounterValue("jvm.gc.memory.allocated"));
        gc.put("memoryPromotedBytes", getCounterValue("jvm.gc.memory.promoted"));
        metrics.put("garbageCollection", gc);
        
        return ResponseEntity.ok(metrics);
    }

    // Helper methods
    
    private double getCounterValue(String counterName) {
        Counter counter = meterRegistry.find(counterName).counter();
        return counter != null ? counter.count() : 0.0;
    }
    
    private double getTimerValue(String timerName) {
        Timer timer = meterRegistry.find(timerName).timer();
        return timer != null ? timer.mean(TimeUnit.MILLISECONDS) : 0.0;
    }
    
    private double getGaugeValue(String gaugeName) {
        try {
            return meterRegistry.find(gaugeName).gauge() != null 
                ? meterRegistry.find(gaugeName).gauge().value() 
                : 0.0;
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    private boolean checkDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean checkRedisConnection() {
        try {
            var connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();
            return "PONG".equals(pong);
        } catch (Exception e) {
            return false;
        }
    }
}
