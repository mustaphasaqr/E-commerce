package com.mustapha.ecommerce.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Async Configuration
 * 
 * Purpose: Enable asynchronous email sending to prevent blocking HTTP requests
 * 
 * Benefits:
 * - Email sending doesn't block user registration/password reset API calls
 * - Better user experience (faster API responses)
 * - Email failures don't cause API errors
 * - Dedicated thread pool for background tasks
 * 
 * Configuration:
 * - Core pool size: 5 threads (handles normal load)
 * - Max pool size: 10 threads (handles spikes)
 * - Queue capacity: 100 (buffers tasks during high load)
 * - Thread name prefix: "async-email-" (for debugging)
 * 
 * Example Usage:
 * ```java
 * @Async("emailTaskExecutor")
 * public void sendWelcomeEmail(String email, String username) {
 *     // This runs in background thread
 * }
 * ```
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    /**
     * Email Task Executor
     * Dedicated thread pool for email sending operations
     */
    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core threads always running (minimum)
        executor.setCorePoolSize(5);
        
        // Maximum threads (scales up under load)
        executor.setMaxPoolSize(10);
        
        // Queue capacity (buffers tasks when all threads busy)
        executor.setQueueCapacity(100);
        
        // Thread naming for debugging
        executor.setThreadNamePrefix("async-email-");
        
        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        // Reject policy: Caller runs (blocks if queue full - prevents email loss)
        executor.setRejectedExecutionHandler(new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());
        
        executor.initialize();
        
        logger.info("✅ Email Task Executor initialized: core={}, max={}, queue={}", 
                   executor.getCorePoolSize(), 
                   executor.getMaxPoolSize(), 
                   executor.getQueueCapacity());
        
        return executor;
    }

    /**
     * Exception Handler for Async Methods
     * Catches and logs exceptions from @Async methods
     */
    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            logger.error("❌ Async method {} threw exception with params: {}", 
                        method.getName(), params, throwable);
            
            // TODO: Store in database for manual retry
            // - Create FailedAsyncTask entity
            // - Store method name, params, error message
            // - Background job can retry later
        };
    }
}
