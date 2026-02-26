# 🔐 Security Architecture: Complete Master Guide

> **دليل شامل للـ Security في أي مشروع**  
> ما يجب معرفته + ما طبقناه + ما لم نطبقه ولماذا + خطة المذاكرة

---

## 📋 Table of Contents

1. [Security Mindset (الطريقة الصحيحة للتفكير)](#1-security-mindset)
2. [Authentication (AuthN) - "Who are you?"](#2-authentication-authn)
3. [Authorization (AuthZ) - "What can you do?"](#3-authorization-authz)
4. [API Security](#4-api-security)
5. [Data Protection](#5-data-protection)
6. [OWASP Top 10 Vulnerabilities](#6-owasp-top-10)
7. [Security Filters & Middleware](#7-security-filters)
8. [Session Management](#8-session-management)
9. [Advanced Security Topics](#9-advanced-topics)
10. [What We Implemented vs What's Missing](#10-implementation-status)
11. [Study Plan (خطة المذاكرة)](#11-study-plan)

---

## 1. Security Mindset (الطريقة الصحيحة للتفكير)

### 🧠 **كيف يفكر Senior/Architect في Security؟**

#### **A. Defense in Depth (دفاع متعدد الطبقات)**

```
هجوم واحد لازم يعدّي على 7 طبقات عشان ينجح:

Layer 1: Network (Firewall, DDoS Protection)
Layer 2: Infrastructure (HTTPS, TLS)
Layer 3: Application Filters (Rate Limiting, IP Whitelist)
Layer 4: Authentication (JWT, Password)
Layer 5: Authorization (Roles, Permissions)
Layer 6: Input Validation (Sanitization)
Layer 7: Data Protection (Encryption)
```

**المبدأ**: لو طبقة واحدة اخترقت، باقي الطبقات تحميك.

---

#### **B. Principle of Least Privilege (أقل صلاحيات ممكنة)**

```java
// ❌ Wrong: Admin has EVERYTHING
if (user.isAdmin()) {
    allowEverything();
}

// ✅ Correct: Each action needs specific permission
@PreAuthorize("hasPermission('DELETE_USER')")
public void deleteUser(String userId) { }

@PreAuthorize("hasPermission('VIEW_FINANCIAL_REPORTS')")
public void viewFinancialReports() { }
```

**المبدأ**: كل user ياخد أقل صلاحيات يحتاجها لشغله فقط.

---

#### **C. Fail Securely (الفشل الآمن)**

```java
// ❌ Wrong: If auth fails, allow access
try {
    validateToken(token);
} catch (Exception e) {
    return "Welcome!"; // DANGEROUS!
}

// ✅ Correct: If anything fails, DENY access
try {
    validateToken(token);
    return allowAccess();
} catch (Exception e) {
    logger.error("Auth failed", e);
    return denyAccess(); // Default DENY
}
```

**المبدأ**: لو حصل أي error، الـ default يكون DENY مش ALLOW.

---

#### **D. Never Trust User Input (مطلقاً لا تثق في user input)**

```java
// ❌ Wrong: Direct SQL
String sql = "SELECT * FROM users WHERE id = " + userId; // SQL Injection!

// ✅ Correct: Parameterized query
String sql = "SELECT * FROM users WHERE id = ?";

// ❌ Wrong: Direct HTML output
return "<div>" + userInput + "</div>"; // XSS Attack!

// ✅ Correct: Encode output
return "<div>" + htmlEncoder.encode(userInput) + "</div>";
```

**المبدأ**: كل حاجة جاية من user تعتبرها هجوم لحد ما تثبت العكس.

---

## 2. Authentication (AuthN) - "Who are you?"

### 🔑 **A. Authentication Strategies (استراتيجيات المصادقة)**

#### **1. Session-Based Authentication (Traditional)**

```
User Login:
1. User sends username + password
2. Server validates credentials
3. Server creates SESSION in memory/database
4. Server sends SESSION_ID in cookie
5. User sends cookie with every request

Storage: Server-side (Redis, Database)
State: STATEFUL (server remembers you)
Scalability: Hard (session store needed)
```

**متى تستخدمها:**
- Traditional web apps (PHP, JSP)
- Admin dashboards
- Single-server deployments

**ما طبقناهاش ليه:**  
❌ احنا عملنا Stateless API (JWT)، مش Traditional Web App.

---

#### **2. Token-Based Authentication (JWT) ⭐ طبقناها**

```
User Login:
1. User sends username + password
2. Server validates credentials
3. Server generates JWT TOKEN (signed)
4. Client stores token (localStorage/cookie)
5. Client sends token in Authorization header

Storage: Client-side
State: STATELESS (server doesn't remember)
Scalability: Easy (no session store needed)
```

**ملفات التطبيق:**
- `JwtTokenProvider.java` - JWT generation & validation
- `JwtAuthenticationFilter.java` - Extract & validate token from header
- `LoginUseCase.java` - Generate JWT after successful login
- `SecurityConfig.java` - Configure JWT filter in chain

```java
// File: JwtTokenProvider.java
public String generateAccessToken(User user) {
    return Jwts.builder()
        .setSubject(user.getId().getValue())
        .claim("email", user.getEmail().getValue())
        .claim("role", user.getRole().name())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
        .signWith(getSigningKey(), SignatureAlgorithm.HS512)
        .compact();
}
```

**Why JWT?**
- ✅ Stateless (no Redis/DB lookup for every request)
- ✅ Scalable (horizontal scaling easy)
- ✅ Mobile-friendly (no cookies needed)
- ✅ Microservices-ready (token contains all info)

---

#### **3. OAuth 2.0 / OpenID Connect (Social Login)**

```
User Login with Google:
1. User clicks "Login with Google"
2. Redirect to Google login page
3. User logs in at Google
4. Google redirects back with AUTHORIZATION CODE
5. Your server exchanges code for ACCESS TOKEN
6. Use token to get user profile from Google
7. Create or login user in your system

Providers: Google, Facebook, GitHub, Microsoft
Protocol: OAuth 2.0 + OpenID Connect
```

**متى تستخدمها:**
- Social login (Login with Google/Facebook)
- Enterprise SSO (Single Sign-On)
- Third-party API access

**ما طبقناهاش ليه:**  
❌ **Not needed for MVP** - محتاجة external provider configuration  
❌ **Cost**: Some providers charge for high volume  
✅ **Future**: نضيفها بعدين لـ better UX

**كيف تضيفها:**
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

---

#### **4. Multi-Factor Authentication (MFA/2FA)**

```
Login Flow with MFA:
1. User enters username + password ✅
2. Server sends OTP via SMS/Email
3. User enters OTP code
4. Server validates OTP ✅
5. Login successful

Methods:
- SMS OTP (Twilio, AWS SNS)
- Email OTP
- Authenticator App (Google Authenticator, Authy)
- Hardware Token (YubiKey)
```

**متى تستخدمها:**
- Banking apps
- Admin access
- High-security apps
- Compliance requirements (PCI-DSS, HIPAA)

**ما طبقناهاش ليه:**  
❌ **Cost**: SMS/Email services cost money (Twilio, SendGrid)  
❌ **Complexity**: Need OTP generation + storage + expiry  
✅ **Future**: Critical for financial/healthcare apps

**كيف تضيفها:**
```java
// 1. Generate OTP
String otp = generateRandomOTP(); // 6 digits
redisTemplate.opsForValue().set("otp:" + userId, otp, 5, TimeUnit.MINUTES);

// 2. Send via SMS
twilioClient.sendSMS(phoneNumber, "Your OTP: " + otp);

// 3. Validate
String storedOtp = redisTemplate.opsForValue().get("otp:" + userId);
if (!otp.equals(storedOtp)) {
    throw new InvalidOtpException();
}
```

---

### 🔐 **B. Password Security (أمان كلمات المرور)**

#### **1. Password Hashing (التشفير) ⭐ طبقناه**

```java
// ❌ NEVER store plain passwords
user.setPassword("MyPassword123"); // DISASTER!

// ❌ NEVER use weak hashing
String hash = MD5(password); // Broken! Can be cracked in seconds

// ✅ ALWAYS use strong hashing (BCrypt, Argon2)
String hash = BCrypt.hashpw(password, BCrypt.gensalt());
```

**ملفات التطبيق:**
- `BCryptPasswordHasher.java` - Password hashing implementation
- `Password.java` (Value Object) - Validates password strength
- `RegisterUserUseCase.java` - Hashes password before saving

```java
// File: BCryptPasswordHasher.java
@Component
public class BCryptPasswordHasher implements PasswordHasher {
    
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    
    @Override
    public String hash(String plainPassword) {
        return encoder.encode(plainPassword);
    }
    
    @Override
    public boolean matches(String plainPassword, String hashedPassword) {
        return encoder.matches(plainPassword, hashedPassword);
    }
}
```

**Why BCrypt?**
- ✅ **Slow by design** (prevents brute force)
- ✅ **Salted automatically** (prevents rainbow table attacks)
- ✅ **Industry standard**
- ✅ **Future-proof** (cost factor can be increased)

**Alternatives:**
- Argon2 (newer, more secure, but not in Spring by default)
- PBKDF2 (older, acceptable)
- Scrypt (good but less common)

---

#### **2. Password Strength Validation ⭐ طبقناه**

```java
// File: Password.java (Value Object)
public static Password fromPlainText(String plainText, PasswordHasher hasher) {
    validateStrength(plainText); // ⬅️ Validation before hashing
    String hashed = hasher.hash(plainText);
    return new Password(hashed);
}

private static void validateStrength(String password) {
    if (password.length() < 8) {
        throw new WeakPasswordException("Password must be at least 8 characters");
    }
    
    // Can add more rules:
    // - Must contain uppercase
    // - Must contain number
    // - Must contain special char
}
```

**Current Rules:**
- ✅ Minimum 8 characters
- ❌ No uppercase requirement (يمكن نضيفها)
- ❌ No number requirement (يمكن نضيفها)
- ❌ No special char requirement (يمكن نضيفها)

**Advanced Password Validation (ما طبقناهش):**
```java
// Check against common passwords list
if (COMMON_PASSWORDS.contains(password)) {
    throw new WeakPasswordException("Password is too common");
}

// Check against breached passwords (HaveIBeenPwned API)
if (isPwnedPassword(password)) {
    throw new WeakPasswordException("Password has been leaked in a data breach");
}
```

---

#### **3. Password Reset Flow ⭐ طبقناه**

**ملفات التطبيق:**
- `PasswordResetToken.java` - Token entity with 24-hour expiry
- `RequestPasswordResetUseCase.java` - Generate reset token
- `ResetPasswordUseCase.java` - Validate token & change password
- `AuthController.java` - `/api/auth/password-reset/request` & `/complete`

```java
// File: RequestPasswordResetUseCase.java
public void execute(String email) {
    User user = userRepository.findByEmail(Email.of(email))
        .orElseThrow(() -> new UserNotFoundException());
    
    // Generate token (UUID with 24h expiry)
    PasswordResetToken token = PasswordResetToken.create(
        user.getId().getValue(),
        email
    );
    
    // Store in Redis (24h TTL)
    passwordResetTokenRepository.save(token);
    
    // Publish event (sends email - async)
    eventPublisher.publishEvent(new PasswordResetRequestedEvent(
        user.getId(),
        email,
        token.getToken()
    ));
}
```

**Security Features:**
- ✅ Token expires in 24 hours
- ✅ One-time use (deleted after reset)
- ✅ Stored in Redis (auto-expiry)
- ✅ Invalidates all refresh tokens after reset

---

## 3. Authorization (AuthZ) - "What can you do?"

### 🛡️ **A. Authorization Models**

#### **1. Role-Based Access Control (RBAC) ⭐ طبقناه**

```java
// File: Role.java (Enum)
public enum Role {
    CUSTOMER,    // Can browse & order
    EMPLOYEE,    // Can manage products
    ADMIN        // Full access
}

// File: User.java
private Role role;

public boolean hasRole(Role requiredRole) {
    return this.role == requiredRole;
}
```

**Access Control Examples:**

```java
// File: SecurityConfig.java - URL-based
http.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")
    .requestMatchers("/api/products/manage/**").hasAnyRole("EMPLOYEE", "ADMIN")
    .requestMatchers("/api/orders/**").authenticated()
);

// File: AdminController.java - Method-level
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(String userId) { }

// File: ProductController.java
@PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
public void updateProduct(String productId, ProductRequest request) { }
```

**Pros:**
- ✅ Simple to implement
- ✅ Easy to understand
- ✅ Good for small/medium apps

**Cons:**
- ❌ Not flexible (can't do "User can edit ONLY their posts")
- ❌ Role explosion in complex apps

---

#### **2. Attribute-Based Access Control (ABAC)**

```java
// ❌ Not implemented (for future)

// Concept: Access based on attributes
@PreAuthorize("hasPermission(#post, 'EDIT')")
public void editPost(Post post) {
    // Can edit if:
    // - User is post author, OR
    // - User is admin, OR
    // - User is moderator AND post is < 24h old
}

// Implementation with custom PermissionEvaluator
public class CustomPermissionEvaluator implements PermissionEvaluator {
    @Override
    public boolean hasPermission(Authentication auth, Object target, Object permission) {
        User user = (User) auth.getPrincipal();
        Post post = (Post) target;
        
        if (user.isAdmin()) return true;
        if (user.getId().equals(post.getAuthorId())) return true;
        if (user.isModerator() && post.isRecent()) return true;
        
        return false;
    }
}
```

**متى تستخدمها:**
- Complex business rules
- Multi-tenant apps
- Fine-grained permissions

**ما طبقناهاش ليه:**  
❌ **RBAC is enough** for e-commerce  
✅ **Future**: Needed for social media, CMS, SaaS apps

---

#### **3. Resource Ownership (نطبقها جزئياً)**

```java
// File: GetUserProfileUseCase.java
public UserResponse execute(String userId) {
    String currentUserId = getCurrentUserId();
    
    // User can only view their own profile (unless admin)
    if (!userId.equals(currentUserId) && !isAdmin()) {
        throw new ForbiddenException("Cannot view other user's profile");
    }
    
    return userRepository.findById(userId);
}
```

**ملفات التطبيق:**
- Partial implementation in use cases
- No centralized ownership checker

**كيف نحسنها:**
```java
// Create OwnershipService
@Service
public class OwnershipService {
    public boolean isOwner(String userId, String resourceId, ResourceType type) {
        return switch (type) {
            case ORDER -> orderRepository.isOwnedBy(resourceId, userId);
            case PROFILE -> userId.equals(resourceId);
            case POST -> postRepository.isAuthor(resourceId, userId);
        };
    }
}

// Use it
@PreAuthorize("@ownershipService.isOwner(principal.id, #orderId, 'ORDER')")
public void cancelOrder(String orderId) { }
```

---

### 🔑 **B. Permission Management**

#### **طبقناه: Role-based only**

```java
// Current: Simple role check
@PreAuthorize("hasRole('ADMIN')")
```

#### **ما طبقناهش: Permission-based**

```java
// Future: Granular permissions
public enum Permission {
    // User permissions
    USER_CREATE,
    USER_READ,
    USER_UPDATE,
    USER_DELETE,
    
    // Product permissions
    PRODUCT_CREATE,
    PRODUCT_UPDATE,
    PRODUCT_DELETE,
    
    // Order permissions
    ORDER_VIEW_ALL,
    ORDER_CANCEL,
    ORDER_REFUND,
    
    // Financial permissions
    FINANCIAL_REPORTS_VIEW,
    FINANCIAL_EXPORT
}

// Role has multiple permissions
public class Role {
    private String name;
    private Set<Permission> permissions;
}

// Check permission instead of role
@PreAuthorize("hasPermission('FINANCIAL_REPORTS_VIEW')")
public void viewFinancialReports() { }
```

**متى محتاجين Permission System:**
- Large enterprise apps
- Complex workflows
- Need to customize access per user
- Audit compliance (who can do what)

---

## 4. API Security

### 🚦 **A. Rate Limiting (تحديد السرعة) ⭐ طبقناه**

**ملفات التطبيق:**
- `GlobalApiRateLimitFilter.java` - 100 req/min per IP, 200 per user
- `ExponentialBackoffFilter.java` - Progressive delays after failed logins
- Uses Redis for counters

```java
// File: GlobalApiRateLimitFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, ...) {
    String ipAddress = getClientIP(request);
    String userId = extractUserId(request);
    
    // Check IP rate limit (100 req/min)
    if (exceedsRateLimit("rate_limit:ip:" + ipAddress, 100)) {
        response.setStatus(429); // Too Many Requests
        return;
    }
    
    // Check user rate limit (200 req/min)
    if (userId != null && exceedsRateLimit("rate_limit:user:" + userId, 200)) {
        response.setStatus(429);
        return;
    }
    
    filterChain.doFilter(request, response);
}

private boolean exceedsRateLimit(String key, int limit) {
    Long count = redisTemplate.opsForValue().increment(key);
    if (count == 1) {
        redisTemplate.expire(key, 1, TimeUnit.MINUTES);
    }
    return count > limit;
}
```

**Types of Rate Limiting:**

| Type | Description | Implemented? |
|------|-------------|--------------|
| **Global** | All endpoints | ✅ Yes (100/min per IP) |
| **Per-User** | Authenticated users | ✅ Yes (200/min per user) |
| **Per-Endpoint** | Login: 5/min, Register: 3/hour | ❌ No (can add) |
| **Sliding Window** | More accurate than fixed window | ❌ No (Redis counter is fixed window) |

**Advanced Rate Limiting (ما طبقناهش):**

```java
// Per-endpoint limits
@RateLimit(requests = 5, duration = "1m")
@PostMapping("/login")
public LoginResponse login(@RequestBody LoginRequest request) { }

// Sliding window algorithm (more fair)
public boolean checkSlidingWindow(String key, int maxRequests, Duration window) {
    long now = System.currentTimeMillis();
    long windowStart = now - window.toMillis();
    
    // Remove old requests
    redisTemplate.opsForZSet().removeRangeByScore(key, 0, windowStart);
    
    // Count requests in window
    Long count = redisTemplate.opsForZSet().count(key, windowStart, now);
    
    if (count < maxRequests) {
        redisTemplate.opsForZSet().add(key, String.valueOf(now), now);
        return true;
    }
    return false;
}
```

---

### 🌐 **B. CORS (Cross-Origin Resource Sharing) ⭐ طبقناه**

**ملفات التطبيق:**
- `SecurityConfig.java` - CORS configuration

```java
// File: SecurityConfig.java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    
    // Production: Only allowed origins
    if (isProd) {
        config.setAllowedOrigins(List.of("https://myapp.com"));
    } else {
        // Development: Allow localhost
        config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:4200"));
    }
    
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);
    
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

**CORS Scenarios:**

```
Scenario 1: Same Origin ✅ Allowed
Frontend: https://myapp.com
API: https://myapp.com/api

Scenario 2: Different Origin ❌ Blocked (unless CORS configured)
Frontend: https://frontend.com
API: https://api.backend.com

Scenario 3: Development ✅ Allowed with config
Frontend: http://localhost:3000 (React)
API: http://localhost:8080 (Spring Boot)
```

---

### 🛡️ **C. CSRF Protection (Cross-Site Request Forgery)**

**ما طبقناهش ليه:**  
❌ **Not needed for JWT APIs** (stateless)  
✅ **Only needed for session-based cookies**

```java
// File: SecurityConfig.java
.csrf(csrf -> csrf.disable()) // ⬅️ Disabled for JWT API
```

**متى محتاج CSRF:**
- Session-based authentication (cookies)
- Traditional web apps (forms)
- Browser submits requests automatically

**Why JWT doesn't need CSRF:**
```
❌ Cookie-based (vulnerable):
Browser automatically sends cookies with EVERY request
Attacker can trick browser to send malicious request

✅ JWT Token-based (safe):
Token stored in localStorage
Must be MANUALLY added to Authorization header
Browser WON'T send it automatically
```

**If you use cookies for JWT (hybrid approach):**
```java
// Enable CSRF for cookie-based JWT
.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
)
```

---

### 🔒 **D. Security Headers ⭐ طبقناه**

**ملفات التطبيق:**
- `SecurityHeadersFilter.java` - Adds all security headers

```java
// File: SecurityHeadersFilter.java
@Override
protected void doFilterInternal(HttpServletRequest request, 
                                HttpServletResponse response, 
                                FilterChain filterChain) {
    
    // Prevent clickjacking (iframe embedding)
    response.setHeader("X-Frame-Options", "DENY");
    
    // Prevent MIME sniffing
    response.setHeader("X-Content-Type-Options", "nosniff");
    
    // Enable XSS protection in browser
    response.setHeader("X-XSS-Protection", "1; mode=block");
    
    // Force HTTPS (after first visit)
    response.setHeader("Strict-Transport-Security", 
        "max-age=31536000; includeSubDomains");
    
    // Content Security Policy (prevent XSS)
    response.setHeader("Content-Security-Policy", 
        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'");
    
    // Referrer policy
    response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
    
    // Permissions policy
    response.setHeader("Permissions-Policy", 
        "geolocation=(), microphone=(), camera=()");
    
    filterChain.doFilter(request, response);
}
```

**What Each Header Does:**

| Header | Protection Against | Example Attack |
|--------|-------------------|----------------|
| `X-Frame-Options` | Clickjacking | Invisible iframe over login button |
| `X-Content-Type-Options` | MIME sniffing | Upload .jpg that's actually .js |
| `X-XSS-Protection` | XSS attacks | `<script>alert('hacked')</script>` |
| `Strict-Transport-Security` | Man-in-the-middle | HTTP → HTTPS downgrade |
| `Content-Security-Policy` | XSS, injection | Inline scripts, external scripts |
| `Referrer-Policy` | Info leakage | URLs leaked in Referer header |
| `Permissions-Policy` | Feature abuse | Unwanted camera/mic access |

---

## 5. Data Protection (حماية البيانات)

### 🔐 **A. Encryption**

#### **1. Data in Transit (نقل البيانات) ⭐ طبقناه**

```
Client → Server: HTTPS (TLS 1.2+)

HTTP Request:
GET /api/users/123
Authorization: Bearer eyJhbGc...

Encrypted during transmission ✅
Can't be intercepted by attacker ✅
```

**ملفات التطبيق:**
- `application.properties` - HTTPS config (production)
- `SecurityHeadersFilter.java` - HSTS header forces HTTPS

```properties
# application-prod.properties
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore.p12
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
```

**ما طبقناهش في Development:**  
❌ Local development uses HTTP (not HTTPS)  
✅ Production MUST use HTTPS (with SSL certificate)

**How to get SSL Certificate:**
- Let's Encrypt (FREE)
- CloudFlare (FREE)
- AWS Certificate Manager (FREE for AWS resources)
- Commercial CA (Paid)

---

#### **2. Data at Rest (تخزين البيانات)**

##### **A. Password Hashing ⭐ طبقناه**

```java
// File: Password.java
private final String hashedValue; // ⬅️ NEVER store plain password

// BCrypt hashing
String hashed = "$2a$10$N9qo8uLOickgx2ZMRZoMye..."; // 60 chars
```

**Strong:** BCrypt with cost factor 10  
**Storage:** MySQL `password_hash` column (VARCHAR 60)

---

##### **B. Sensitive Data Encryption (ما طبقناهش)**

```java
// ❌ Current: Sensitive data stored in plain text
user.setPhoneNumber("+201234567890"); // Plain text in DB
order.setCreditCardNumber("1234-5678-9012-3456"); // Plain text!

// ✅ Should be: Encrypted before storage
String encrypted = aesEncryptor.encrypt(phoneNumber, secretKey);
user.setPhoneNumber(encrypted); // "k8Js9mP3..." stored in DB
```

**What Should Be Encrypted:**
- Credit card numbers (PCI-DSS requirement)
- Social security numbers
- Bank account numbers
- Health records (HIPAA requirement)
- Personal addresses (GDPR compliance)

**How to Implement:**

```java
@Component
public class AESEncryptor {
    
    @Value("${encryption.secret.key}")
    private String secretKey; // 256-bit key from env var
    
    public String encrypt(String plainText) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(), "AES");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, generateIV());
        
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, gcmSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes());
        
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public String decrypt(String encryptedText) throws Exception {
        // Reverse process
    }
}

// Use in entity
@Entity
public class CreditCard {
    
    @Convert(converter = EncryptedStringConverter.class)
    private String cardNumber; // Auto-encrypted in DB
    
    @Convert(converter = EncryptedStringConverter.class)
    private String cvv;
}
```

**ما طبقناهاش ليه:**  
❌ **Not needed for basic e-commerce** (we don't store credit cards)  
❌ **Complexity**: Key management, rotation, backup  
✅ **Future**: Required for payment processing, PCI-DSS compliance

---

#### **3. Database Encryption**

##### **A. Column-Level Encryption (ما طبقناهش)**

```sql
-- MySQL supports Transparent Data Encryption (TDE)
ALTER TABLE users ENCRYPTION='Y';
```

**ما طبقناهش ليه:**  
❌ **Performance overhead**  
❌ **Not critical for non-financial data**  
✅ **Future**: Healthcare, banking apps

---

##### **B. Connection Encryption ⭐ طبقناه (Production)**

```properties
# application-prod.properties
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce?useSSL=true&requireSSL=true
```

**Encrypts:** MySQL client ↔ MySQL server communication  
**Prevents:** Network sniffing, man-in-the-middle attacks

---

### 🛡️ **B. Data Masking & Redaction**

#### **طبقناه جزئياً: Logging**

```java
// File: LoggingAspect.java
@Around("@annotation(LogExecutionTime)")
public Object logExecutionTime(ProceedingJoinPoint joinPoint) {
    Object[] args = joinPoint.getArgs();
    
    // ❌ DON'T log sensitive data
    // logger.info("Password: {}", password); // NEVER!
    
    // ✅ Mask sensitive fields
    logger.info("Login attempt: email={}", maskEmail(email));
    
    return joinPoint.proceed();
}

private String maskEmail(String email) {
    // user@example.com → u***@example.com
    return email.replaceAll("(?<=.{1}).(?=.*@)", "*");
}
```

#### **ما طبقناهش: API Response Masking**

```java
// ❌ Current: Returns full data
{
    "userId": "123",
    "email": "user@example.com",
    "phoneNumber": "+201234567890"
}

// ✅ Should mask for non-owners
{
    "userId": "123",
    "email": "u***@example.com",
    "phoneNumber": "+2012****7890"
}
```

---

## 6. OWASP Top 10 Vulnerabilities (أشهر 10 ثغرات)

### 🎯 **A1: Broken Access Control ⭐ طبقنا الحماية**

**What it is:**  
User can access data/functions they shouldn't.

**Examples:**
```
❌ View another user's order
GET /api/orders/456 (not your order)

❌ Delete user without admin permission
DELETE /api/users/789

❌ Change product price without employee role
PUT /api/products/123 { "price": 0.01 }
```

**Our Protection:**

```java
// File: SecurityConfig.java - URL-based control
.requestMatchers("/api/admin/**").hasRole("ADMIN")
.requestMatchers("/api/orders/**").authenticated()

// File: OrderController.java - Resource ownership
public OrderResponse getOrder(String orderId) {
    String currentUserId = getCurrentUserId();
    Order order = orderRepository.findById(orderId);
    
    if (!order.getCustomerId().equals(currentUserId) && !isAdmin()) {
        throw new ForbiddenException(); // ✅ Protected
    }
    
    return toResponse(order);
}

// File: AdminController.java - Role-based
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(String userId) { }
```

**Test Coverage:**  
✅ AuthorizationTest.java - 40+ tests

---

### 🎯 **A2: Cryptographic Failures ⭐ طبقنا الحماية**

**What it is:**  
Exposing sensitive data due to weak/missing encryption.

**Examples:**
```
❌ Store passwords in plain text
❌ Use weak hashing (MD5, SHA1)
❌ No HTTPS (man-in-the-middle attack)
❌ Hardcoded encryption keys in code
```

**Our Protection:**

```java
// ✅ Strong password hashing (BCrypt)
// File: BCryptPasswordHasher.java
BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

// ✅ Secrets in environment variables (not code)
@Value("${jwt.secret}") // From application.properties
private String jwtSecret;

// ✅ HTTPS in production
// File: application-prod.properties
server.ssl.enabled=true

// ✅ Secure token generation (UUID v4, not predictable)
String token = UUID.randomUUID().toString();
```

**What's Missing:**  
❌ Column-level encryption for sensitive data  
❌ Database encryption at rest  
✅ Good for MVP, upgrade for PCI-DSS/HIPAA

---

### 🎯 **A3: Injection (SQL, NoSQL, Command) ⭐ طبقنا الحماية**

**What it is:**  
Attacker injects malicious code through input.

**SQL Injection Example:**
```java
// ❌ VULNERABLE: Direct string concatenation
String sql = "SELECT * FROM users WHERE email = '" + email + "'";

// Attacker sends: email = "' OR '1'='1"
// Result: SELECT * FROM users WHERE email = '' OR '1'='1'
// Returns ALL users! 🔥
```

**Our Protection:**

```java
// ✅ JPA prevents SQL injection automatically
// File: UserJpaRepository.java (Spring Data)
public interface UserJpaRepository extends JpaRepository<UserEntity, String> {
    Optional<UserEntity> findByEmail(String email); // ✅ Parameterized query
}

// Generated SQL (safe):
// SELECT * FROM users WHERE email = ?
// Parameter: "user@example.com"
```

**Input Sanitization:**

```java
// File: InputSanitizationService.java
@Service
public class InputSanitizationService {
    private final Encoder htmlEncoder = Encoders.forHtml();
    
    public String sanitize(String input) {
        if (input == null) return null;
        
        // Remove SQL injection attempts
        String cleaned = input.replaceAll("('|--|;|/\\*|\\*/)", "");
        
        // Encode HTML entities (XSS prevention)
        cleaned = htmlEncoder.encode(cleaned);
        
        return cleaned;
    }
}

// Custom validators
@NoScriptTag // ⬅️ Blocks <script> tags
@NoSqlInjection // ⬅️ Blocks SQL keywords
public record ProductRequest(
    String name,
    String description
) {}
```

**Test Coverage:**  
✅ EdgeCasesTest.java - SQL injection attempts  
✅ SecurityVulnerabilityTest.java

---

### 🎯 **A4: Insecure Design (تصميم غير آمن)**

**What it is:**  
Missing security requirements in design phase.

**Examples:**
```
❌ No rate limiting → DDoS attack possible
❌ No token expiry → Stolen token works forever
❌ No password complexity → Easy to guess
❌ No MFA for admin → One password breach = game over
```

**Our Protection:**

| Requirement | Implemented? | File |
|-------------|--------------|------|
| Rate limiting | ✅ Yes | GlobalApiRateLimitFilter.java |
| Token expiry | ✅ Yes (15 min) | JwtTokenProvider.java |
| Password rules | ✅ Basic | Password.java |
| MFA for admin | ❌ No | Future enhancement |
| Session timeout | ✅ Yes (24h) | LoginSession.java |
| Failed login limit | ✅ Yes | ExponentialBackoffFilter.java |

**What's Missing (Design Phase):**  
❌ Threat modeling (STRIDE analysis)  
❌ Security user stories  
❌ Abuse cases (what attackers might do)  
✅ Good for MVP, critical for enterprise

---

### 🎯 **A5: Security Misconfiguration ⭐ طبقنا الحماية**

**What it is:**  
Default configs, exposed debug info, unnecessary features.

**Examples:**
```
❌ Debug mode enabled in production
❌ Default passwords (admin/admin)
❌ Exposed error stack traces
❌ Unnecessary open ports
❌ Directory listing enabled
```

**Our Protection:**

```properties
# File: application-prod.properties

# ✅ Disable debug
debug=false
logging.level.root=WARN

# ✅ Hide Spring Boot banner
spring.main.banner-mode=off

# ✅ Hide error details
server.error.include-stacktrace=never
server.error.include-message=never

# ✅ Actuator secured
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=when-authorized

# ✅ Secrets from environment
jwt.secret=${JWT_SECRET}
spring.datasource.password=${DB_PASSWORD}
```

```java
// File: GlobalExceptionHandler.java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleException(Exception e) {
    
    // ❌ DON'T expose stack trace
    // return new ErrorResponse(e.getMessage(), e.getStackTrace());
    
    // ✅ Generic error message
    logger.error("Unexpected error", e); // Log internally
    return ResponseEntity
        .status(500)
        .body(new ErrorResponse("Internal server error", null));
}
```

**Test Coverage:**  
✅ ApiResponseValidationTest.java

---

### 🎯 **A6: Vulnerable Components (مكتبات بها ثغرات)**

**What it is:**  
Using outdated libraries with known vulnerabilities.

**Examples:**
```
❌ Spring Boot 2.0 (has CVE-2022-12345)
❌ Jackson 2.9 (has deserialization vulnerability)
❌ Log4j 2.14 (Log4Shell vulnerability)
```

**Our Protection:**

```xml
<!-- File: pom.xml -->
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.1</version> <!-- ✅ Latest version -->
</parent>

<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version> <!-- ✅ Latest -->
</dependency>
```

**How to Check Vulnerabilities:**

```bash
# Maven dependency check
mvn org.owasp:dependency-check-maven:check

# Output: CVE report
```

**Best Practices:**
- ✅ Update dependencies monthly
- ✅ Subscribe to security advisories
- ✅ Use Dependabot (GitHub) for auto-updates
- ✅ Run OWASP Dependency-Check in CI/CD

**ما طبقناهشغيره:**  
❌ Automated dependency scanning in CI/CD  
❌ Software Composition Analysis (SCA) tool  
✅ Manual updates (good for small projects)

---

### 🎯 **A7: Authentication Failures ⭐ طبقنا الحماية**

**What it is:**  
Broken authentication mechanisms.

**Examples:**
```
❌ Brute force attacks (unlimited login attempts)
❌ Weak passwords allowed
❌ Session fixation attacks
❌ No logout functionality
❌ Tokens don't expire
```

**Our Protection:**

```java
// ✅ Rate limiting on login
// File: GlobalApiRateLimitFilter.java
if (request.getRequestURI().contains("/login")) {
    // Extra strict: 5 attempts per minute per IP
}

// ✅ Exponential backoff after failed logins
// File: ExponentialBackoffFilter.java
int failedAttempts = getFailedAttempts(ipAddress);
if (failedAttempts > 3) {
    int delaySeconds = (int) Math.pow(2, failedAttempts - 3); // 2, 4, 8, 16...
    Thread.sleep(delaySeconds * 1000);
}

// ✅ Strong password requirements
// File: Password.java
if (password.length() < 8) {
    throw new WeakPasswordException();
}

// ✅ Token expiry
// File: JwtTokenProvider.java
.setExpiration(new Date(System.currentTimeMillis() + 900000)) // 15 min

// ✅ Token blacklist on logout
// File: LogoutUseCase.java
tokenBlacklistService.blacklist(token, expiryTime);

// ✅ Refresh token rotation (one-time use)
// File: RefreshTokenUseCase.java
refreshTokenRepository.delete(oldToken); // Can't reuse
```

**Test Coverage:**  
✅ AuthControllerIntegrationTest.java - 22 tests  
✅ RateLimitingSecurityTest.java

---

### 🎯 **A8: Software and Data Integrity Failures**

**What it is:**  
Code/data modified without verification.

**Examples:**
```
❌ Download dependency without checksum verification
❌ Auto-update without signature verification
❌ CI/CD pipeline has no integrity checks
❌ Unsigned JARs in production
```

**طبقناه جزئياً:**

```xml
<!-- File: pom.xml -->
<!-- ✅ Maven Central repository (verified sources) -->
<repositories>
    <repository>
        <id>central</id>
        <url>https://repo.maven.apache.org/maven2</url>
    </repository>
</repositories>
```

**ما طبقناهش:**  
❌ JAR signing  
❌ Build reproducibility  
❌ Supply chain security (SBOM)  
✅ Low priority for internal projects

**How to Improve:**
```bash
# Sign JAR files
jarsigner -keystore keystore.jks -signedjar signed.jar app.jar mykey

# Verify signature
jarsigner -verify signed.jar
```

---

### 🎯 **A9: Security Logging & Monitoring ⭐ طبقنا جزئياً**

**What it is:**  
Can't detect/respond to attacks.

**Our Logging:**

```java
// File: LoggingAspect.java (AOP)
@Around("execution(* com.mustapha.ecommerce.user.auth..*(..))")
public Object logAuthOperations(ProceedingJoinPoint joinPoint) {
    
    logger.info("🔐 Auth operation: {}", joinPoint.getSignature());
    
    try {
        Object result = joinPoint.proceed();
        logger.info("✅ Success");
        return result;
    } catch (Exception e) {
        logger.error("❌ Failed: {}", e.getMessage());
        throw e;
    }
}

// File: LoginUseCase.java
logger.info("✅ Login successful: userId={}, ip={}",  user.getId(), ipAddress);

// File: ExponentialBackoffFilter.java
logger.warn("⚠️ Failed login attempt #{} from IP: {}", attempts, ipAddress);
```

**What We Log:**
- ✅ Failed login attempts
- ✅ Password changes
- ✅ User blocks/deletions
- ✅ Admin actions
- ✅ Rate limit violations

**What's Missing:**
- ❌ Centralized logging (ELK, Splunk)
- ❌ Real-time alerts (Slack, PagerDuty)
- ❌ Security analytics (AI-based anomaly detection)
- ❌ Log integrity (prevent tampering)

**How to Improve:**

```yaml
# File: docker-compose.yml (Centralized logging)
services:
  elasticsearch:
    image: elasticsearch:8.11.0
  
  logstash:
    image: logstash:8.11.0
    # Collect logs from app
  
  kibana:
    image: kibana:8.11.0
    # Visualize logs
```

```java
// Structured logging (JSON)
@Slf4j
public class StructuredLogger {
    public void logSecurityEvent(String event, Map<String, Object> details) {
        logger.info("SECURITY_EVENT: {}", 
            new ObjectMapper().writeValueAsString(
                Map.of(
                    "event", event,
                    "timestamp", Instant.now(),
                    "userId", getCurrentUserId(),
                    "ipAddress", getClientIP(),
                    "details", details
                )
            )
        );
    }
}
```

---

### 🎯 **A10: Server-Side Request Forgery (SSRF)**

**What it is:**  
Attacker tricks server to make requests to internal resources.

**Example Attack:**
```java
// ❌ VULNERABLE: User controls URL
@GetMapping("/fetch")
public String fetchUrl(@RequestParam String url) {
    return restTemplate.getForObject(url, String.class);
}

// Attacker sends: url=http://localhost:8080/admin/delete-all-users
// Server makes request to INTERNAL admin endpoint!
```

**ما عندناش ثغرة دي:**  
✅ We don't accept URLs from user input  
✅ No external API fetching based on user input

**If We Needed This Feature:**
```java
// ✅ SAFE: Whitelist allowed domains
private static final Set<String> ALLOWED_DOMAINS = Set.of(
    "api.github.com",
    "api.stripe.com"
);

public String fetchUrl(String url) {
    URI uri = new URI(url);
    
    if (!ALLOWED_DOMAINS.contains(uri.getHost())) {
        throw new ForbiddenException("Domain not allowed");
    }
    
    return restTemplate.getForObject(url, String.class);
}
```

---

## 7. Security Filters & Middleware (طبقة الحماية)

### 🔒 **Filter Execution Order (الترتيب مهم جداً)**

```java
// File: SecurityConfig.java
http
    .addFilterBefore(securityHeadersFilter, UsernamePasswordAuthenticationFilter.class)
    .addFilterAfter(adminIpWhitelistFilter, SecurityHeadersFilter.class)
    .addFilterAfter(exponentialBackoffFilter, AdminIpWhitelistFilter.class)
    .addFilterAfter(globalApiRateLimitFilter, ExponentialBackoffFilter.class)
    .addFilterAfter(jwtAuthenticationFilter, GlobalApiRateLimitFilter.class);
```

**Execution Order:**

```
1. SecurityHeadersFilter          → Add security headers
2. AdminIpWhitelistFilter         → Block non-whitelisted IPs from /admin
3. ExponentialBackoffFilter       → Delay brute force attempts
4. GlobalApiRateLimitFilter       → Check rate limits
5. JwtAuthenticationFilter        → Validate JWT token
6. Spring Security Authorization  → Check permissions
7. Your Controller                → Business logic
```

**Why This Order?**

```
❌ Wrong: JWT filter BEFORE rate limiting
→ Attacker can brute force JWT validation (DoS)

✅ Correct: Rate limiting BEFORE JWT filter
→ Block attacker before expensive JWT validation
```

---

### 🛡️ **Our Security Filters (مفصل)**

#### **1. SecurityHeadersFilter.java**

**Purpose:** Add security headers to every response

```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) {
        
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // XSS protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Force HTTPS
        response.setHeader("Strict-Transport-Security", 
            "max-age=31536000; includeSubDomains");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'");
        
        filterChain.doFilter(request, response);
    }
}
```

---

#### **2. AdminIpWhitelistFilter.java**

**Purpose:** Restrict /api/admin/** to whitelisted IPs only

```java
@Component
public class AdminIpWhitelistFilter extends OncePerRequestFilter {
    
    @Value("${security.admin.allowed-ips}")
    private List<String> allowedIps; // From application.properties
    
    @Override
    protected void doFilterInternal(...) {
        
        // Only check admin endpoints
        if (!request.getRequestURI().startsWith("/api/admin/")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = getClientIP(request);
        
        if (!allowedIps.contains(clientIp)) {
            logger.warn("🚫 Admin access denied from IP: {}", clientIp);
            response.setStatus(403); // Forbidden
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getClientIP(HttpServletRequest request) {
        // Check X-Forwarded-For (proxy/load balancer)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }
}
```

**Configuration:**
```properties
# application-prod.properties
security.admin.allowed-ips=192.168.1.100,10.0.0.5,34.123.45.67
```

---

#### **3. ExponentialBackoffFilter.java**

**Purpose:** Progressive delays after failed login attempts

```java
@Component
public class ExponentialBackoffFilter extends OncePerRequestFilter {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    protected void doFilterInternal(...) {
        
        // Only for login endpoint
        if (!request.getRequestURI().equals("/api/auth/login")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String ipAddress = getClientIP(request);
        String key = "failed_login:" + ipAddress;
        
        // Get failed attempts count
        Integer attempts = (Integer) redisTemplate.opsForValue().get(key);
        if (attempts == null) attempts = 0;
        
        // Apply exponential backoff (2^n seconds)
        if (attempts >= 3) {
            int delaySeconds = (int) Math.pow(2, attempts - 2); // 2, 4, 8, 16...
            logger.warn("⏱️ Delaying login attempt #{} from {}: {}s", 
                attempts, ipAddress, delaySeconds);
            
            Thread.sleep(delaySeconds * 1000);
        }
        
        filterChain.doFilter(request, response);
        
        // Increment on failure (handled in LoginUseCase)
    }
}
```

**Failed Login Handler:**
```java
// File: LoginUseCase.java
try {
    // Validate password
} catch (InvalidPasswordException e) {
    // Increment failed attempts
    String key = "failed_login:" + ipAddress;
    redisTemplate.opsForValue().increment(key);
    redisTemplate.expire(key, 1, TimeUnit.HOURS);
    
    throw e;
}

// On success: Clear failed attempts
redisTemplate.delete("failed_login:" + ipAddress);
```

**Delay Progression:**
```
Attempt 1: No delay
Attempt 2: No delay
Attempt 3: No delay
Attempt 4: 2 seconds delay
Attempt 5: 4 seconds delay
Attempt 6: 8 seconds delay
Attempt 7: 16 seconds delay
...
```

---

#### **4. GlobalApiRateLimitFilter.java**

**Already covered in detail above**

---

#### **5. JwtAuthenticationFilter.java**

**Purpose:** Extract & validate JWT token, set authentication in SecurityContext

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService blacklistService;
    
    @Override
    protected void doFilterInternal(...) {
        
        // Extract token from header
        String token = extractToken(request);
        
        if (token != null && tokenProvider.validateToken(token)) {
            
            // Check blacklist (logout)
            if (blacklistService.isBlacklisted(token)) {
                logger.warn("🚫 Blacklisted token used");
                response.setStatus(401);
                return;
            }
            
            // Get user details from token
            String userId = tokenProvider.getUserIdFromToken(token);
            String role = tokenProvider.getRoleFromToken(token);
            
            // Create authentication object
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    userId, 
                    null, 
                    List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
            
            // Set in SecurityContext (for @PreAuthorize checks)
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

**How It Enables Authorization:**
```java
// In controller
@GetMapping("/profile")
public UserResponse getProfile() {
    // Get authenticated user ID from SecurityContext
    String userId = SecurityContextHolder.getContext()
        .getAuthentication()
        .getName();
    
    return userService.getProfile(userId);
}

// Or with @PreAuthorize
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser() {
    // Spring checks role from SecurityContext
}
```

---

## 8. Session Management (إدارة الجلسات)

### 🔑 **Our Approach: Hybrid (JWT + Redis Sessions)**

```
Traditional Session: Server-side state in Redis/DB
JWT Token: Client-side state (self-contained)

We use BOTH:
- JWT for API authentication (stateless)
- Redis for session management (stateful metadata)
```

---

### 📝 **A. LoginSession (Redis-based) ⭐ طبقناه**

**Purpose:** Track active user sessions for security

**ملفات التطبيق:**
- `LoginSession.java` (Domain model)
- `LoginSessionRedisRepository.java` (Infrastructure)
- `LoginUseCase.java` - Create session on login
- `LogoutUseCase.java` - Invalidate session
- `LogoutAllDevicesUseCase.java` - Invalidate all user sessions

```java
// File: LoginSession.java
public class LoginSession implements Serializable {
    private final String sessionId;      // UUID
    private final String userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt; // 24 hours
    private boolean active;
    private LocalDateTime lastAccessedAt;
    private String ipAddress;
    private String userAgent;
    
    public void access() {
        ensureActive();
        ensureNotExpired();
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    public void invalidate() {
        if (!active) {
            throw new InvalidTokenException("Already invalidated");
        }
        this.active = false;
    }
}
```

**Storage in Redis:**
```
Key: "login_session:550e8400-e29b-41d4-a716-446655440000"
Value: {
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "userId": "user123",
    "createdAt": "2026-02-08T10:00:00",
    "expiresAt": "2026-02-09T10:00:00",
    "active": true,
    "ipAddress": "192.168.1.100",
    "userAgent": "Mozilla/5.0..."
}
TTL: 24 hours (auto-expire)
```

**Why We Need This (in addition to JWT):**

```
JWT Limitations:
❌ Can't revoke token before expiry
❌ Can't track active sessions
❌ Can't logout all devices

LoginSession Solves:
✅ Logout invalidates session (even if JWT still valid)
✅ Can see all active sessions
✅ Can logout from all devices
✅ Track last access time
✅ Track IP & user agent (security audit)
```

**Logout Flow:**
```java
// File: LogoutUseCase.java
public void execute(String token) {
    String userId = jwtProvider.getUserIdFromToken(token);
    
    // 1. Blacklist JWT token (can't be used again)
    tokenBlacklistService.blacklist(token, jwtExpiryTime);
    
    // 2. Find and invalidate login session
    LoginSession session = sessionRepository.findByUserId(userId)
        .orElseThrow();
    
    session.invalidate(); // Set active = false
    sessionRepository.save(session);
    
    // 3. Publish event (for analytics)
    eventPublisher.publishEvent(new UserLoggedOutEvent(userId));
}
```

**Logout All Devices:**
```java
// File: LogoutAllDevicesUseCase.java
public void execute(String userId) {
    // Get all active sessions for user
    List<LoginSession> sessions = sessionRepository.findAllByUserId(userId);
    
    // Invalidate each session
    sessions.forEach(session -> {
        session.invalidate();
        sessionRepository.save(session);
    });
    
    // Also delete all refresh tokens
    refreshTokenRepository.deleteAllByUserId(userId);
}
```

---

### 🔄 **B. RefreshToken (Long-lived) ⭐ طبقناه**

**Purpose:** Get new JWT without re-authentication

**Why Needed:**

```
Problem:
JWT expires in 15 minutes
User has to login every 15 minutes (bad UX)

Solution:
- Access Token (JWT): 15 min (used for API calls)
- Refresh Token: 30 days (used to get new access token)

Flow:
1. Login → Get both tokens
2. Access token expires after 15 min
3. Use refresh token to get new access token
4. Refresh token is ONE-TIME USE (rotated)
```

**ملفات التطبيق:**
- `RefreshToken.java` (Domain model)
- `RefreshTokenRedisRepository.java`
- `RefreshTokenUseCase.java` - Exchange refresh for new access token

```java
// File: RefreshToken.java
public class RefreshToken implements Serializable {
    private final String tokenValue;     // UUID
    private final String userId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiresAt; // 30 days
    private boolean revoked;
    private LocalDateTime revokedAt;
    private LocalDateTime usedAt;
    
    public void use() {
        ensureNotExpired();
        ensureNotRevoked();
        ensureNotUsed(); // ⬅️ One-time use!
        
        this.usedAt = LocalDateTime.now();
    }
    
    public void revoke() {
        this.revoked = true;
        this.revokedAt = LocalDateTime.now();
    }
}
```

**Refresh Flow:**
```java
// File: RefreshTokenUseCase.java
public TokenResponse execute(String refreshTokenValue) {
    
    // 1. Find refresh token
    RefreshToken refreshToken = repository.findByTokenValue(refreshTokenValue)
        .orElseThrow(() -> new InvalidTokenException());
    
    // 2. Validate & mark as used
    refreshToken.use(); // Throws if expired/revoked/used
    
    // 3. Generate NEW tokens
    User user = userRepository.findById(refreshToken.getUserId())
        .orElseThrow();
    
    String newAccessToken = jwtProvider.generateAccessToken(user);
    RefreshToken newRefreshToken = RefreshToken.create(user.getId());
    
    // 4. Delete old refresh token (rotation)
    repository.delete(refreshToken);
    repository.save(newRefreshToken);
    
    return new TokenResponse(newAccessToken, newRefreshToken.getTokenValue());
}
```

**Security Benefits:**
- ✅ **Token Rotation:** Old token can't be reused (prevents replay attacks)
- ✅ **Long-lived but revocable:** Can invalidate by deleting from Redis
- ✅ **Detects theft:** If old token used after rotation → alert (token theft)

---

### 🔐 **C. Token Blacklist ⭐ طبقناه**

**Purpose:** Invalidate JWT tokens before expiry

**Problem:**
```
JWT is stateless → Can't revoke it before expiry
User logs out → Token still valid for 15 minutes!
```

**Solution:** Blacklist in Redis

```java
// File: TokenBlacklistService.java
@Service
public class TokenBlacklistService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void blacklist(String token, long expiryMs) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "revoked", expiryMs, TimeUnit.MILLISECONDS);
    }
    
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
```

**Usage:**
```java
// Logout
tokenBlacklistService.blacklist(token, jwtExpiryTime);

// Password change
List<RefreshToken> tokens = refreshTokenRepository.findAllByUserId(userId);
tokens.forEach(t -> tokenBlacklistService.blacklist(t.getTokenValue(), ...));
```

**Why Redis TTL?**
```
Token expires at: 2026-02-08 10:15:00
Blacklisted at:   2026-02-08 10:05:00

Set Redis TTL: 10 minutes (time until JWT expires)

After 10:15:00:
- JWT is expired anyway
- Redis auto-deletes blacklist entry (no memory leak)
```

---

## 9. Advanced Security Topics (مواضيع متقدمة)
### 🔐 **A. Security Compliance (الامتثال)**

#### **1. GDPR (General Data Protection Regulation)**

**ما طبقناه:**
- ✅ User can delete their account (Right to be forgotten)
- ✅ Password hashing (Data protection)
- ✅ Audit logs (failed logins, admin actions)

**ما طبقناهش:**
- ❌ Data export (user can download all their data)
- ❌ Consent management (cookies, terms acceptance)
- ❌ Data retention policies (auto-delete old data)
- ❌ Privacy Policy, Cookie Policy

**كيف نضيفها:**
```java
// Export user data
public byte[] exportUserData(String userId) {
    User user = userRepository.findById(userId);
    List<Order> orders = orderRepository.findByUserId(userId);
    
    Map<String, Object> data = Map.of(
        "profile", user,
        "orders", orders,
        "activity", activityLogs
    );
    
    return new ObjectMapper().writeValueAsBytes(data); // JSON export
}
```

---

#### **2. PCI-DSS (Payment Card Industry Data Security Standard)**

**ما محتاجينوش:**  
❌ We don't store credit card numbers (use payment gateway instead)

**If We Did Payment Processing:**
- ❌ Encrypt card numbers in database
- ❌ PCI-DSS compliant hosting
- ❌ Annual security audit
- ❌ Dedicated payment environment (isolated)

**Best Practice:** Use Stripe/PayPal (they handle PCI compliance)

---

#### **3. HIPAA (Healthcare)**

**Not applicable** for e-commerce

---

### 🛡️ **B. Advanced Attack Prevention**

#### **1. Account Takeover Prevention ⭐ طبقناه جزئياً**

**Our Protection:**
- ✅ Strong password hashing
- ✅ Rate limiting on login
- ✅ Exponential backoff
- ✅ Logout all devices on password change
- ❌ No email notification on suspicious login
- ❌ No device fingerprinting

**Advanced:**
```java
// Detect suspicious login
if (isNewDevice(userId, userAgent) && isNewLocation(userId, ipAddress)) {
    sendEmail(user.getEmail(), "New device login detected");
    requireMfaVerification();
}
```

---

#### **2. API Abuse Prevention ⭐ طبقناه**

**Our Protection:**
- ✅ Rate limiting (100/min per IP)
- ✅ Authentication required for most endpoints
- ✅ Input validation
- ❌ No CAPTCHA on public endpoints
- ❌ No bot detection (User-Agent analysis)

**Advanced:**
```java
// Bot detection
if (isBot(userAgent)) {
    requireCaptcha();
}

private boolean isBot(String userAgent) {
    return userAgent.contains("bot") || 
           userAgent.contains("crawler") ||
           userAgent.contains("spider");
}
```

---

#### **3. Business Logic Abuse ⭐ طبقناه**

**Examples:**
```java
// Prevent negative quantity
if (quantity < 1) {
    throw new ValidationException("Min quantity is 1");
}

// Prevent order manipulation
if (order.getTotalAmount().compareTo(calculatedTotal) != 0) {
    throw new ValidationException("Price mismatch");
}

// Prevent stock overselling (optimistic locking)
@Version
private Long version; // In Product entity
```

---

## 10. Implementation Status (إيه الطبقناه وإيه اللي لسه)

### ✅ **Fully Implemented (جاهز للإنتاج)**

| Feature | Files | Status |
|---------|-------|--------|
| JWT Authentication | JwtTokenProvider, JwtAuthenticationFilter | ✅ |
| Password Hashing (BCrypt) | BCryptPasswordHasher, Password | ✅ |
| Role-Based Authorization | SecurityConfig, @PreAuthorize | ✅ |
| Rate Limiting | GlobalApiRateLimitFilter | ✅ |
| Brute Force Protection | ExponentialBackoffFilter | ✅ |
| Admin IP Whitelist | AdminIpWhitelistFilter | ✅ |
| Security Headers | SecurityHeadersFilter | ✅ |
| Token Blacklist | TokenBlacklistService | ✅ |
| Refresh Token Rotation | RefreshTokenUseCase | ✅ |
| Session Management | LoginSession, LoginSessionRepository | ✅ |
| Input Sanitization | InputSanitizationService, @NoScriptTag | ✅ |
| SQL Injection Prevention | Spring Data JPA | ✅ |
| XSS Prevention | OWASP Encoder, CSP headers | ✅ |
| CORS Configuration | SecurityConfig | ✅ |
| Password Reset Flow | PasswordResetToken, Reset UseCases | ✅ |
| Email Verification | EmailVerificationToken | ✅ |
| Audit Logging | LoggingAspect | ✅ |
| HTTPS (Production) | application-prod.properties | ✅ |
| Secrets Management | Environment variables | ✅ |

---

### ⚠️ **Partially Implemented (نقدر نحسنها)**□ OAuth 2.0: Google Login (Spring Security OAuth2 Client)
□ Email MFA: OTP via email (for admin/sensitive operations)
□ reCAPTCHA v3: Invisible bot detection on login/register
□ Permission System: Move from roles to granular permissions
□ Suspicious Login Detection: Email alert on new device/IP
□ Password Breach Check: HaveIBeenPwned API integration
□ Advanced Rate Limiting: Per-endpoint limits (login 5/min, register 3/hour)
□ Security Dashboard: Real-time failed logins, rate limit violations

| Feature | Current State | Missing | Priority |
|---------|---------------|---------|----------|
| Password Validation | Min 8 chars | Complexity rules (uppercase, number, special) | Medium |
| Authorization | RBAC only | ABAC, fine-grained permissions | Low |
| Resource Ownership | Manual checks | Centralized OwnershipService | Medium |
| Logging | Basic logs | Structured JSON logs, centralized (ELK) | High |
| Error Handling | Stack traces hidden | Better error codes, user messages | Medium |
| Data Encryption | Passwords only | Column-level encryption for sensitive data | Low |

---

### ❌ **Not Implemented (مستقبل)**

| Feature | Why Not | When Needed |
|---------|---------|-------------|
| Multi-Factor Authentication | Cost (SMS/Email service) | Banking, healthcare apps |
| OAuth 2.0 / Social Login | Not needed for MVP | Better UX, enterprise SSO |
| CAPTCHA | No bot problem yet | High traffic public endpoints |
| Penetration Testing | Cost | Before production launch |
| WAF (Web Application Firewall) | Cost | Large-scale production |
| DDoS Protection | Cost (Cloudflare Pro) | After traffic grows |
| Intrusion Detection | Cost + complexity | Enterprise apps |
| Data Loss Prevention | Cost | Compliance requirements |
| Security Training | Time | Before going to market |
| Bug Bounty Program | Cost | After product launch |
| Security Incident Response Plan | Time | Before production |

---

## 11. Study Plan (خطة المذاكرة)

### 📚 **Phase 1: Core Concepts (أساسيات - أسبوع واحد)**

#### **Day 1-2: Authentication Basics**
```
□ Read: JwtTokenProvider.java
□ Understand: How JWT is generated
□ Understand: How JWT is validated
□ Read: LoginUseCase.java
□ Trace: Full login flow (email → password → JWT)
□ Practice: Generate JWT manually with Debugger

Resources:
- jwt.io (decode JWT tokens)
- RFC 7519 (JWT specification)
```

#### **Day 3-4: Authorization**
```
□ Read: SecurityConfig.java
□ Understand: Filter chain order
□ Read: @PreAuthorize examples in controllers
□ Understand: RBAC vs ABAC
□ Practice: Add new role (MODERATOR)

Resources:
- Spring Security docs
- OWASP Authorization Cheat Sheet
```

#### **Day 5-6: Password Security**
```
□ Read: BCryptPasswordHasher.java
□ Understand: Why BCrypt (salt, cost factor)
□ Read: Password.java (value object)
□ Practice: Test password strength validation
□ Learn: Password cracking techniques (understand the enemy)

Resources:
- OWASP Password Storage Cheat Sheet
- Have I Been Pwned API
```

#### **Day 7: Review**
```
□ Draw: Authentication flow diagram
□ Draw: Authorization decision tree
□ Test: Can you explain JWT vs Session to a 5-year-old?
□ Test: Can you implement basic auth from scratch?
```

---

### 📚 **Phase 2: Defense Mechanisms (حماية - أسبوع واحد)**

#### **Day 1-2: Rate Limiting & Brute Force**
```
□ Read: GlobalApiRateLimitFilter.java
□ Read: ExponentialBackoffFilter.java
□ Understand: Redis counter pattern
□ Understand: Exponential backoff math (2^n)
□ Practice: Test rate limiting with Postman (100 requests)

Resources:
- Redis commands (INCR, EXPIRE)
- Rate limiting algorithms (Token Bucket, Leaky Bucket)
```

#### **Day 3-4: Injection Attacks**
```
□ Read: OWASP Injection examples
□ Read: InputSanitizationService.java
□ Practice: Try SQL injection on test endpoint
□ Practice: Try XSS attack (see how CSP blocks it)
□ Learn: How JPA prevents SQL injection

Resources:
- OWASP SQL Injection Cheat Sheet
- PortSwigger Web Security Academy
```

#### **Day 5-6: Session Management**
```
□ Read: LoginSession.java
□ Read: RefreshToken.java
□ Read: TokenBlacklistService.java
□ Understand: Why we need all 3 (JWT + Session + Refresh)
□ Draw: Token lifecycle diagram

Resources:
- OWASP Session Management Cheat Sheet
```

#### **Day 7: Review**
```
□ Can you explain rate limiting algorithms?
□ Can you list 10 ways to prevent SQL injection?
□ Can you explain refresh token rotation?
```

---

### 📚 **Phase 3: OWASP Top 10 (أخطر ثغرات - أسبوع واحد)**

#### **Day 1: A01 - Broken Access Control**
```
□ Read: AuthorizationTest.java
□ Practice: Try accessing admin endpoint as customer
□ Practice: Try accessing another user's order
□ Understand: How Spring Security stops you

Resources:
- OWASP Top 10 2021
```

#### **Day 2: A02 - Cryptographic Failures**
```
□ Learn: BCrypt vs MD5 vs SHA-256
□ Practice: Hash same password 10 times (see different hashes - that's salt!)
□ Learn: HTTPS/TLS basics

Resources:
- Cryptography 101
- SSL certificate basics
```

#### **Day 3: A03 - Injection**
```
Review from Phase 2
```

#### **Day 4: A07 - Authentication Failures**
```
Review authentication from Phase 1
```

#### **Day 5-6: Remaining OWASP (A04, A05, A06, A08, A09, A10)**
```
□ Read each section above
□ Understand concept
□ Find in codebase (Ctrl+F)
```

#### **Day 7: Hands-on Security Testing**
```
□ Install: OWASP ZAP or Burp Suite
□ Scan: Your local application
□ Fix: Any issues found
□ Practice: Explain findings to non-technical person
```

---

### 📚 **Phase 4: Advanced Topics (متقدم - أسبوعين)**

#### **Week 1: Logging, Monitoring, Compliance**
```
□ Read: LoggingAspect.java
□ Set up: ELK stack (Docker)
□ Practice: Send logs to Elasticsearch
□ Learn: GDPR basics
□ Practice: Implement data export API

Resources:
- ELK Stack Tutorial
- GDPR for developers
```

#### **Week 2: Production Readiness**
```
□ Set up: HTTPS with Let's Encrypt
□ Configure: Production secret management (AWS Secrets Manager)
□ Learn: Docker security best practices
□ Learn: Kubernetes security
□ Create: Security incident response plan

Resources:
- CIS Benchmarks
- NIST Cybersecurity Framework
```

---

### 📚 **Phase 5: Practical Projects (تطبيق عملي - أسبوعين)**

#### **Project 1: Security Audit Your App**
```
□ Review all security filters
□ Test all authentication flows
□ Test all authorization rules
□ Use OWASP ZAP to scan
□ Fix any issues found
□ Document security architecture
```

#### **Project 2: Implement Missing Features**
```
Choose 2-3 from:
□ Multi-Factor Authentication (OTP via email)
□ Permission-based authorization (instead of just roles)
□ Account activity notifications (new device login)
□ Advanced password rules (ZXCVBN library)
□ OAuth 2.0 login with Google
```

#### **Project 3: Security Training Presentation**
```
□ Create: 30-minute presentation on "Web Security Fundamentals"
□ Explain: Authentication vs Authorization
□ Demo: Live SQL injection attack & prevention
□ Demo: XSS attack & CSP prevention
□ Q&A: Can you answer security questions?
```

---

## 📖 **Recommended Resources (مصادر مهمة)**

### 📚 **Books**
1. **"Web Application Security" by Andrew Hoffman** (Start here!)
2. **"The Tangled Web" by Michal Zalewski** (Browser security)
3. **"Hacking: The Art of Exploitation" by Jon Erickson** (Understand attackers)

### 🎓 **Courses**
1. **PortSwigger Web Security Academy** (FREE, best hands-on)
2. **OWASP Top 10** (FREE, video series)
3. **Udemy: Complete Spring Security** (Paid, ~$15)

### 🛠️ **Tools to Learn**
1. **Burp Suite Community** (FREE) - Intercept HTTP requests
2. **OWASP ZAP** (FREE) - Security scanner
3. **Postman** - API testing
4. **jwt.io** - JWT decoder

### 📰 **Stay Updated**
1. Subscribe: OWASP Newsletter
2. Follow: @OWASP on Twitter
3. Read: Security Weekly (blog)
4. Join: Reddit r/netsec

---

## 🎯 **Final Checklist (قبل الإنتاج)**

### ✅ **Security Pre-Launch**

```
Authentication:
□ JWT secret is strong (256-bit random)
□ JWT expires in reasonable time (15 min)
□ Refresh token rotation enabled
□ Token blacklist working
□ Password hashing is BCrypt (not MD5/SHA1)
□ Password complexity enforced
□ Failed login rate limiting enabled
□ Brute force protection active

Authorization:
□ All admin endpoints require ADMIN role
□ All sensitive endpoints require authentication
□ Resource ownership checks in place
□ No debug endpoints exposed

API Security:
□ CORS configured for production domains only
□ Rate limiting enabled (adjust limits for traffic)
□ Security headers enabled
□ HTTPS enforced (HSTS header)
□ CSRF disabled (for JWT stateless API)

Data Protection:
□ Passwords never logged
□ Sensitive fields never in error messages
□ Database connection encrypted (SSL)
□ Secrets in environment variables (not code)
□ No hardcoded API keys

Monitoring:
□ Failed login attempts logged
□ Admin actions logged
□ Error monitoring set up (Sentry/Rollbar)
□ Uptime monitoring (UptimeRobot)

Compliance:
□ Privacy Policy published
□ Terms of Service published
□ GDPR consent (if EU users)
□ Data retention policy defined

Testing:
□ All authentication flows tested
□ All authorization rules tested
□ Penetration test conducted
□ Load test passed
□ Security scan passed (OWASP ZAP)
```

---

## 🏆 **You Are Production Ready When...**

✅ You can explain **every security decision** in your architecture  
✅ You can trace **authentication flow** from start to finish  
✅ You know **why JWT** vs **Session** for your use case  
✅ You understand **OWASP Top 10** and how you prevent each  
✅ You can implement **basic security** from scratch (no copy-paste)  
✅ You can **secure any API** (not just this project)  
✅ You think like an **attacker** (what would I exploit?)  
✅ You can teach **security basics** to junior developers  

---

## 💡 **Key Takeaways (الخلاصة)**

### **1. Security is Layers, Not a Single Feature**
```
Network → Transport → Application → Authentication → Authorization → Input → Data
Every layer adds defense
```

### **2. Never Trust User Input**
```
Validate → Sanitize → Encode → Escape
Always assume malicious input
```

### **3. Fail Securely**
```
if (error) {
    denyAccess(); // ⬅️ Not allowAccess()
}
```

### **4. Keep It Simple**
```
Complex security = Hard to maintain = More bugs
Simple, auditable code > Clever obscurity
```

### **5. Stay Updated**
```
Security is ongoing, not one-time
Update dependencies, read advisories, test regularly
```

---

## 🎓 **You Now Know:**

### ✅ **Concepts**
- Authentication strategies (JWT, OAuth, Session, MFA)
- Authorization models (RBAC, ABAC, Ownership)
- OWASP Top 10 vulnerabilities
- Defense in depth
- Cryptography basics (hashing, encryption)

### ✅ **Implementation**
- JWT generation & validation
- Spring Security filter chain
- Rate limiting with Redis
- Password security (BCrypt)
- Session management
- Token rotation

### ✅ **Files You Should Know**
- `SecurityConfig.java` - Security configuration
- `JwtTokenProvider.java` - JWT logic
- `JwtAuthenticationFilter.java` - Token validation
- `GlobalApiRateLimitFilter.java` - Rate limiting
- `ExponentialBackoffFilter.java` - Brute force prevention
- `BCryptPasswordHasher.java` - Password hashing
- `LoginSession.java` - Session model
- `RefreshToken.java` - Token refresh
- `TokenBlacklistService.java` - Token revocation

### ✅ **What We Didn't Implement (And Why)**
- **MFA** - Cost (SMS/Email service)
- **OAuth 2.0** - Not needed for MVP
- **Data encryption at rest** - Not financial/healthcare app
- **Penetration testing** - Cost
- **WAF/DDoS** - Cost, needed after scale
- **Centralized logging** - Overkill for MVP

---

## 🚀 **Next Steps:**

1. ✅ **Study this document** (bookmark it!)
2. ✅ **Follow study plan** (12 weeks total)
3. ✅ **Build security muscle memory** (practice on every project)
4. ✅ **Stay paranoid** (always think: "How would I hack this?")

---

**Remember:** Security is a journey, not a destination. You're not building a fortress, you're building layers of smart defense. 🛡️

الأمان مش feature واحدة، ده طريقة تفكير! 🧠
