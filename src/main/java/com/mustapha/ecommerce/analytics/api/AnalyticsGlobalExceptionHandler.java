package com.mustapha.ecommerce.analytics.api;

import com.mustapha.ecommerce.analytics.application.exception.InvalidDateRangeException;
import com.mustapha.ecommerce.analytics.application.exception.InvalidLimitException;
import com.mustapha.ecommerce.analytics.application.exception.InvalidQueryParametersException;
import com.mustapha.ecommerce.analytics.domain.exception.InsufficientDataException;
import com.mustapha.ecommerce.analytics.domain.exception.InvalidMetricException;
import com.mustapha.ecommerce.analytics.infrastructure.exception.QueryExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;

/**
 * Global Exception Handler for Analytics Bounded Context
 * Responsibility: Translate analytics exceptions to HTTP responses
 * Pattern: Exception Translation (Domain/Application → HTTP)
 * SOLID: SRP (HTTP error handling for analytics only)
 * 
 * Enhanced Features:
 * - Correlation ID tracking for distributed tracing
 * - Comprehensive logging with context
 * - Standardized error response format
 * - Security: No sensitive data leaked to clients
 * 
 * This handler catches exceptions from analytics controllers and
 * converts them into meaningful HTTP responses with proper status codes.
 */
@RestControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE - 100)
public class AnalyticsGlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(AnalyticsGlobalExceptionHandler.class);

    // ========== Application Layer Exceptions ==========
    
    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidDateRange(InvalidDateRangeException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics validation error - Invalid date range | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Date Range",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidLimitException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLimit(InvalidLimitException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics validation error - Invalid limit | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Limit Parameter",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(InvalidQueryParametersException.class)
    public ResponseEntity<ErrorResponse> handleInvalidQueryParameters(InvalidQueryParametersException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics validation error - Invalid query parameters | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Query Parameters",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    // ========== Domain Layer Exceptions ==========
    
    @ExceptionHandler(InsufficientDataException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientData(InsufficientDataException ex) {
        String correlationId = MDC.get("correlationId");
        log.info("Analytics query returned insufficient data | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.NOT_FOUND.value(),
            "Insufficient Data",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InvalidMetricException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMetric(InvalidMetricException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics domain error - Invalid metric | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Invalid Metric",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(error);
    }

    // ========== Infrastructure Layer Exceptions ==========
    
    @ExceptionHandler(QueryExecutionException.class)
    public ResponseEntity<ErrorResponse> handleQueryExecution(QueryExecutionException ex) {
        String correlationId = MDC.get("correlationId");
        // Log full exception for debugging (don't expose internal details to client)
        log.error("Analytics query execution failed | correlationId={} | error={}", 
                correlationId, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Query Execution Failed",
            "An error occurred while executing the analytics query. Please try again later.",
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ========== Generic Exceptions ==========
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics request missing parameter | correlationId={} | parameter={}", 
                correlationId, ex.getParameterName());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Missing Required Parameter",
            String.format("Required parameter '%s' is missing", ex.getParameterName()),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics request parameter type mismatch | correlationId={} | parameter={} | expectedType={}", 
                correlationId, ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Parameter Type",
            String.format("Parameter '%s' has invalid format. Expected type: %s", 
                ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown"),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        String correlationId = MDC.get("correlationId");
        log.warn("Analytics illegal argument | correlationId={} | message={}", 
                correlationId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Argument",
            ex.getMessage(),
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        String correlationId = MDC.get("correlationId");
        // Log full exception for debugging - this is a catch-all
        log.error("Unexpected error in analytics | correlationId={} | exceptionType={} | message={}", 
                correlationId, ex.getClass().getSimpleName(), ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred. Please contact support if the problem persists.",
            LocalDateTime.now(),
            correlationId
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // ========== Error Response DTO ==========
    
    /**
     * Standard error response structure for analytics API
     * 
     * @param status HTTP status code
     * @param error Error category/type
     * @param message Detailed error message (safe for client display)
     * @param timestamp When the error occurred
     * @param correlationId Trace ID for distributed tracing (Zipkin/logging)
     */
    public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        String correlationId
    ) {}
}

