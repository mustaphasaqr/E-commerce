package com.mustapha.ecommerce.shared.security;

import com.mustapha.ecommerce.shared.observability.LoggingContextHolder;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Authentication Filter
 * 
 * Responsibilities:
 * 1. Extract JWT from Authorization header
 * 2. Validate token signature and expiration
 * 3. Extract userId and role from claims
 * 4. Set SecurityContext with Authentication
 * 
 * Pattern: Spring Security Filter Chain
 * Position: Before UsernamePasswordAuthenticationFilter
 * 
 * Note: Session validation not performed here for performance.
 * Logout-all is enforced by deleting sessions, and JWTs expire naturally.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenGenerator jwtTokenGenerator;
    private final TokenBlacklistService tokenBlacklistService;

    public JwtAuthenticationFilter(JwtTokenGenerator jwtTokenGenerator,
                                   TokenBlacklistService tokenBlacklistService) {
        this.jwtTokenGenerator = jwtTokenGenerator;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        
        try {
            // Check if token is blacklisted (logged out)
            if (tokenBlacklistService.isBlacklisted(token)) {
                SecurityContextHolder.clearContext();
                filterChain.doFilter(request, response);
                return;
            }
            
            if (jwtTokenGenerator.validateToken(token)) {
                String userId = jwtTokenGenerator.extractUserId(token);
                String role = jwtTokenGenerator.extractRole(token);
                String sessionId = jwtTokenGenerator.extractSessionId(token);
                
                // Create Authentication with userId as principal and role as authority
                SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + role);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(authority)
                );
                
                // Store sessionId in authentication details for logout
                authentication.setDetails(sessionId);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                // Add user context to MDC for structured logging
                // All subsequent log statements will automatically include userId, sessionId, and role
                LoggingContextHolder.setUserId(userId);
                LoggingContextHolder.setSessionId(sessionId);
                LoggingContextHolder.setUserRole(role);
            }
        } catch (Exception e) {
            // Invalid token - clear context
            SecurityContextHolder.clearContext();
        }
        
        filterChain.doFilter(request, response);
    }
}
