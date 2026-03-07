package com.mustapha.ecommerce.test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Factory to create SecurityContext for @TestWithAuthentication annotation.
 * Provides authenticated user for integration tests.
 */
public class TestSecurityContextFactory implements WithSecurityContextFactory<TestWithAuthentication> {

    @Override
    public SecurityContext createSecurityContext(TestWithAuthentication annotation) {
        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for (String role : annotation.roles()) {
            grantedAuthorities.add(new SimpleGrantedAuthority(role));
        }

        // Create a principal that includes user details
        TestUserPrincipal principal = new TestUserPrincipal(
            annotation.userId(),
            annotation.username(),
            annotation.email(),
            grantedAuthorities
        );

        // Create authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            principal,
            null,
            grantedAuthorities
        );

        SecurityContext context = new SecurityContextImpl();
        context.setAuthentication(authentication);
        return context;
    }

    /**
     * Lightweight user principal for testing
     */
    public static class TestUserPrincipal {
        private final long id;
        private final String username;
        private final String email;
        private final List<GrantedAuthority> authorities;

        public TestUserPrincipal(long id, String username, String email, List<GrantedAuthority> authorities) {
            this.id = id;
            this.username = username;
            this.email = email;
            this.authorities = authorities;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public List<GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String toString() {
            return "TestUserPrincipal{" +
                    "id=" + id +
                    ", username='" + username + '\'' +
                    ", email='" + email + '\'' +
                    '}';
        }
    }
}
