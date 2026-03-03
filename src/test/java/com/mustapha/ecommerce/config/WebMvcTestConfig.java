package com.mustapha.ecommerce.config;

import com.mustapha.ecommerce.shared.security.JwtTokenGenerator;
import com.mustapha.ecommerce.shared.security.TokenBlacklistService;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

import static org.mockito.Mockito.mock;

/**
 * Test Configuration for @WebMvcTest
 * 
 * Provides mock beans that are required by filters/interceptors
 * but not part of the web layer under test.
 * 
 * Used by:
 * - PaymentControllerTest
 * - PaymentWebhookControllerTest
 * - Any other @WebMvcTest that needs Redis or method security
 */
@TestConfiguration
@EnableMethodSecurity
public class WebMvcTestConfig {
    
    /**
     * Mock RedisTemplate<String, String> for ExponentialBackoffFilter
     * 
     * The filter uses RedisTemplate for rate limiting, but in controller
     * tests we don't need actual Redis functionality.
     */
    @Bean
    public RedisTemplate<String, String> stringRedisTemplate() {
        return mock(RedisTemplate.class);
    }
    
    /**
     * Mock RedisTemplate<String, Object> for GlobalApiRateLimitFilter
     * 
     * The filter uses RedisTemplate for API rate limiting.
     */
    @Bean
    public RedisTemplate<String, Object> objectRedisTemplate() {
        return mock(RedisTemplate.class);
    }
    
    /**
     * Mock RedisConnectionFactory (required by Spring Data Redis)
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return mock(RedisConnectionFactory.class);
    }
    
    /**
     * Mock JwtTokenGenerator for JwtAuthenticationFilter
     */
    @Bean
    public JwtTokenGenerator jwtTokenGenerator() {
        return mock(JwtTokenGenerator.class);
    }
    
    /**
     * MeterRegistry for HttpMetricsFilter
     * Using SimpleMeterRegistry instead of mock to avoid config() returning null
     */
    @Bean
    public MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }
    
    /**
     * Mock TokenBlacklistService for JwtAuthenticationFilter
     */
    @Bean
    public TokenBlacklistService tokenBlacklistService() {
        return mock(TokenBlacklistService.class);
    }
}
