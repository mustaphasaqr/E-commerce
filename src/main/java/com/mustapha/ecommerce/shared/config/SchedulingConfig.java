package com.mustapha.ecommerce.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

/**
 * Scheduling Configuration
 * 
 * Enables background jobs (@Scheduled methods) for:
 * - Token cleanup (password reset, email verification)
 * - Abandoned cart tracking
 * - Cache warming
 * - Report generation
 * - Session cleanup
 * 
 * Pattern: Spring Task Scheduling
 * Layer: INFRASTRUCTURE / CONFIGURATION
 * 
 * Thread Pool Configuration:
 * - 5 threads for scheduled tasks
 * - Thread naming: "scheduler-" prefix for debugging
 * - Graceful shutdown: Waits for tasks to complete
 * 
 * Performance Considerations:
 * - Scheduled tasks run in separate thread pool (not web threads)
 * - Don't block web requests
 * - Use @Transactional for database operations
 */
@Configuration
@EnableScheduling
public class SchedulingConfig implements SchedulingConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);
    
    private static final int POOL_SIZE = 5;
    
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        
        // Thread pool configuration
        taskScheduler.setPoolSize(POOL_SIZE);
        taskScheduler.setThreadNamePrefix("scheduler-");
        taskScheduler.setAwaitTerminationSeconds(60);
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        
        // Error handling
        taskScheduler.setErrorHandler(throwable -> {
            logger.error("⚠️ Scheduled task failed: {}", throwable.getMessage(), throwable);
        });
        
        taskScheduler.initialize();
        taskRegistrar.setTaskScheduler(taskScheduler);
        
        logger.info("✅ Scheduling configuration initialized - Pool size: {}", POOL_SIZE);
    }
}
