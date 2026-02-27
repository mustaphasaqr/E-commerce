package com.mustapha.ecommerce.shared.security.authorization;

import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.exception.ForbiddenException;
import com.mustapha.ecommerce.shared.exception.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;

/**
 * AOP Aspect for automatic resource ownership verification.
 * 
 * <p>This aspect intercepts methods annotated with @VerifyOwnership and automatically
 * performs authorization checks before allowing method execution. It eliminates the need
 * for repetitive ownership verification boilerplate in controllers and services.
 * 
 * <p>How It Works:
 * <ol>
 *   <li>Aspect intercepts any method with @VerifyOwnership annotation</li>
 *   <li>Extracts authenticated userId from Spring SecurityContext</li>
 *   <li>Extracts resourceId from method parameters using annotation's resourceIdParam</li>
 *   <li>Calls ResourceOwnershipService to verify ownership</li>
 *   <li>Throws ForbiddenException if ownership check fails</li>
 *   <li>Allows method execution if ownership verified</li>
 * </ol>
 * 
 * <p>Benefits of AOP Approach:
 * <ul>
 *   <li>Declarative Security: Ownership rules visible in method signatures</li>
 *   <li>DRY Principle: No repeated ownership check code</li>
 *   <li>Separation of Concerns: Authorization logic separated from business logic</li>
 *   <li>Consistency: Same verification mechanism across entire application</li>
 *   <li>Easy to Audit: Search for @VerifyOwnership to find all protected endpoints</li>
 * </ul>
 * 
 * <p>Example Usage:
 * <pre>{@code
 * @DeleteMapping("/orders/{orderId}")
 * @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
 * public ResponseEntity<Void> deleteOrder(@PathVariable String orderId) {
 *     // Aspect automatically verifies user owns this order before execution
 *     orderService.deleteOrder(orderId);
 *     return ResponseEntity.noContent().build();
 * }
 * }</pre>
 * 
 * @see VerifyOwnership
 * @see ResourceOwnershipService
 * @see ResourceType
 */
