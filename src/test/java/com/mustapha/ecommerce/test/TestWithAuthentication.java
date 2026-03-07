package com.mustapha.ecommerce.test;

import org.springframework.security.test.context.support.WithSecurityContext;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation to provide authenticated user context for integration tests.
 * Usage: @TestWithAuthentication on test methods/classes
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = TestSecurityContextFactory.class)
public @interface TestWithAuthentication {
    String username() default "testuser";
    String email() default "test@example.com";
    String[] roles() default {"ROLE_USER"};
    long userId() default 1L;
}
