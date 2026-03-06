package com.mustapha.ecommerce.shared.observability;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class RequestIdFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Should generate X-Request-ID header when not provided")
    void generateRequestIdHeader() throws Exception {
        mockMvc.perform(get("/api/v1/products"))
               .andExpect(status().isOk())
               .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("Should preserve custom X-Request-ID header")
    void preserveCustomRequestId() throws Exception {
        String customId = "custom-test-request-id-123";

        mockMvc.perform(get("/api/v1/products")
                       .header("X-Request-ID", customId))
               .andExpect(header().string("X-Request-ID", customId));
    }

    @Test
    @DisplayName("Should generate new ID when header is empty string")
    void generateNewIdWhenHeaderEmpty() throws Exception {
        mockMvc.perform(get("/api/v1/products")
                       .header("X-Request-ID", "   "))
               .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("Should include requestId in all responses")
    void includeRequestIdInAllResponses() throws Exception {
        mockMvc.perform(get("/api/health"))
               .andExpect(header().exists("X-Request-ID"));
    }

    @Test
    @DisplayName("Should generate different IDs for concurrent requests")
    void generateDifferentIdsForConcurrentRequests() throws Exception {
        String requestId1 = mockMvc.perform(get("/api/v1/products"))
                                   .andExpect(status().isOk())
                                   .andReturn()
                                   .getResponse()
                                   .getHeader("X-Request-ID");

        String requestId2 = mockMvc.perform(get("/api/v1/products"))
                                   .andExpect(status().isOk())
                                   .andReturn()
                                   .getResponse()
                                   .getHeader("X-Request-ID");

        assert requestId1 != null && requestId2 != null;
        assert !requestId1.equals(requestId2);
    }
}


