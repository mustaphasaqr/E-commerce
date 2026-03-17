package com.mustapha.ecommerce.order.api.webhook;

import com.mustapha.ecommerce.order.application.port.PaymentPort;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentStatus;
import com.mustapha.ecommerce.order.application.command.VerifyPaymentCommand;
import com.mustapha.ecommerce.order.application.usecase.VerifyPaymentUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.Map;

/**
 * Payment Callback Controller
 * 
 * Purpose: Handle payment redirects from Accept (Paymob) after customer completes payment
 * 
 * Accept Payment Redirect Flow:
 * 1. Customer completes payment on Accept payment page
 * 2. Accept redirects to this endpoint with checkout ID
 * 3. We verify payment status with Accept API
 * 4. We update order status based on payment result
 * 5. We redirect customer to success/failure page
 * 
 * Callback URL Configuration:
 * - Development: http://localhost:8080/api/webhooks/payment/callback
 * - Production: https://yourdomain.com/api/webhooks/payment/callback
 * 
 * Security:
 * - Payment verification via server-to-server API call
 * - Idempotency prevents duplicate processing
 * 
 * Accept (Paymob) Documentation:
 * https://docs.paymob.com/docs/accept-standard-redirect
 */
@RestController
@RequestMapping({"/api/webhooks/payment", "/api/v1/api/webhooks/payment"})
public class PaymentWebhookController {
    
    private static final Logger logger = LoggerFactory.getLogger(PaymentWebhookController.class);
    
    private final VerifyPaymentUseCase verifyPaymentUseCase;
    
    public PaymentWebhookController(VerifyPaymentUseCase verifyPaymentUseCase) {
        this.verifyPaymentUseCase = verifyPaymentUseCase;
    }

    /**
     * Accept Payment Callback Endpoint
     * 
     * Called when Accept redirects customer back after payment
     * 
     * Query Parameters:
     * - id: Checkout ID (required)
     * - resourcePath: Payment resource path (optional, provided by Accept)
     * 
     * Example URL:
     * https://yourdomain.com/api/webhooks/payment/callback?id=payment_key_here
     */
    @GetMapping("/callback")
    public RedirectView handlePaymentCallback(
            @RequestParam(value = "id", required = false) String checkoutId,
            @RequestParam(value = "resourcePath", required = false) String resourcePath) {
        
        try {
            logger.info("📨 Received Accept payment callback: checkoutId={}, resourcePath={}", checkoutId, resourcePath);
            
            if (checkoutId == null || checkoutId.trim().isEmpty()) {
                logger.error("Missing checkout ID in callback");
                return new RedirectView("/payment/failure?error=missing_checkout_id");
            }
            
            // Verify payment and apply order orchestration (paid/failed/cancelled flows)
            PaymentVerificationResult result = verifyPaymentUseCase.execute(new VerifyPaymentCommand(checkoutId));
            
            logger.info("Payment verification result: status={}, transactionId={}", 
                       result.status(), result.transactionId());
            
            // Process based on payment status
            if (result.status() == PaymentStatus.SUCCESS) {
                logger.info("✅ Payment succeeded for checkoutId: {}", checkoutId);
                
                return new RedirectView("/payment/success?transactionId=" + result.transactionId());
                
            } else if (result.status() == PaymentStatus.FAILED) {
                logger.warn("❌ Payment failed for checkoutId: {}", checkoutId);
                
                return new RedirectView("/payment/failure?reason=" + result.message());
                
            } else if (result.status() == PaymentStatus.PENDING) {
                logger.info("⏳ Payment pending for checkoutId: {}", checkoutId);
                
                return new RedirectView("/payment/pending?transactionId=" + result.transactionId());
                
            } else if (result.status() == PaymentStatus.CANCELLED) {
                logger.info("🚫 Payment cancelled by customer: {}", checkoutId);
                
                return new RedirectView("/payment/cancelled");
            }
            
            // Unknown status
            logger.warn("⚠️ Unknown payment status: {}", result.status());
            return new RedirectView("/payment/failure?error=unknown_status");
            
        } catch (Exception e) {
            logger.error("❌ Error processing Accept payment callback", e);
            return new RedirectView("/payment/failure?error=internal_error");
        }
    }

    /**
     * API endpoint for payment verification
     * Use this for server-side payment verification without redirect
     */
    @GetMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestParam(value = "checkoutId") String checkoutId) {
        
        try {
            logger.info("Verifying payment for checkoutId: {}", checkoutId);
            
            PaymentVerificationResult result = verifyPaymentUseCase.execute(new VerifyPaymentCommand(checkoutId));
            
            return ResponseEntity.ok(Map.of(
                    "success", result.status() == PaymentStatus.SUCCESS,
                    "status", result.status().name(),
                    "transactionId", result.transactionId() != null ? result.transactionId() : "",
                    "message", result.message() != null ? result.message() : ""
            ));
            
        } catch (Exception e) {
            logger.error("Error verifying payment", e);
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     * Test that callback URL is reachable
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "payment-webhook",
                "provider", "accept-paymob"
        ));
    }
}
