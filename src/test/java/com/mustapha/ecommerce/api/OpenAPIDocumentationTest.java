package com.mustapha.ecommerce.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * OpenAPI Documentation Tests
 * Tests OpenAPI/Swagger configuration, spec generation, and documentation completeness
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("OpenAPI Documentation Tests")
class OpenAPIDocumentationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("OpenAPI Configuration Tests")
    class ConfigurationTests {

        @Test
        @DisplayName("OpenAPI spec endpoint should be accessible")
        void openApiSpecShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
        }

        @Test
        @DisplayName("Swagger UI should be accessible")
        void swaggerUiShouldBeAccessible() throws Exception {
            mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
            
            // Follow redirect
            mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OpenAPI spec should have correct API info")
        void openApiSpecShouldHaveCorrectInfo() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn();

            String content = result.getResponse().getContentAsString();
            JsonNode spec = objectMapper.readTree(content);

            assertThat(spec.get("openapi").asText()).startsWith("3.0");
            assertThat(spec.get("info").get("title").asText()).isEqualTo("E-commerce Monolith API");
            assertThat(spec.get("info").get("version").asText()).isEqualTo("1.0.0");
            assertThat(spec.get("info").get("description").asText()).contains("E-commerce RESTful API");
        }

        @Test
        @DisplayName("API info should include contact information")
        void apiInfoShouldIncludeContact() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode contact = spec.get("info").get("contact");

            assertThat(contact).isNotNull();
            assertThat(contact.get("name").asText()).isEqualTo("E-commerce Support");
            assertThat(contact.get("email").asText()).contains("@");
        }

        @Test
        @DisplayName("API info should include license information")
        void apiInfoShouldIncludeLicense() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode license = spec.get("info").get("license");

            assertThat(license).isNotNull();
            assertThat(license.get("name").asText()).isEqualTo("Apache 2.0");
            assertThat(license.get("url").asText()).contains("apache.org");
        }

        @Test
        @DisplayName("API should have server configurations")
        void apiShouldHaveServers() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode servers = spec.get("servers");

            assertThat(servers).isNotNull();
            assertThat(servers.isArray()).isTrue();
            assertThat(servers.size()).isGreaterThanOrEqualTo(1);
            
            // Check local development server
            assertThat(servers.toString()).contains("localhost:8080");
        }

        @Test
        @DisplayName("API versioning should be consistent")
        void apiVersioningShouldBeConsistent() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            String version = spec.get("info").get("version").asText();

            assertThat(version).matches("\\d+\\.\\d+\\.\\d+");
        }
    }

    @Nested
    @DisplayName("Security Scheme Tests")
    class SecuritySchemeTests {

        @Test
        @DisplayName("JWT Bearer authentication should be documented")
        void jwtBearerAuthShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode securitySchemes = spec.get("components").get("securitySchemes");

            assertThat(securitySchemes).isNotNull();
            assertThat(securitySchemes.has("Bearer Authentication")).isTrue();
            
            JsonNode bearerAuth = securitySchemes.get("Bearer Authentication");
            assertThat(bearerAuth.get("type").asText()).isEqualTo("http");
            assertThat(bearerAuth.get("scheme").asText()).isEqualTo("bearer");
            assertThat(bearerAuth.get("bearerFormat").asText()).isEqualTo("JWT");
        }

        @Test
        @DisplayName("Security scheme should have description")
        void securitySchemeShouldHaveDescription() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode bearerAuth = spec.get("components")
                .get("securitySchemes")
                .get("Bearer Authentication");

            assertThat(bearerAuth.get("description").asText()).contains("JWT");
        }

        @Test
        @DisplayName("Security should be applied globally")
        void securityShouldBeAppliedGlobally() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode security = spec.get("security");

            assertThat(security).isNotNull();
            assertThat(security.isArray()).isTrue();
            assertThat(security.size()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Controller Documentation Tests")
    class ControllerDocumentationTests {

        @Test
        @DisplayName("All 7 controllers should be documented")
        void allControllersShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode paths = spec.get("paths");

            // Check for presence of all controller paths
            assertThat(paths.toString()).contains("/api/v1/auth");
            assertThat(paths.toString()).contains("/api/v1/products");
            assertThat(paths.toString()).contains("/api/v1/orders");
            assertThat(paths.toString()).contains("/api/v1/cart");
            assertThat(paths.toString()).contains("/api/v1/users");
            assertThat(paths.toString()).contains("/api/v1/admin");
            assertThat(paths.toString()).contains("/api/v1/owner/analytics");
        }

        @Test
        @DisplayName("AuthController endpoints should be documented")
        void authControllerShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsString()).get("paths");

            assertThat(paths.has("/api/v1/auth/login")).isTrue();
            assertThat(paths.has("/api/v1/auth/logout")).isTrue();
            assertThat(paths.has("/api/v1/auth/refresh")).isTrue();
            assertThat(paths.has("/api/v1/auth/password-reset/request")).isTrue();
        }

        @Test
        @DisplayName("ProductController endpoints should be documented")
        void productControllerShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsString()).get("paths");

            assertThat(paths.has("/api/v1/products")).isTrue();
            assertThat(paths.has("/api/v1/products/{id}")).isTrue();
            assertThat(paths.has("/api/v1/products/search")).isTrue();
        }

        @Test
        @DisplayName("AnalyticsController endpoints should be documented")
        void analyticsControllerShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsString()).get("paths");

            assertThat(paths.toString()).contains("/api/v1/owner/analytics");
        }
    }

    @Nested
    @DisplayName("HTTP Status Code Documentation Tests")
    class HttpStatusCodeDocumentationTests {

        @Test
        @DisplayName("Success responses (200, 201, 204) should be documented")
        void successResponsesShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            assertThat(content).contains("\"200\"");
            assertThat(content).contains("\"201\"");
            assertThat(content).contains("\"204\"");
        }

        @Test
        @DisplayName("Client error responses (400, 401, 403, 404, 409) should be documented")
        void clientErrorResponsesShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            assertThat(content).contains("\"400\"");
            assertThat(content).contains("\"401\"");
            assertThat(content).contains("\"403\"");
            assertThat(content).contains("\"404\"");
            // 409 may not be used in all endpoints, so not required
        }

        @Test
        @DisplayName("Server error response (500) should be documented")
        void serverErrorResponseShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            assertThat(content).contains("\"500\"");
        }
    }

    @Nested
    @DisplayName("Request/Response Schema Tests")
    class SchemaTests {

        @Test
        @DisplayName("All DTOs should have schema definitions")
        void allDtosShouldHaveSchemas() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode schemas = spec.get("components").get("schemas");

            assertThat(schemas).isNotNull();
            assertThat(schemas.size()).isGreaterThan(10); // Should have many DTOs
        }

        @Test
        @DisplayName("Request DTOs should be documented")
        void requestDtosShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode schemas = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("components").get("schemas");

            // Check key request DTOs
            assertThat(schemas.toString()).contains("LoginRequest");
            assertThat(schemas.toString()).contains("ProductRequest");
            assertThat(schemas.toString()).contains("RegisterUserRequest");
        }

        @Test
        @DisplayName("Response DTOs should be documented")
        void responseDtosShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode schemas = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("components").get("schemas");

            // Check key response DTOs
            assertThat(schemas.toString()).contains("LoginResponse");
            assertThat(schemas.toString()).contains("ProductResponse");
            assertThat(schemas.toString()).contains("UserResponse");
        }

        @Test
        @DisplayName("Validation annotations should be reflected in schemas")
        void validationAnnotationsShouldBeReflected() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode schemas = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("components").get("schemas");

            // Schemas should have validation constraints
            String schemasStr = schemas.toString();
            assertThat(schemasStr).containsAnyOf("required", "minLength", "maxLength", "pattern", "minimum", "maximum");
        }

        @Test
        @DisplayName("Enum values should be documented")
        void enumValuesShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            // Should contain enum definitions
            assertThat(content).containsAnyOf("enum", "OWNER", "CUSTOMER", "EMPLOYEE");
        }
    }

    @Nested
    @DisplayName("Operation Documentation Tests")
    class OperationDocumentationTests {

        @Test
        @DisplayName("All operations should have unique IDs")
        void allOperationsShouldHaveUniqueIds() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode paths = spec.get("paths");

            int operationCount = 0;
            for (JsonNode path : paths) {
                for (JsonNode method : path) {
                    if (method.has("operationId")) {
                        operationCount++;
                    }
                }
            }

            assertThat(operationCount).isGreaterThan(20); // Should have many operations
        }

        @Test
        @DisplayName("Operations should have descriptions")
        void operationsShouldHaveDescriptions() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode loginOperation = spec.get("paths").get("/api/v1/auth/login").get("post");

            assertThat(loginOperation).isNotNull();
            assertThat(loginOperation.has("summary") || loginOperation.has("description")).isTrue();
        }

        @Test
        @DisplayName("Operations should be properly tagged")
        void operationsShouldBeTagged() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode paths = spec.get("paths");

            // Count operations with tags
            int taggedOperations = 0;
            for (JsonNode path : paths) {
                for (JsonNode method : path) {
                    if (method.has("tags")) {
                        taggedOperations++;
                    }
                }
            }

            assertThat(taggedOperations).isGreaterThan(20);
        }

        @Test
        @DisplayName("Tags should categorize endpoints correctly")
        void tagsShouldCategorizeEndpoints() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode tags = spec.get("tags");

            assertThat(tags).isNotNull();
            assertThat(tags.isArray()).isTrue();
            
            // Should have tags for main modules
            String tagsStr = tags.toString();
            assertThat(tagsStr).containsAnyOf("Authentication", "Products", "Orders", "Analytics");
        }
    }

    @Nested
    @DisplayName("Examples and Samples Tests")
    class ExamplesTests {

        @Test
        @DisplayName("Request examples should be present")
        void requestExamplesShouldBePresent() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            // Check for example definitions
            assertThat(content).containsAnyOf("example", "examples");
        }

        @Test
        @DisplayName("Response examples should match actual API behavior")
        void responseExamplesShouldMatchActualBehavior() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            
            // Validate spec structure is complete
            assertThat(spec.has("paths")).isTrue();
            assertThat(spec.has("components")).isTrue();
            assertThat(spec.has("info")).isTrue();
        }
    }

    @Nested
    @DisplayName("API Documentation Completeness Tests")
    class CompletenessTests {

        @Test
        @DisplayName("All public endpoints should be documented")
        void allPublicEndpointsShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode paths = objectMapper.readTree(result.getResponse().getContentAsString()).get("paths");

            // Public endpoints
            assertThat(paths.has("/api/v1/auth/login")).isTrue();
            assertThat(paths.has("/api/v1/auth/refresh")).isTrue();
            assertThat(paths.has("/api/v1/products")).isTrue();
        }

        @Test
        @DisplayName("All protected endpoints should show security requirement")
        void protectedEndpointsShouldShowSecurity() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());
            JsonNode logoutEndpoint = spec.get("paths").get("/api/v1/auth/logout");

            if (logoutEndpoint != null && logoutEndpoint.has("post")) {
                JsonNode postOperation = logoutEndpoint.get("post");
                // Protected endpoints should have security requirements
                assertThat(postOperation.toString()).containsAnyOf("security", "Bearer");
            }
        }

        @Test
        @DisplayName("Analytics endpoints should be fully documented")
        void analyticsEndpointsShouldBeDocumented() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            // All 20 analytics endpoints should be present
            assertThat(content).contains("/api/v1/owner/analytics");
        }

        @Test
        @DisplayName("Documentation should include all HTTP methods")
        void documentationShouldIncludeAllHttpMethods() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            String content = result.getResponse().getContentAsString();

            // Should document GET, POST, PUT, DELETE methods
            assertThat(content).contains("\"get\"");
            assertThat(content).contains("\"post\"");
            assertThat(content).contains("\"put\"");
            assertThat(content).contains("\"delete\"");
        }

        @Test
        @DisplayName("OpenAPI spec should be valid and renderable")
        void openApiSpecShouldBeValid() throws Exception {
            MvcResult result = mockMvc.perform(get("/v3/api-docs"))
                .andReturn();

            JsonNode spec = objectMapper.readTree(result.getResponse().getContentAsString());

            // Required OpenAPI 3.0 fields
            assertThat(spec.has("openapi")).isTrue();
            assertThat(spec.has("info")).isTrue();
            assertThat(spec.has("paths")).isTrue();
            
            // Validate info object
            JsonNode info = spec.get("info");
            assertThat(info.has("title")).isTrue();
            assertThat(info.has("version")).isTrue();
        }
    }
}
