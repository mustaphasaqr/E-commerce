package com.mustapha.ecommerce.shared.observability;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestIdFilter Tests")
class RequestIdFilterTest {

    private RequestIdFilter filter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        filter = new RequestIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate new requestId when header not provided")
    void generateNewRequestId_WhenHeaderMissing() throws ServletException, IOException {
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(filterChain).doFilter(request, response);
        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("Should use existing requestId from header")
    void useExistingRequestId_WhenHeaderProvided() throws ServletException, IOException {
        String existingRequestId = "test-request-id-123";
        when(request.getHeader("X-Request-ID")).thenReturn(existingRequestId);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader("X-Request-ID", existingRequestId);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate new requestId when header is empty")
    void generateNewRequestId_WhenHeaderEmpty() throws ServletException, IOException {
        when(request.getHeader("X-Request-ID")).thenReturn("   ");

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clean up MDC after request")
    void cleanupMDC_AfterRequest() throws ServletException, IOException {
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");

        filter.doFilterInternal(request, response, filterChain);

        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("Should clean up MDC even when filter chain throws exception")
    void cleanupMDC_WhenExceptionThrown() throws ServletException, IOException {
        when(request.getHeader("X-Request-ID")).thenReturn("test-id");
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        try {
            filter.doFilterInternal(request, response, filterChain);
        } catch (ServletException e) {
            // Expected
        }

        assertThat(MDC.get("requestId")).isNull();
    }

    @Test
    @DisplayName("Should generate valid UUID format")
    void generateValidUuid() throws ServletException, IOException {
        when(request.getHeader("X-Request-ID")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setHeader(eq("X-Request-ID"), argThat(id -> 
            id != null && id.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")
        ));
    }

    @Test
    @DisplayName("Should preserve requestId in MDC during request processing")
    void preserveRequestIdDuringProcessing() throws ServletException, IOException {
        String requestId = "correlation-id-abc-123";
        when(request.getHeader("X-Request-ID")).thenReturn(requestId);

        doAnswer(invocation -> {
            assertThat(MDC.get("requestId")).isEqualTo(requestId);
            return null;
        }).when(filterChain).doFilter(request, response);

        filter.doFilterInternal(request, response, filterChain);
    }
}
