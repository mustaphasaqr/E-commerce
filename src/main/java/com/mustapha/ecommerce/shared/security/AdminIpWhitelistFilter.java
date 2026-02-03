package com.mustapha.ecommerce.shared.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class AdminIpWhitelistFilter extends OncePerRequestFilter {
    
    private final Environment environment;
    
    @Value("${admin.allowed-ips:}")
    private String allowedIpsConfig;
    
    private static final String ADMIN_PATH_PREFIX = "/api/admin/";
    
    public AdminIpWhitelistFilter(Environment environment) {
        this.environment = environment;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {
        // Skip IP whitelist in test environment
        if (Arrays.asList(environment.getActiveProfiles()).contains("test")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String path = request.getRequestURI();
        
        if (!path.startsWith(ADMIN_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }
        
        Set<String> allowedIps = parseAllowedIps();
        
        if (allowedIps.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }
        
        String clientIp = extractIpAddress(request);
        
        if (!allowedIps.contains(clientIp)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(String.format(
                "{\"error\":\"Access forbidden: IP address %s not whitelisted for admin endpoints\",\"timestamp\":\"%s\"}",
                clientIp,
                java.time.Instant.now()
            ));
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    private Set<String> parseAllowedIps() {
        if (allowedIpsConfig == null || allowedIpsConfig.trim().isEmpty()) {
            return new HashSet<>();
        }
        
        return new HashSet<>(Arrays.asList(allowedIpsConfig.split(",")));
    }
    
    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
