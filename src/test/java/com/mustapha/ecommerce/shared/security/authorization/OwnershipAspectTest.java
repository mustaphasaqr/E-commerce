package com.mustapha.ecommerce.shared.security.authorization;

import com.mustapha.ecommerce.shared.exception.ErrorCode;
import com.mustapha.ecommerce.shared.exception.ForbiddenException;
import com.mustapha.ecommerce.shared.exception.UnauthorizedException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("OwnershipAspect Tests")
class OwnershipAspectTest {

    private OwnershipAspect aspect;

    @Mock
    private ResourceOwnershipService ownershipService;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private VerifyOwnership verifyOwnership;

    private static final String USER_ID = "user-123";
    private static final String RESOURCE_ID = "resource-456";

    @BeforeEach
    void setUp() {
        aspect = new OwnershipAspect(ownershipService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @DisplayName("Should verify ownership for authenticated user")
    void verifyOwnershipForAuthenticatedUser() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        
        when(verifyOwnership.resourceType()).thenReturn(ResourceType.ORDER);
        when(verifyOwnership.resourceIdParam()).thenReturn("orderId");
        
        Method method = TestController.class.getMethod("deleteOrder", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{RESOURCE_ID});

        aspect.checkOwnership(joinPoint, verifyOwnership);

        verify(ownershipService).checkOwnership(USER_ID, RESOURCE_ID, ResourceType.ORDER);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when not authenticated")
    void throwUnauthorizedWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("testMethod");

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(UnauthorizedException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.AUTH_INVALID_TOKEN);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when authentication not authenticated")
    void throwUnauthorizedWhenAuthNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("testMethod");

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should throw UnauthorizedException when principal is null")
    void throwUnauthorizedWhenPrincipalNull() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(null);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getName()).thenReturn("testMethod");

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when parameter not found")
    void throwIllegalArgumentWhenParameterNotFound() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        
        when(verifyOwnership.resourceIdParam()).thenReturn("nonexistentParam");
        
        Method method = TestController.class.getMethod("deleteOrder", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("deleteOrder");
        when(joinPoint.getArgs()).thenReturn(new Object[]{RESOURCE_ID});

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("nonexistentParam");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when resource ID is null")
    void throwIllegalArgumentWhenResourceIdNull() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        
        when(verifyOwnership.resourceIdParam()).thenReturn("orderId");
        
        Method method = TestController.class.getMethod("deleteOrder", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(methodSignature.getName()).thenReturn("deleteOrder");
        when(joinPoint.getArgs()).thenReturn(new Object[]{null});

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("cannot be null");
    }

    @Test
    @DisplayName("Should propagate ForbiddenException from ownership service")
    void propagateForbiddenException() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        
        when(verifyOwnership.resourceType()).thenReturn(ResourceType.ORDER);
        when(verifyOwnership.resourceIdParam()).thenReturn("orderId");
        
        Method method = TestController.class.getMethod("deleteOrder", String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{RESOURCE_ID});
        
        doThrow(new ForbiddenException(ErrorCode.AUTHZ_NOT_RESOURCE_OWNER, "Not owner"))
            .when(ownershipService).checkOwnership(USER_ID, RESOURCE_ID, ResourceType.ORDER);

        assertThatThrownBy(() -> 
            aspect.checkOwnership(joinPoint, verifyOwnership)
        )
        .isInstanceOf(ForbiddenException.class)
        .hasMessageContaining("Not owner");
    }

    @Test
    @DisplayName("Should extract resource ID from correct parameter position")
    void extractResourceIdFromCorrectPosition() throws NoSuchMethodException {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(USER_ID);
        
        when(verifyOwnership.resourceType()).thenReturn(ResourceType.ORDER);
        when(verifyOwnership.resourceIdParam()).thenReturn("productId");
        
        Method method = TestController.class.getMethod("updateProduct", String.class, String.class);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(method);
        when(joinPoint.getArgs()).thenReturn(new Object[]{"other-param", RESOURCE_ID});

        aspect.checkOwnership(joinPoint, verifyOwnership);

        verify(ownershipService).checkOwnership(USER_ID, RESOURCE_ID, ResourceType.ORDER);
    }

    // Test controller with annotated methods
    public static class TestController {
        @VerifyOwnership(resourceType = ResourceType.ORDER, resourceIdParam = "orderId")
        public void deleteOrder(String orderId) {}
        
        public void updateProduct(String userId, String productId) {}
    }
}
