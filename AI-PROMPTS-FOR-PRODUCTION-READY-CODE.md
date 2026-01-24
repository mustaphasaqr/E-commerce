# AI Prompts for Production-Ready Code
**Copy-paste prompts to ensure your AI implements all senior engineer concerns**

---

## 📋 Table of Contents
1. [Initial Project Setup Prompt](#1-initial-project-setup-prompt)
2. [Security Implementation Prompt](#2-security-implementation-prompt)
3. [Performance & Resilience Prompt](#3-performance--resilience-prompt)
4. [Observability & Monitoring Prompt](#4-observability--monitoring-prompt)
5. [Testing Strategy Prompt](#5-testing-strategy-prompt)
6. [Database & Data Management Prompt](#6-database--data-management-prompt)
7. [Code Review Checklist Prompt](#7-code-review-checklist-prompt)
8. [Production Deployment Prompt](#8-production-deployment-prompt)

---

## 1. Initial Project Setup Prompt

### When to use:
Starting a new project or reviewing existing architecture

### The Prompt:
```
I'm building a production-ready [describe your app, e.g., "e-commerce REST API"] 
using [tech stack, e.g., "Spring Boot, Java 17, PostgreSQL"].

Expected scale:
- Concurrent users: [e.g., "100-1000"]
- Requests per second: [e.g., "50-500"]
- Data size: [e.g., "100k orders, 10k products"]
- Uptime requirement: [e.g., "99.9%"]

Please help me implement these production concerns for FREE on my laptop:

**1. Project Structure**
- Clean architecture (domain-driven design)
- Package organization for maintainability
- Separation of concerns

**2. Configuration Management**
- Environment-specific configs (dev, test, prod)
- Externalized configuration (no hardcoded values)
- Secret management strategy

**3. Dependency Management**
- Include essential production libraries:
  - Connection pooling (HikariCP)
  - Caching (Caffeine or Redis client)
  - Resilience (Resilience4j)
  - Security (Spring Security)
  - Validation (Hibernate Validator)
  - Metrics (Micrometer)
  - API docs (Springdoc OpenAPI)
  - Database migrations (Flyway)
  - Testing (JUnit 5, Testcontainers)

**4. Build Configuration**
- Maven/Gradle setup with proper plugins
- Code coverage (JaCoCo with 80% minimum)
- Dependency vulnerability scanning (OWASP Dependency Check)
- Code quality checks (optional: SpotBugs, Checkstyle)

Please provide:
1. Complete pom.xml/build.gradle with all dependencies
2. application.properties templates for dev, test, prod
3. Directory structure diagram
4. Initial configuration classes

Focus on FREE, open-source solutions that run on my laptop.
```

---

## 2. Security Implementation Prompt

### When to use:
Implementing authentication, authorization, and security features

### The Prompt:
```
I need to implement production-grade security for my [Spring Boot/other framework] application.

Requirements:
- User authentication (username/password)
- Password hashing with bcrypt/argon2
- JWT token-based sessions
- Role-based access control (RBAC)
- Protection against OWASP Top 10 vulnerabilities

Please implement these security features:

**1. Authentication**
- User registration endpoint with validation
  - Email format validation
  - Password strength requirements (min 8 chars, mix of upper/lower/numbers/symbols)
  - Duplicate email prevention
- Login endpoint returning JWT token
- Password hashing with BCrypt (strength 12)
- Token expiration (15 min access token, 7 day refresh token)

**2. Authorization**
- Role-based access control (USER, ADMIN roles)
- Method-level security annotations
- Endpoint protection based on roles
- User can only access their own data

**3. Security Best Practices**
- SQL injection prevention (use parameterized queries/JPA)
- XSS prevention (input sanitization, output encoding)
- CSRF protection for state-changing operations
- Rate limiting (100 requests/minute per user)
- Secure headers (HSTS, X-Frame-Options, X-Content-Type-Options)
- Input validation on all endpoints

**4. Security Configuration**
- Configure Spring Security with JWT
- Password encoder bean
- Authentication entry point
- CORS configuration (specify allowed origins)
- Disable unnecessary endpoints (/actuator publicly)

**5. Security Tests**
- SQL injection attempt tests
- XSS attack prevention tests
- Unauthorized access tests
- Invalid JWT token tests
- Rate limiting tests

Please provide complete, production-ready code with:
- All security configuration classes
- UserDetailsService implementation
- JWT utility class (generation, validation)
- Security filter chain
- Comprehensive security tests

Use FREE libraries only (Spring Security, JJWT, etc.).
```

---

## 3. Performance & Resilience Prompt

### When to use:
Adding performance optimization and failure handling

### The Prompt:
```
I need to make my [describe service] resilient and performant for production use.

Current architecture:
- [Describe your services, e.g., "Order Service calls Payment API and Inventory API"]
- [Database: PostgreSQL/MySQL/etc.]
- [External dependencies: Stripe, SendGrid, etc.]

Please implement these resilience patterns:

**1. Connection Pooling**
- Configure HikariCP for database connections
- Pool size calculation based on: [cores: 4, expected concurrent users: 100]
- Leak detection enabled
- Connection timeout and max lifetime settings
- Provide configuration for application.properties

**2. Caching Strategy**
- Implement caching for [specify what to cache, e.g., "product catalog, user sessions"]
- Use Caffeine for application-level cache
- Cache TTL: [specify, e.g., "products: 5 min, categories: 1 hour"]
- Cache invalidation on updates
- Cache hit ratio monitoring

**3. Circuit Breaker Pattern**
- Apply circuit breaker to all external API calls:
  - [List external services, e.g., "Payment API, Inventory Service, Email Service"]
- Configuration:
  - Failure threshold: 50% of last 10 calls
  - Wait duration in open state: 30 seconds
  - Slow call threshold: 5 seconds
- Fallback strategies for each service

**4. Retry Logic**
- Implement exponential backoff retry for transient failures
- Max retry attempts: 3
- Initial delay: 100ms, multiplier: 2
- Only retry on: network timeouts, 503 errors, 429 errors
- Do NOT retry on: 400, 401, 404 errors
- Ensure all retried operations are idempotent

**5. Rate Limiting**
- API rate limiting: 100 requests/minute per user
- Use bucket4j or Resilience4j RateLimiter
- Return 429 Too Many Requests when exceeded
- Include Retry-After header

**6. Bulkhead Pattern**
- Separate thread pools for:
  - [List critical operations, e.g., "Payment processing: 10 threads"]
  - [e.g., "Inventory checks: 20 threads"]
  - [e.g., "Email notifications: 5 threads"]
- Prevent one slow service from blocking entire system

**7. Graceful Degradation**
- Define fallback behavior when services unavailable:
  - [e.g., "Payment down → offer cash on delivery"]
  - [e.g., "Inventory unavailable → allow orders with manual verification"]
  - [e.g., "Recommendations down → show popular products"]

Please provide:
- Complete Resilience4j configuration
- HikariCP configuration
- Caffeine cache configuration
- Annotated service methods with @CircuitBreaker, @Retry, @RateLimiter
- Fallback method implementations
- Tests for each resilience pattern

Use only FREE libraries (Resilience4j, Caffeine, Bucket4j).
```

---

## 4. Observability & Monitoring Prompt

### When to use:
Setting up logging, metrics, and monitoring

### The Prompt:
```
I need comprehensive observability for my production application.

Application: [describe your app]
Tech stack: [Spring Boot/other]
Deployment: [local Docker / Heroku / cloud provider]

Please set up the three pillars of observability:

**1. Logging**
- Structured logging with Logback (JSON format)
- Log levels: DEBUG for dev, INFO for prod, ERROR for critical
- Include in every log: timestamp, correlation-id, user-id, request-id
- Log business events:
  - [e.g., "User registration, order placed, payment processed"]
- Security logging:
  - Failed login attempts
  - Authorization failures
  - Suspicious activity
- Performance logging:
  - Slow queries (>100ms)
  - Slow API calls (>200ms)
- Never log sensitive data (passwords, credit cards, PII)

**2. Metrics**
- Enable Spring Boot Actuator with Micrometer
- Expose Prometheus endpoint (/actuator/prometheus)
- Custom business metrics:
  - [e.g., "orders_total counter"]
  - [e.g., "revenue_total counter"]
  - [e.g., "active_users gauge"]
- Track the Four Golden Signals:
  - Latency (p50, p95, p99 response times)
  - Traffic (requests per second)
  - Errors (error rate percentage)
  - Saturation (CPU, memory, connection pool usage)
- Database metrics:
  - Connection pool utilization
  - Query execution time
  - Slow query count

**3. Distributed Tracing** (optional for microservices)
- Correlation ID for request tracking
- MDC (Mapped Diagnostic Context) for thread-local storage
- Trace ID propagation across services

**4. Local Monitoring Stack (FREE)**
- Docker Compose setup for:
  - Prometheus (metrics collection)
  - Grafana (visualization)
  - Elasticsearch (log storage, optional)
  - Kibana (log visualization, optional)
- Grafana dashboards for:
  - Application health (error rate, latency, throughput)
  - JVM metrics (heap, GC, threads)
  - Database metrics (connections, query time)
  - Business metrics (orders, revenue)

**5. Alerting Rules**
- CPU usage > 80% for 5 minutes
- Memory usage > 90%
- Error rate > 1%
- p95 latency > 500ms
- Database connection pool > 90% utilization

Please provide:
- logback-spring.xml configuration
- application.properties for Actuator/Prometheus
- Custom metrics annotations and code
- docker-compose.yml for Prometheus + Grafana
- Prometheus configuration (prometheus.yml)
- Sample Grafana dashboard JSON
- Alert rules configuration

Everything should run FREE locally via Docker.
```

---

## 5. Testing Strategy Prompt

### When to use:
Creating comprehensive test suite

### The Prompt:
```
I need a production-grade test suite for my [Spring Boot/other] application.

Application features:
- [List main features, e.g., "User management, product catalog, order processing, payments"]

Please implement this comprehensive testing strategy:

**1. Unit Tests (70% of tests)**
- Test all business logic in domain layer
- Mock all external dependencies
- Target: >90% coverage for business logic
- Use JUnit 5 + Mockito + AssertJ
- Test cases to include:
  - Happy path scenarios
  - Edge cases (null, empty, boundary values)
  - Error scenarios (invalid input, business rule violations)
  - Validation logic

**2. Integration Tests (20% of tests)**
- Test component interactions
- Use @SpringBootTest with real Spring context
- Test with real database (H2 in-memory or Testcontainers PostgreSQL)
- Test scenarios:
  - [e.g., "Order creation → inventory reservation → payment processing"]
  - Repository layer (CRUD operations)
  - Service layer integration
  - API layer (@WebMvcTest for controllers)

**3. E2E Tests (10% of tests)**
- Test complete user flows via REST API
- Use RestAssured or TestRestTemplate
- Test scenarios:
  - [e.g., "User registers → login → browse products → place order → view order history"]
- Include authentication in E2E tests

**4. Performance Tests**
- Load test: [e.g., "100 concurrent users placing orders"]
- Stress test: [e.g., "500 concurrent users to find breaking point"]
- Spike test: [e.g., "Sudden jump from 10 to 500 users"]
- Soak test: [e.g., "50 users sustained for 10 minutes - check for memory leaks"]
- Target metrics:
  - p95 latency < 200ms
  - Error rate < 0.1%
  - Success rate > 99%

**5. Chaos Tests**
- Database connection failure
- External API timeout/unavailability
- Slow responses (latency injection)
- Network partition scenarios
- Partial service degradation
- Verify circuit breakers activate
- Verify fallback strategies work

**6. Security Tests**
- SQL injection attempts (should fail)
- XSS attack prevention
- CSRF protection
- Authentication bypass attempts (should fail)
- Authorization bypass attempts (should fail)
- Rate limiting enforcement
- Invalid JWT token rejection

**7. Idempotency Tests**
- Duplicate event handling
- Retry safety verification
- State-based idempotency checks

**8. Migration/Compatibility Tests**
- Old data format works with new code
- Database schema migrations don't break existing data
- Backward compatibility verification

**9. Code Coverage**
- Configure JaCoCo with 80% minimum coverage
- Enforce in Maven/Gradle build
- Generate HTML reports
- Fail build if coverage drops below threshold

Please provide:
- Complete test classes for each category
- Test configuration (application-test.properties)
- Testcontainers setup if using PostgreSQL
- JaCoCo Maven/Gradle configuration
- Performance test implementation (JUnit or k6/JMeter scripts)
- Chaos test implementation using mocks or fault injection
- Security test examples

All tests should run FREE locally with no external dependencies.
```

---

## 6. Database & Data Management Prompt

### When to use:
Setting up database, migrations, and data best practices

### The Prompt:
```
I need production-ready database setup and management.

Database: [PostgreSQL/MySQL]
Expected data volume: [e.g., "100k orders, 10k products, 50k users"]
Read/write ratio: [e.g., "80% reads, 20% writes"]

Please implement:

**1. Database Configuration**
- Connection pooling (HikariCP) configured for:
  - Maximum pool size: [calculated based on cores and concurrent users]
  - Minimum idle connections
  - Connection timeout
  - Leak detection
- Transaction management
- Query timeout settings
- Isolation level: [READ_COMMITTED/REPEATABLE_READ]

**2. Schema Design**
- JPA entity design with proper relationships
- Indexing strategy for:
  - Primary keys
  - Foreign keys
  - Frequently queried columns (e.g., email, order_date, status)
- Unique constraints where needed
- NOT NULL constraints
- Default values
- Audit fields (created_at, updated_at, created_by, updated_by)

**3. Database Migrations with Flyway**
- Versioned migration scripts
- Naming convention: V{version}__{description}.sql
- Initial schema creation
- Sample data for development
- Rollback scripts
- Migration testing in CI/CD

**4. Optimistic Locking**
- Add @Version field to entities with concurrent updates
- Handle OptimisticLockException with retry logic
- Test concurrent update scenarios

**5. Query Optimization**
- Use pagination for list endpoints (default page size: 20)
- Eager vs lazy loading strategy
- N+1 query prevention (@EntityGraph or JOIN FETCH)
- Named queries for complex operations
- Projection DTOs for read-only queries

**6. Data Validation**
- Bean Validation annotations on entities
- Custom validators for business rules
- Database constraints match application validation

**7. Repository Pattern**
- Spring Data JPA repositories
- Custom query methods using JPQL
- Native queries for complex reporting
- Specification pattern for dynamic filtering

**8. Testing Strategy**
- Integration tests with real database
- Use Testcontainers for PostgreSQL/MySQL
- Test migrations execute successfully
- Test data access patterns
- Test transaction boundaries
- Test optimistic locking

**9. Backup Strategy** (for production)
- Document backup frequency
- Document restore procedure
- RTO (Recovery Time Objective)
- RPO (Recovery Point Objective)

**10. Data Privacy**
- PII identification
- Data encryption at rest (document which fields)
- Data retention policy
- GDPR compliance considerations (if applicable)

Please provide:
- Complete JPA entity classes with annotations
- Repository interfaces
- Flyway migration scripts (V1, V2 examples)
- application.properties database configuration
- Testcontainers configuration
- Sample integration tests
- Indexing strategy documentation

Everything should work FREE locally with Docker PostgreSQL/MySQL.
```

---

## 7. Code Review Checklist Prompt

### When to use:
Reviewing existing code for production readiness

### The Prompt:
```
Please review my code for production readiness using this comprehensive checklist:

**1. Architecture & Design**
- [ ] Clean separation of concerns (controller → service → repository)
- [ ] Domain logic in domain/service layer, not controllers
- [ ] DTOs used for API layer (not exposing entities directly)
- [ ] Proper exception handling with meaningful error messages
- [ ] No business logic in constructors or getters/setters
- [ ] Dependency injection used (no 'new' keyword for services)

**2. Security**
- [ ] Authentication implemented (username/password, JWT)
- [ ] Passwords hashed with BCrypt/Argon2
- [ ] Authorization checks on all protected endpoints
- [ ] SQL injection prevention (parameterized queries/JPA)
- [ ] XSS prevention (input validation, output encoding)
- [ ] CSRF protection enabled
- [ ] Rate limiting implemented
- [ ] Sensitive data not logged (passwords, tokens, credit cards)
- [ ] HTTPS enforced in production config
- [ ] Security headers configured (HSTS, X-Frame-Options, etc.)

**3. Performance**
- [ ] Connection pooling configured (HikariCP)
- [ ] Caching implemented for frequently accessed data
- [ ] Database queries optimized (indexed columns, no N+1)
- [ ] Pagination for list endpoints
- [ ] Lazy loading for large relationships
- [ ] Resource cleanup (try-with-resources, @PreDestroy)

**4. Resilience**
- [ ] Circuit breakers on external API calls
- [ ] Retry logic with exponential backoff
- [ ] Timeouts configured for external calls
- [ ] Graceful degradation/fallback strategies
- [ ] Bulkhead pattern for resource isolation
- [ ] Idempotency for critical operations

**5. Observability**
- [ ] Structured logging with appropriate levels
- [ ] Correlation IDs for request tracking
- [ ] Business metrics exposed (orders, revenue, etc.)
- [ ] Technical metrics exposed (latency, error rate, etc.)
- [ ] Actuator endpoints secured
- [ ] No sensitive data in logs

**6. Data Management**
- [ ] Database migrations versioned (Flyway)
- [ ] Transactions properly scoped
- [ ] Optimistic locking for concurrent updates
- [ ] Validation on all inputs
- [ ] Indexes on frequently queried columns
- [ ] Database constraints match application rules

**7. Testing**
- [ ] Unit tests for business logic (>90% coverage)
- [ ] Integration tests for component interactions
- [ ] E2E tests for critical user flows
- [ ] Performance tests for concurrent load
- [ ] Security tests for OWASP vulnerabilities
- [ ] Chaos tests for failure scenarios
- [ ] Code coverage >80% overall

**8. Configuration**
- [ ] Externalized configuration (no hardcoded values)
- [ ] Environment-specific configs (dev, test, prod)
- [ ] Secrets not in version control
- [ ] Connection pool sized appropriately
- [ ] Timeouts configured for all external calls

**9. Error Handling**
- [ ] Global exception handler (@ControllerAdvice)
- [ ] Appropriate HTTP status codes
- [ ] Meaningful error messages (not stack traces to users)
- [ ] Failed requests logged with context
- [ ] Validation errors clearly communicated

**10. API Design**
- [ ] RESTful conventions followed
- [ ] Consistent naming (plural nouns for collections)
- [ ] Proper HTTP methods (GET, POST, PUT, DELETE)
- [ ] Versioning strategy (if needed)
- [ ] API documentation (Swagger/OpenAPI)
- [ ] Request/response validation

**11. Code Quality**
- [ ] No code duplication
- [ ] Methods are small and focused (<20 lines ideal)
- [ ] Variables and methods well-named
- [ ] Comments for complex logic only
- [ ] No commented-out code
- [ ] No TODOs in production code

**12. Dependencies**
- [ ] All dependencies up to date
- [ ] No known CVEs in dependencies
- [ ] Dependency scanning in CI/CD
- [ ] Minimal dependency count (avoid bloat)

For each failed check, please:
1. Explain the issue
2. Show the current code
3. Provide the corrected code
4. Explain why the change matters for production

Focus on issues that affect:
- Security vulnerabilities
- Performance bottlenecks  
- Reliability/uptime
- Data integrity
- Maintainability
```

---

## 8. Production Deployment Prompt

### When to use:
Preparing application for production deployment

### The Prompt:
```
I'm ready to deploy my application to production. Please help me prepare:

**Application Details:**
- Tech stack: [Spring Boot, PostgreSQL, etc.]
- Deployment target: [Heroku/Railway/AWS/DigitalOcean/Docker]
- Expected load: [concurrent users, requests/sec]

**Please provide production deployment checklist and implementation:**

**1. Environment Configuration**
- Production application.properties/application.yml
- Environment variables for:
  - Database URL, username, password
  - JWT secret key
  - External API keys (Stripe, SendGrid, etc.)
  - Redis URL (if using)
- Secrets management strategy
- Configuration for:
  - Connection pool (production sizing)
  - Cache settings
  - Logging level (INFO/WARN for prod)
  - Actuator endpoints (disable or secure)

**2. Database Setup**
- Production database provisioning guide
- Connection pool configuration for production load
- Migration execution plan
- Backup strategy
- Monitoring setup

**3. Security Hardening**
- HTTPS enforcement
- Security headers configuration
- CORS allowed origins (production domains only)
- Actuator endpoints secured
- Rate limiting enforced
- Disable dev-only features (H2 console, debug endpoints)

**4. Performance Optimization**
- JVM tuning parameters
  - Heap size settings
  - GC configuration
- Connection pool final tuning
- Cache configuration review
- Enable gzip compression

**5. Monitoring Setup**
- Application Performance Monitoring (APM) integration
  - Options: New Relic free tier, Datadog free tier, etc.
- Log aggregation
  - Options: Logtail free tier, Papertrail, CloudWatch
- Uptime monitoring
  - Options: UptimeRobot, Pingdom free tier
- Health check endpoint
- Readiness and liveness probes (for Kubernetes)

**6. CI/CD Pipeline**
- GitHub Actions workflow for:
  - Build on every commit
  - Run tests
  - Security scanning (OWASP Dependency Check)
  - Code coverage check (fail if <80%)
  - Deploy to staging on merge to develop
  - Deploy to production on merge to main
- Rollback strategy

**7. Deployment Strategy**
- Zero-downtime deployment approach
- Database migration in deployment pipeline
- Health check verification after deployment
- Rollback procedure

**8. Documentation**
- Production runbook:
  - How to deploy
  - How to rollback
  - Common issues and solutions
- Architecture diagram
- API documentation (Swagger URL)
- Monitoring dashboard links

**9. Disaster Recovery**
- Backup schedule and retention
- Restore procedure testing
- RTO (Recovery Time Objective)
- RPO (Recovery Point Objective)
- Incident response plan

**10. Production Smoke Tests**
- Critical path verification after deployment:
  - [e.g., "User can register"]
  - [e.g., "User can login"]
  - [e.g., "User can place order"]
  - [e.g., "Payment processing works"]
- Automated smoke test script

**11. Scaling Plan**
- Horizontal scaling strategy
- Load balancer configuration
- Database read replicas (when needed)
- Caching layer (Redis)
- CDN for static assets

**12. Cost Optimization**
- Use free tiers where available:
  - Heroku free tier / Railway / Render
  - Supabase PostgreSQL free tier
  - Cloudflare free CDN
  - New Relic free APM
- Document monthly cost breakdown
- Alert on unexpected usage spikes

Please provide:
- Production-ready application.properties
- Dockerfile (if containerizing)
- docker-compose.yml for local production-like environment
- GitHub Actions workflow (.github/workflows/deploy.yml)
- Deployment script
- Monitoring setup guide
- Production runbook template

Focus on FREE or low-cost solutions suitable for <1000 users initially.
```

---

## 🎯 How to Use These Prompts

### Step-by-Step Workflow:

**Week 1-2: Foundation**
1. Use **Initial Project Setup Prompt** → Get proper architecture
2. Use **Database & Data Management Prompt** → Set up schema and migrations
3. Implement basic CRUD operations

**Week 3-4: Security**
4. Use **Security Implementation Prompt** → Add authentication/authorization
5. Test security thoroughly

**Week 5-6: Resilience**
6. Use **Performance & Resilience Prompt** → Add circuit breakers, caching, retries
7. Use **Testing Strategy Prompt** → Build comprehensive test suite

**Week 7: Observability**
8. Use **Observability & Monitoring Prompt** → Add logging, metrics, monitoring
9. Set up local Prometheus + Grafana

**Week 8: Review & Deploy**
10. Use **Code Review Checklist Prompt** → Review everything
11. Fix any gaps
12. Use **Production Deployment Prompt** → Deploy to FREE tier

---

## 💡 Pro Tips for Using These Prompts

### 1. Be Specific
Replace placeholders with YOUR actual requirements:
- ❌ "concurrent users: [specify]"
- ✅ "concurrent users: 100-500 initially, may spike to 2000 during sales"

### 2. Iterate
Don't expect perfection in one go:
```
First prompt: "Implement circuit breaker for Payment API"
Follow-up: "Add fallback strategy when circuit is open"
Follow-up: "Add metrics to track circuit state"
Follow-up: "Add tests for circuit breaker behavior"
```

### 3. Ask for Examples
```
"Show me an example of how this circuit breaker would work 
when Payment API is down for 2 minutes, then recovers"
```

### 4. Request Tests
Always end with:
```
"Please include comprehensive tests for this implementation,
including happy path, error scenarios, and edge cases"
```

### 5. Verify Understanding
```
"Before implementing, please explain:
1. Why this approach is production-ready
2. What could go wrong without it
3. How to verify it's working correctly
4. What metrics to monitor"
```

### 6. Local-First
Always specify:
```
"Everything should run FREE on my laptop using Docker.
No paid cloud services required for development."
```

---

## 🚀 Quick Reference: One Prompt for Everything

### The "Make It Production Ready" Mega-Prompt:

```
I have a [describe app] built with [tech stack].

Please review and implement ALL production-readiness concerns:

**MUST HAVES (Critical):**
1. Security: Authentication (JWT), authorization (RBAC), OWASP Top 10 protection
2. Database: Connection pooling, migrations, optimistic locking, indexes
3. Resilience: Circuit breakers, retries with backoff, timeouts, fallbacks
4. Testing: Unit (70%), integration (20%), E2E (10%), >80% coverage
5. Observability: Structured logging, Prometheus metrics, health checks
6. Error Handling: Global exception handler, meaningful errors, no stack traces to users
7. Configuration: Externalized, environment-specific, no secrets in code
8. Validation: Input validation, SQL injection prevention, rate limiting

**SHOULD HAVES (Important):**
9. Performance: Caching (Caffeine/Redis), pagination, query optimization
10. Idempotency: For critical operations (payments, orders)
11. API Documentation: Swagger/OpenAPI
12. CI/CD: GitHub Actions with tests + security scanning

**NICE TO HAVES (If time permits):**
13. Chaos testing: Failure scenario tests
14. Performance testing: Load, stress, spike tests
15. Monitoring stack: Docker Compose with Prometheus + Grafana
16. Deployment: Dockerfile, production runbook

For each area:
1. Review current implementation
2. Identify gaps
3. Provide production-ready code
4. Include tests
5. Explain why it matters

Use only FREE, open-source tools that run locally.
All external services should have free tiers.

Start with the most critical security issues first.
```

---

## 📚 Prompt Library for Common Scenarios

### "I just finished a feature, make it production-ready"
```
I just implemented [feature description].
Please review it for production readiness:
- Security vulnerabilities
- Performance issues
- Missing error handling
- Testing gaps
- Observability needs

Show me what's missing and how to fix it.
```

### "Optimize this slow endpoint"
```
My [endpoint description] is slow (taking [X seconds]).

Please help me optimize:
1. Analyze current implementation for bottlenecks
2. Check database queries (N+1 problem?)
3. Suggest caching strategy
4. Add performance tests to verify improvement

Target: p95 latency < 200ms
```

### "Make this code more resilient"
```
My service calls [external API].
Sometimes it fails and breaks my entire app.

Please implement resilience:
- Circuit breaker
- Retry with exponential backoff
- Timeout
- Fallback strategy
- Tests for failure scenarios

Explain what happens when [external API] is down for 5 minutes.
```

### "Add monitoring to this service"
```
I need to monitor [service name] in production.

Please add:
- Business metrics: [e.g., "orders placed, revenue"]
- Technical metrics: latency, error rate, throughput
- Structured logging with correlation IDs
- Alert rules for problems
- Grafana dashboard

Use Prometheus + Grafana (FREE, local Docker).
```

---

## ✅ Success Criteria

After using these prompts, you should have:

- [ ] Zero security vulnerabilities (OWASP Top 10 protected)
- [ ] >80% code coverage with meaningful tests
- [ ] Circuit breakers on all external dependencies
- [ ] Connection pooling configured properly
- [ ] Structured logging with correlation IDs
- [ ] Prometheus metrics exposed
- [ ] API documentation (Swagger)
- [ ] Database migrations (Flyway)
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] Can deploy to production FREE tier (Heroku/Railway/Render)
- [ ] Monitoring dashboard (Grafana) working locally
- [ ] Performance tests passing (p95 < 200ms)
- [ ] Chaos tests passing (handles failures gracefully)
- [ ] Production runbook documented

---

## 🎓 Learning Path

Use prompts in this order for best results:

```
Week 1: Initial Setup → Database
Week 2: Security → Testing (security tests)
Week 3: Performance → Resilience
Week 4: Observability → More Testing (chaos, performance)
Week 5: Code Review → Fix Gaps
Week 6: Deployment → Production!
```

---

**Remember**: AI is your production-readiness assistant. Use these prompts to ensure nothing is forgotten! 🚀
