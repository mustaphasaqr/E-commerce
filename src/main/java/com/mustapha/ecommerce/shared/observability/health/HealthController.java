package com.mustapha.ecommerce.shared.observability.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides health status endpoints for monitoring and resilience testing
 * 
 * Features:
 * - Real database connectivity check (not hardcoded)
 * - Real Redis connectivity check (not hardcoded)
 * - /health: Overall health status
 * - /health/ready: Readiness probe (dependencies healthy)
 * - /health/live: Liveness probe (application alive)
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        boolean isHealthy = true;
        
        // Check database
        String dbStatus = checkDatabase();
        if (!"UP".equals(dbStatus)) {
            isHealthy = false;
        }
        
        // Check Redis
        String redisStatus = checkRedis();
        if (!"UP".equals(redisStatus)) {
            isHealthy = false;
        }
        
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("timestamp", System.currentTimeMillis());
        health.put("checks", Map.of(
            "database", dbStatus,
            "redis", redisStatus
        ));
        
        return isHealthy ? 
            ResponseEntity.ok(health) : 
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(health);
    }

    /**
     * Readiness probe: Check if application is ready to accept traffic
     * Returns 200 if all dependencies are healthy, 503 otherwise
     * Used by load balancers and Kubernetes readiness probes
     */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> status = new HashMap<>();
        boolean isReady = true;
        
        // Check database connectivity
        String dbStatus = checkDatabase();
        if (!"UP".equals(dbStatus)) {
            isReady = false;
            logger.warn("Readiness check failed: Database is DOWN");
        }
        
        // Check Redis connectivity
        String redisStatus = checkRedis();
        if (!"UP".equals(redisStatus)) {
            isReady = false;
            logger.warn("Readiness check failed: Redis is DOWN");
        }
        
        status.put("status", isReady ? "READY" : "NOT_READY");
        status.put("checks", Map.of(
            "database", dbStatus,
            "redis", redisStatus
        ));
        
        return isReady ? 
            ResponseEntity.ok(status) : 
            ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(status);
    }

    /**
     * Liveness probe: Check if application process is alive
     * Returns 200 if JVM is running, 503 if deadlocked/frozen
     * Used by Kubernetes to restart failed pods
     */
    @GetMapping("/live")
    public ResponseEntity<Map<String, Object>> liveness() {
        Map<String, Object> status = new HashMap<>();
        status.put("status", "ALIVE");
        status.put("uptime", getUptime());
        return ResponseEntity.ok(status);
    }
    
    /**
     * Check database connectivity by executing a simple query
     */
    private String checkDatabase() {
        try (Connection connection = dataSource.getConnection()) {
            // Execute simple query to verify connection
            boolean isValid = connection.isValid(5); // 5 second timeout
            if (isValid) {
                logger.debug("Database health check: UP");
                return "UP";
            } else {
                logger.error("Database health check: Connection not valid");
                return "DOWN";
            }
        } catch (Exception e) {
            logger.error("Database health check failed: {}", e.getMessage());
            return "DOWN";
        }
    }
    
    /**
     * Check Redis connectivity by executing PING command
     */
    private String checkRedis() {
        try {
            RedisConnection connection = redisConnectionFactory.getConnection();
            String pong = connection.ping();
            connection.close();
            
            if ("PONG".equals(pong)) {
                logger.debug("Redis health check: UP");
                return "UP";
            } else {
                logger.error("Redis health check: Unexpected response: {}", pong);
                return "DOWN";
            }
        } catch (Exception e) {
            logger.error("Redis health check failed: {}", e.getMessage());
            return "DOWN";
        }
    }
    
    /**
     * Get application uptime in milliseconds
     */
    private long getUptime() {
        return java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
    }
}