@Aspect
@Component
public class OwnershipAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(OwnershipAspect.class);
    
    private final ResourceOwnershipService ownershipService;
    
    public OwnershipAspect(ResourceOwnershipService ownershipService) {
        this.ownershipService = ownershipService;
    }
    
    /**
     * Advice that executes BEFORE any method annotated with @VerifyOwnership.
     * 
     * <p>This advice:
     * <ol>
     *   <li>Extracts authentication from SecurityContext (ensures user is logged in)</li>
     *   <li>Checks if user has bypass role (OWNER, EMPLOYEE) - if yes, allows access</li>
     *   <li>Extracts userId from authentication principal</li>
     *   <li>Extracts resourceId from method parameters</li>
     *   <li>Delegates to ResourceOwnershipService for actual ownership check</li>
     * </ol>
     * 
     * <p>Exception Handling:
     * <ul>
     *   <li>UnauthorizedException: If not authenticated (SecurityContext empty)</li>
     *   <li>ForbiddenException: If authenticated but not owner (thrown by ownershipService)</li>
     *   <li>IllegalArgumentException: If resourceIdParam doesn't match any parameter</li>
     * </ul>
     * 
     * @param joinPoint the method execution join point
     * @param verifyOwnership the @VerifyOwnership annotation instance
     * @throws UnauthorizedException if user not authenticated
     * @throws ForbiddenException if user not owner of resource
     */
    @Before("@annotation(verifyOwnership)")
    public void checkOwnership(JoinPoint joinPoint, VerifyOwnership verifyOwnership) {
        logger.debug("Ownership aspect triggered for method: {}", joinPoint.getSignature().getName());
        
        // Step 1: Get authentication to check roles
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new com.mustapha.ecommerce.shared.exception.UnauthorizedException(
                com.mustapha.ecommerce.shared.exception.ErrorCode.AUTH_INVALID_TOKEN,
                "Authentication required to access this resource"
            );
        }
        
        // Step 2: Check if user has bypass role (OWNER, EMPLOYEE can access any resource)
        if (hasBypassRole(authentication, verifyOwnership.bypassRoles())) {
            logger.debug("User has bypass role, skipping ownership check for: {}", 
                         joinPoint.getSignature().getName());
            return;
        }
        
        // Step 3: Extract authenticated user ID from SecurityContext
        String userId = extractAuthenticatedUserId();
        
        // Step 4: Extract resource ID from method parameters
        String resourceId = extractResourceId(joinPoint, verifyOwnership.resourceIdParam());
        
        // Step 5: Get resource type from annotation
        ResourceType resourceType = verifyOwnership.resourceType();
        
        // Step 6: Delegate to ResourceOwnershipService for ownership verification
        // This will throw ForbiddenException if user is not the owner
        ownershipService.checkOwnership(userId, resourceId, resourceType);
        
        logger.debug("Ownership verified - allowing method execution: {}", 
                     joinPoint.getSignature().getName());
    }
    
    /**
     * Check if authenticated user has any of the bypass roles.
     */
    private boolean hasBypassRole(Authentication authentication, String[] bypassRoles) {
        return authentication.getAuthorities().stream()
            .map(org.springframework.security.core.GrantedAuthority::getAuthority)
            .anyMatch(authority -> {
                String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
                return java.util.Arrays.asList(bypassRoles).contains(role);
            });
    }
    
    /**
     * Extracts the authenticated user ID from Spring SecurityContext.
     * 
     * <p>The userId is stored as the principal in the Authentication object
     * by JwtAuthenticationFilter after successful JWT validation.
     * 
     * @return the authenticated user's ID (String)
     * @throws UnauthorizedException if no authentication present (user not logged in)
     */
    private String extractAuthenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            logger.warn("No authentication found in SecurityContext - user not logged in");
            throw new UnauthorizedException(
                ErrorCode.AUTH_INVALID_TOKEN,
                "You must be logged in to perform this action"
            );
        }
        
        // Principal is set to userId by JwtAuthenticationFilter
        Object principal = authentication.getPrincipal();
        
        if (principal == null) {
            logger.warn("Authentication principal is null");
            throw new UnauthorizedException(
                ErrorCode.AUTH_INVALID_TOKEN,
                "Invalid authentication token"
            );
        }
        
        return principal.toString();
    }
    
    /**
     * Extracts the resource ID from method parameters using reflection.
     * 
     * <p>The annotation's resourceIdParam specifies which parameter contains the resource ID.
     * This method uses reflection to find that parameter's value at runtime.
     * 
     * <p>Example:
     * <pre>{@code
     * // Annotation: @VerifyOwnership(resourceType=ORDER, resourceIdParam="orderId")
     * // Method: deleteOrder(@PathVariable String orderId)
     * // This method finds "orderId" parameter and extracts its value
     * }</pre>
     * 
     * @param joinPoint the method execution join point containing parameter values
     * @param parameterName the name of the parameter to extract (from annotation)
     * @return the resource ID value (as String)
     * @throws IllegalArgumentException if parameter name not found in method signature
     */
    private String extractResourceId(JoinPoint joinPoint, String parameterName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();
        
        // Find the parameter by name and extract its value
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].getName().equals(parameterName)) {
                Object value = args[i];
                
                if (value == null) {
                    logger.error("Resource ID parameter '{}' is null", parameterName);
                    throw new IllegalArgumentException(
                        "Resource ID parameter '" + parameterName + "' cannot be null"
                    );
                }
                
                return value.toString();
            }
        }
        
        // Parameter name not found - developer error (annotation misconfigured)
        logger.error("Parameter '{}' not found in method signature: {}", 
                     parameterName, signature.getMethod().getName());
        throw new IllegalArgumentException(
            "Parameter '" + parameterName + "' not found in method signature. " +
            "Check @VerifyOwnership annotation resourceIdParam value."
        );
    }
}
