package com.mustapha.ecommerce.test;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for integration tests.
 * 
 * Provides:
 * - Integration test configuration
 * - MockMvc for REST testing
 * - Transactional tests (rollback after each test)
 * - Integration test Spring profile activation
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Transactional
public abstract class BaseIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    /**
     * Get JWT token for authenticated requests in tests.
     * Can be overridden in subclasses for custom token generation.
     */
    protected String getAuthToken() {
        return "Bearer test-token";
    }

    /**
     * Helper method to assert specific HTTP status with content type
     */
    protected String contentType() {
        return "application/json";
    }
}
