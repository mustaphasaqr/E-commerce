package com.mustapha.ecommerce.order.api;

import com.mustapha.ecommerce.order.exception.OrderNotFoundException;
import com.mustapha.ecommerce.order.exception.OrderValidationException;
import com.mustapha.ecommerce.order.exception.PaymentRejectedException;
import com.mustapha.ecommerce.shared.dto.BaseResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Order-specific exception handler
 * Responsibility: Order-specific HTTP error mapping only
 */
@RestControllerAdvice(assignableTypes = OrderController.class)
public class OrderExceptionHandler {

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<BaseResponse> handleOrderNotFound(OrderNotFoundException ex) {
        BaseResponse response = new BaseResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(OrderValidationException.class)
    public ResponseEntity<BaseResponse> handleOrderValidation(OrderValidationException ex) {
        BaseResponse response = new BaseResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(PaymentRejectedException.class)
    public ResponseEntity<BaseResponse> handlePaymentRejected(PaymentRejectedException ex) {
        BaseResponse response = new BaseResponse(false, ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED).body(response);
    }
}
