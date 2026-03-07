# Integration Test Guide

## Overview

This directory contains test utilities and configurations for integration testing the E-commerce application with real services (MySQL, Redis).

## Test Profiles

### 1. **test** (Default - Unit Tests)
- Uses H2 in-memory database
- No Redis required
- Fast, isolated tests
- **Location:** `src/test/resources/application-test.properties`

### 2. **integration-test** (CI/CD Integration Tests)
- Uses real MySQL database (`ecommerce_test`)
- Requires Redis service
- Tests with actual services
- **Location:** `src/test/resources/application-integration-test.properties`

## Running Tests Locally

### Unit Tests (H2, no services required)
```bash
mvn clean test
```

### Integration Tests (MySQL + Redis required)
```bash
# Start services first
docker-compose -f docker-compose.yml up -d mysql redis

# Run integration tests
mvn clean test -Dspring.profiles.active=integration-test

# Or with Maven Surefire
mvn test -Dtest=\*Test -Dspring.profiles.active=integration-test
```

## Test Authentication Setup

### Using `@TestWithAuthentication` Annotation

For tests that require authentication, use the `@TestWithAuthentication` annotation:

```java
import com.mustapha.ecommerce.test.TestWithAuthentication;

@TestWithAuthentication(
    username = "testuser",
    email = "test@example.com",
    roles = {"ROLE_USER"},
    userId = 1L
)
@Test
void changeEmail_WithValidEmail_Returns200() {
    // test code here
}
```

### Using `@WithMockUser` (Alternative)

Spring's built-in annotation also works:

```java
import org.springframework.security.test.context.support.WithMockUser;

@WithMockUser(username = "testuser", roles = {"USER"})
@Test
void someTest() {
    // test code here
}
```

## Extending BaseIntegrationTest

For integration test classes, extend `BaseIntegrationTest`:

```java
import com.mustapha.ecommerce.test.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class UserIntegrationTest extends BaseIntegrationTest {

    @TestWithAuthentication
    @Test
    void getUser_WithAuth_Returns200() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/1")
                .header("Authorization", "Bearer " + getAuthToken()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(contentType()));
    }

    @Test
    void getUser_NoAuth_Returns401() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/1"))
            .andExpect(status().isUnauthorized());
    }
}
```

## Environment Variables for CI/CD

When running in GitHub Actions, these are automatically set:

```yaml
SPRING_PROFILES_ACTIVE: integration-test
JWT_SECRET: test-secret-key-for-testing-purposes-only-12345
SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/ecommerce_test
SPRING_REDIS_HOST: localhost
```

## Common Issues & Solutions

### 1. Tests Fail with "Unable to connect to Redis"
**Solution:** Make sure Redis is running. For CI/CD, it's automatically provided by GitHub Actions service.

```bash
# Start locally
docker-compose -f docker-compose.yml up -d redis
```

### 2. Tests Fail with "401 Unauthorized"
**Solution:** Add `@TestWithAuthentication` to test methods that need auth:

```java
@TestWithAuthentication
@Test
void testProtectedEndpoint() { }
```

### 3. Database Related Failures
**Solution:** Ensure MySQL is running with correct credentials:

```bash
# Check MySQL is healthy
docker exec $(docker ps -q -f "ancestor=mysql:8.0") mysql -uroot -proot -e "SELECT 1"
```

### 4. Cache Related Issues
**Solution:** If tests expect caching behavior but Redis isn't available:

```java
// Mock the cache in the test
@MockBean
private StringRedisTemplate redisTemplate;
```

## Test Data Management

Test data is automatically seeded from:
- `src/test/resources/test-data/` folder
- Database initialization scripts in migration files

For custom test data setup, use `@Sql` annotation:

```java
@Sql(scripts = "/test-data/users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Test
void testWithPredefinedData() { }
```

## Coverage Reports

After running tests:

```bash
# Generate JaCoCo coverage report
mvn jacoco:report

# View report
open target/site/jacoco/index.html
```

## CI/CD Pipeline Test Flow

The GitHub Actions pipeline runs tests as follows:

1. **Checkout code** ✓
2. **Set up JDK 21** ✓
3. **Start MySQL service** (docker image) ✓
4. **Start Redis service** (docker image) ✓
5. **Wait for services** to be healthy ✓
6. **Run tests** with `integration-test` profile ✓
7. **Generate reports** and upload artifacts ✓

See `.github/workflows/ci-cd.yml` for details.

## Troubleshooting in CI/CD

If tests fail in GitHub Actions but pass locally:

1. Check environment variables are set correctly
2. Verify MySQL/Redis services are healthy
3. Review test output logs in GitHub Actions
4. Use same profile: `integration-test`
5. Check JWT configuration in `application-integration-test.properties`

## Adding New Integration Tests

1. Create test class extending `BaseIntegrationTest`
2. Ensure `@ActiveProfiles("integration-test")` is set (inherited)
3. Use `@TestWithAuthentication` for protected endpoints
4. Use `mockMvc` for HTTP testing
5. Tests are `@Transactional` (auto-rollback)

Example template:

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import com.mustapha.ecommerce.test.BaseIntegrationTest;
import com.mustapha.ecommerce.test.TestWithAuthentication;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class YourIntegrationTest extends BaseIntegrationTest {

    @TestWithAuthentication
    @Test
    void yourTest() throws Exception {
        mockMvc.perform(get("/api/your-endpoint"))
            .andExpect(status().isOk());
    }
}
```
