package com.mustapha.ecommerce.config;

import com.mustapha.ecommerce.order.infrastructure.scheduler.AbandonedCartRecoveryScheduler;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;

/**
 * Test configuration to disable scheduled tasks during testing
 */
@TestConfiguration
public class TestSchedulingConfig {

    @MockBean
    private AbandonedCartRecoveryScheduler abandonedCartRecoveryScheduler;
}
