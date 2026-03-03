package com.mustapha.ecommerce.shared.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) Configuration for E-commerce API Documentation
 * Accessible at: http://localhost:8080/swagger-ui.html
 *                http://localhost:8080/v3/api-docs
 *
 * Features:
 * - Interactive API documentation
 * - JWT authentication support
 * - Request/response examples
 * - API versioning
 * - Server configurations
 */
@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI ecommerceOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .servers(serverList())
                .addSecurityItem(securityRequirement())
                .components(securityComponents());
    }

    private Info apiInfo() {
        return new Info()
                .title("E-commerce Monolith API")
                .version("1.0.0")
                .description("""
                    ## E-commerce RESTful API
                    
                    Comprehensive e-commerce platform with the following features:
                    
                    ### Core Modules:
                    - **Authentication & Authorization**: JWT-based security with role-based access control (OWNER, CUSTOMER)
                    - **Product Management**: Full product lifecycle with inventory tracking
                    - **Order Management**: End-to-end order processing with payment integration
                    - **Cart Management**: Session-based shopping cart with abandonment tracking
                    - **Analytics**: Advanced business intelligence and reporting
                    - **Observability**: Real-time monitoring and health checks
                    
                    ### Security Features:
                    - JWT Bearer Token Authentication
                    - Rate Limiting (100 requests/minute global)
                    - CSRF Protection
                    - XSS Protection
                    - SQL Injection Prevention
                    - Admin IP Whitelisting
                    
                    ### Monitoring:
                    - Prometheus metrics at `/actuator/prometheus`
                    - Health checks at `/actuator/health`
                    - Custom observability endpoints at `/api/observability/*`
                    
                    ### Architecture:
                    - Hexagonal Architecture (Ports & Adapters)
                    - Domain-Driven Design (DDD)
                    - Event-Driven Architecture
                    - Circuit Breakers (Resilience4j)
                    - Distributed Caching (Redis)
                    
                    **Note**: All endpoints require authentication except `/api/auth/*` and `/actuator/*`
                    """)
                .contact(new Contact()
                        .name("E-commerce Support")
                        .email("support@ecommerce.com")
                        .url("https://ecommerce.com/support"))
                .license(new License()
                        .name("Apache 2.0")
                        .url("https://www.apache.org/licenses/LICENSE-2.0.html"));
    }

    private List<Server> serverList() {
        return List.of(
                new Server()
                        .url("http://localhost:8080")
                        .description("Local Development Server"),
                new Server()
                        .url("https://staging.ecommerce.com")
                        .description("Staging Environment"),
                new Server()
                        .url("https://api.ecommerce.com")
                        .description("Production Environment")
        );
    }

    private SecurityRequirement securityRequirement() {
        return new SecurityRequirement().addList("Bearer Authentication");
    }

    private Components securityComponents() {
        return new Components()
                .addSecuritySchemes("Bearer Authentication",
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("""
                                    JWT Authorization header using the Bearer scheme.
                                    
                                    Enter 'Bearer' [space] and then your token in the text input below.
                                    
                                    Example: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
                                    
                                    **How to get a token:**
                                    1. Use POST `/api/auth/login` with valid credentials
                                    2. Copy the `token` from the response
                                    3. Click the "Authorize" button at the top
                                    4. Enter: Bearer <your-token>
                                    """));
    }
}
