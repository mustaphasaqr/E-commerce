package com.mustapha.ecommerce.shared.security.authorization;

import com.mustapha.ecommerce.order.domain.model.Order;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;
import com.mustapha.ecommerce.order.domain.repository.OrderRepository;
import com.mustapha.ecommerce.shared.exception.ForbiddenException;
import com.mustapha.ecommerce.shared.exception.UnauthorizedException;
import com.mustapha.ecommerce.shared.exception.ErrorCode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Optional;

/**
 * DEPRECATED: Duplicate of OwnershipAspect - DO NOT USE
 * 
 * This aspect was created as a duplicate of the existing OwnershipAspect.
 * Use OwnershipAspect + ResourceOwnershipService instead.
 * 
 * Disabled by removing @Component annotation.
 * 
 * @see OwnershipAspect
 * @see ResourceOwnershipService
 */
@Aspect
// @Component - DISABLED: Use OwnershipAspect instead
public class ResourceOwnershipAspect {

    private static final Logger log = LoggerFactory.getLogger(ResourceOwnershipAspect.class);

    private final OrderRepository orderRepository;

    public ResourceOwnershipAspect(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Before("@annotation(verifyOwnership)")
    public void verifyResourceOwnership(JoinPoint joinPoint, VerifyOwnership verifyOwnership) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException(
                ErrorCode.AUTH_INVALID_TOKEN,
                "Authentication required to access this resource"
            );
        }

        // Extract authenticated user ID
        String authenticatedUserId = authentication.getName();
        
        // Check if user has bypass role
        if (hasBypassRole(authentication, verifyOwnership.bypassRoles())) {
            log.debug("User {} has bypass role, skipping ownership check", authenticatedUserId);
            return;
        }

        // Extract resource ID from method parameters
        String resourceId = extractResourceId(joinPoint, verifyOwnership.resourceIdParam());
        
        // Verify ownership based on resource type
        boolean isOwner = verifyOwnershipForResourceType(
            authenticatedUserId, 
            resourceId, 
            verifyOwnership.resourceType()
        );

        if (!isOwner) {
            log.warn("User {} attempted to access {} {} without permission", 
                authenticatedUserId, verifyOwnership.resourceType().name(), resourceId);
            throw new ForbiddenException(
                ErrorCode.AUTHZ_NOT_RESOURCE_OWNER,
                String.format("You do not have permission to access this %s", 
                    verifyOwnership.resourceType().name().toLowerCase())
            );
        }

        log.debug("User {} verified as owner of {} {}", 
            authenticatedUserId, verifyOwnership.resourceType().name(), resourceId);
    }

    /**
     * Check if authenticated user has any of the bypass roles
     */
    private boolean hasBypassRole(Authentication authentication, String[] bypassRoles) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> {
                String role = authority.startsWith("ROLE_") ? authority.substring(5) : authority;
                return Arrays.asList(bypassRoles).contains(role);
            });
    }

    /**
     * Extract resource ID from method parameters
     */
    private String extractResourceId(JoinPoint joinPoint, String paramName) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Parameter[] parameters = signature.getMethod().getParameters();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            
            // Check @PathVariable annotation
            var pathVariable = param.getAnnotation(org.springframework.web.bind.annotation.PathVariable.class);
            if (pathVariable != null) {
                String pvName = pathVariable.value().isEmpty() ? param.getName() : pathVariable.value();
                if (pvName.equals(paramName)) {
                    return args[i].toString();
                }
            }
            
            // Check @RequestParam annotation
            var requestParam = param.getAnnotation(org.springframework.web.bind.annotation.RequestParam.class);
            if (requestParam != null) {
                String rpName = requestParam.value().isEmpty() ? param.getName() : requestParam.value();
                if (rpName.equals(paramName)) {
                    return args[i].toString();
                }
            }
        }

        throw new IllegalStateException(
            "Could not find parameter '" + paramName + "' in method signature. " +
            "Ensure @VerifyOwnership.resourceIdParam matches @PathVariable or @RequestParam name."
        );
    }

    /**
     * Verify ownership based on resource type
     */
    private boolean verifyOwnershipForResourceType(String userId, String resourceId, ResourceType resourceType) {
        return switch (resourceType) {
            case ORDER -> verifyOrderOwnership(userId, resourceId);
            // Add more resource types as needed:
            // case CART -> verifyCartOwnership(userId, resourceId);
            // case WISHLIST -> verifyWishlistOwnership(userId, resourceId);
            default -> throw new IllegalArgumentException(
                "Unknown resource type: " + resourceType + ". Add ownership verification logic."
            );
        };
    }

    /**
     * Verify user owns the specified order
     * 
     * <p>Returns true if order doesn't exist to allow the use case to throw proper 404.
     * Authorization should only deny access to EXISTING resources the user doesn't own.
     */
    private boolean verifyOrderOwnership(String userId, String orderId) {
        Optional<Order> orderOpt = orderRepository.findById(new OrderId(orderId));
        
        if (orderOpt.isEmpty()) {
            log.debug("Order {} not found - skipping ownership check (use case will handle 404)", orderId);
            return true; // Let use case throw OrderNotFoundException for proper 404
        }

        Order order = orderOpt.get();
        String orderOwnerId = order.getCustomerId().getValue();
        
        return orderOwnerId.equals(userId);
    }
}
