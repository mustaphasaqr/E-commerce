package com.mustapha.ecommerce.order.api.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.mustapha.ecommerce.order.application.command.InitiatePaymentCommand;
import com.mustapha.ecommerce.order.application.command.VerifyPaymentCommand;
import com.mustapha.ecommerce.order.application.port.PaymentPort.CheckoutResult;
import com.mustapha.ecommerce.order.application.port.PaymentPort.PaymentVerificationResult;
import com.mustapha.ecommerce.order.application.usecase.InitiatePaymentUseCase;
import com.mustapha.ecommerce.order.application.usecase.VerifyPaymentUseCase;
import com.mustapha.ecommerce.order.domain.model.valueobject.OrderId;

import java.util.Map;

/**
 * Payment REST Controller
 * Responsibility: HTTP endpoints for payment operations
 * Pattern: REST API, CQRS Commands
 * 
 * Endpoints:
 * - POST /api/payments/checkout → Initiate payment (get redirect URL)
 * - POST /api/payments/verify   → Verify payment after redirect
 * - GET  /api/payments/health   → Health check endpoint
 * 
 * ========================================
 * 📝 TODO: FRONTEND INTEGRATION GUIDE
 * ========================================
 * 
 * This section documents how to integrate payment functionality in the frontend.
 * Reference: See ACCEPT_PAYMOB_QUICK_START.md for complete implementation details.
 * 
 * Production Payment Flow (Customer Journey):
 * ┌────────────────────────────────────────────────────────────────────────────┐
 * │ 1. Customer adds items to cart                                             │
 * │ 2. Customer clicks "Proceed to Checkout"                                   │
 * │ 3. Frontend validates cart and creates order (POST /api/orders)            │
 * │ 4. Frontend confirms order (POST /api/orders/{orderId}/confirm)            │
 * │ 5. Frontend shows payment method selection (Visa/Mastercard/Mada)          │
 * │ 6. Customer clicks "Pay Now"                                               │
 * │ 7. Frontend calls POST /api/payments/checkout                              │
 * │    ├─ Request: {orderId, paymentMethod, customerEmail}                     │
 * │    └─ Response: {checkoutId, redirectUrl, expiresInSeconds}               │
 │ 8. Frontend redirects customer to redirectUrl (Accept payment page)        │
 │ 9. Customer enters card details on Accept secure page                      │
 │ 10. Accept processes payment                                               │
 │ 11. Accept redirects back to frontend with ?id=checkoutId                  │
 * │ 12. Frontend extracts checkoutId from URL query parameter                  │
 * │ 13. Frontend calls POST /api/payments/verify                               │
 * │     ├─ Request: {checkoutId}                                               │
 * │     └─ Response: {success, status, transactionId, message}                │
 * │ 14. Frontend shows success/failure page based on status                    │
 * └────────────────────────────────────────────────────────────────────────────┘
 * 
 * Frontend Pages to Implement:
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ Page 1: Checkout Page (/checkout)                                         │
 * │ ├─ Shows order summary (items, total, shipping)                           │
 * │ ├─ Payment method selector:                                               │
 * │ │  └─ Radio buttons: ( ) Visa  ( ) Mastercard  ( ) Mada                  │
 * │ ├─ Customer email input (for payment receipt)                             │
 * │ └─ [Pay Now] button → calls initiatePayment()                             │
 * │                                                                            │
 * │ Page 2: Payment Processing Page (/payment/processing)                     │
 │ ├─ Redirects immediately to Accept (no UI needed)                         │
 * │ └─ window.location.href = checkoutResult.redirectUrl                      │
 * │                                                                            │
 * │ Page 3: Payment Return Handler (/payment/return)                          │
 * │ ├─ Shows "Processing payment..." spinner                                  │
 * │ ├─ Extracts checkoutId from URL: ?id=8a82944...                           │
 * │ ├─ Calls POST /api/payments/verify with checkoutId                        │
 * │ └─ Redirects to success/failure page based on result                      │
 * │                                                                            │
 * │ Page 4: Payment Success Page (/order-confirmation)                        │
 * │ ├─ Shows "✅ Payment Successful" message                                  │
 * │ ├─ Displays order details (order ID, items, total)                        │
 * │ ├─ Shows transaction ID (for customer reference)                          │
 * │ └─ "View Order" button → redirects to order details page                  │
 * │                                                                            │
 * │ Page 5: Payment Failed Page (/payment-failed)                             │
 * │ ├─ Shows "❌ Payment Failed" message                                      │
 * │ ├─ Displays error reason (card declined, expired, etc.)                   │
 * │ ├─ "Retry Payment" button → returns to checkout page                      │
 * │ └─ "Contact Support" link                                                 │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * Frontend JavaScript Example (React/Vue/Angular):
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ // Step 1: Initiate Payment (on Checkout Page)                            │
 * │ async function initiatePayment(orderId, paymentMethod, customerEmail) {   │
 * │   const response = await fetch('/api/payments/checkout', {               │
 * │     method: 'POST',                                                       │
 * │     headers: { 'Content-Type': 'application/json' },                     │
 * │     body: JSON.stringify({                                               │
 * │       orderId: orderId,                                                  │
 * │       paymentMethod: paymentMethod,  // 'VISA', 'MASTERCARD', 'MADA'    │
 * │       customerEmail: customerEmail                                       │
 * │     })                                                                   │
 * │   });                                                                    │
 * │                                                                          │
 * │   const data = await response.json();                                   │
 * │                                                                          │
 * │   if (data.success) {                                                   │
 * │     // Redirect to Accept (Paymob) payment page                        │
 * │     window.location.href = data.redirectUrl;                            │
 * │   } else {                                                              │
 * │     alert('Payment initiation failed: ' + data.message);                │
 * │   }                                                                     │
 * │ }                                                                       │
 * │                                                                         │
 * │ // Step 2: Handle Return from Accept (on Return Page)                  │
 * │ async function handlePaymentReturn() {                                 │
 * │   // Extract checkoutId from URL                                       │
 * │   const urlParams = new URLSearchParams(window.location.search);      │
 * │   const checkoutId = urlParams.get('id'); // Accept adds ?id=...      │
 * │                                                                        │
 * │   if (!checkoutId) {                                                  │
 * │     alert('No checkout ID found');                                    │
 * │     return;                                                           │
 * │   }                                                                   │
 * │                                                                       │
 * │   // Verify payment with backend                                     │
 * │   const response = await fetch('/api/payments/verify', {            │
 * │     method: 'POST',                                                  │
 * │     headers: { 'Content-Type': 'application/json' },                │
 * │     body: JSON.stringify({ checkoutId: checkoutId })                │
 * │   });                                                                │
 * │                                                                      │
 * │   const result = await response.json();                             │
 * │                                                                     │
 * │   if (result.success && result.status === 'SUCCESS') {             │
 * │     // Payment successful - redirect to success page               │
 * │     window.location.href = '/order-confirmation?txId=' +           │
 * │                            result.transactionId;                   │
 * │   } else if (result.status === 'FAILED') {                        │
 * │     // Payment failed - redirect to failure page                  │
 * │     window.location.href = '/payment-failed?reason=' +            │
 * │                            encodeURIComponent(result.message);    │
 * │   } else {                                                        │
 * │     // Payment pending - show waiting message                    │
 * │     alert('Payment is pending. Check your email for updates.');  │
 * │   }                                                               │
 * │ }                                                                 │
 * └───────────────────────────────────────────────────────────────────┘
 * 
 * Accept (Paymob) Configuration (Dashboard Setup):
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ 1. Login to Accept Dashboard: https://accept.paymob.com/portal2/en/login │
 * │ 2. Go to: Settings → Payment Integrations                                 │
 * │ 3. Copy your Integration ID and API Key                                   │
 * │ 4. Set callback URLs in dashboard:                                        │
 * │    └─ Production: https://yoursite.com/api/webhooks/payment/callback     │
 * │    └─ Development: http://localhost:8080/api/webhooks/payment/callback   │
 * │ 5. Enable payment methods: ✓ Visa ✓ Mastercard ✓ Fawry ✓ Mobile Wallets │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * Error Handling:
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ Frontend must handle these scenarios:                                     │
 * │ ├─ Network errors (fetch fails) → Show "Connection error" message        │
 * │ ├─ Validation errors (400) → Show "Invalid data" message                 │
 * │ ├─ Payment declined → Show "Card declined, try another card"             │
 * │ ├─ Expired checkout → Show "Session expired, please retry"               │
 * │ ├─ Customer cancels → Redirect to checkout with "Payment cancelled"      │
 * │ └─ Server errors (500) → Show "System error, contact support"            │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * Testing with Accept (Paymob) Test Cards:
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ Use these test cards in Accept test environment:                          │
 * │ ├─ Visa Success:     4987 6543 2109 8769   CVV: 123   Exp: 05/30        │
 * │ ├─ Mastercard Success: 5123 4567 8901 2346  CVV: 123   Exp: 05/30       │
 * │ └─ Visa Declined:    4000 0000 0000 0002   CVV: 123   Exp: 05/30        │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * Security Notes for Frontend:
 * ┌──────────────────────────────────────────────────────────────────────────┐
 * │ ⚠️ IMPORTANT: Never store card details in frontend                        │
 * │ ⚠️ Always redirect to Accept for payment (PCI-compliant)                 │
 * │ ⚠️ Validate checkoutId before calling verify endpoint                     │
 * │ ⚠️ Use HTTPS in production (required by Accept)                           │
 * │ ⚠️ Configure proper CORS (not origins = "*")                              │
 * └──────────────────────────────────────────────────────────────────────────┘
 * 
 * Additional Resources:
 * └─ Full Integration Guide: ACCEPT_PAYMOB_QUICK_START.md
 * └─ Accept API Docs: https://docs.paymob.com/
 * └─ Test Environment: https://accept.paymob.com
 * 
 * ========================================
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*") // TODO: Configure proper CORS in production
public class PaymentController {

    private static final Logger logger = LoggerFactory.getLogger(PaymentController.class);
    
    private final InitiatePaymentUseCase initiatePaymentUseCase;
    private final VerifyPaymentUseCase verifyPaymentUseCase;

    public PaymentController(
            InitiatePaymentUseCase initiatePaymentUseCase,
            VerifyPaymentUseCase verifyPaymentUseCase) {
        this.initiatePaymentUseCase = initiatePaymentUseCase;
        this.verifyPaymentUseCase = verifyPaymentUseCase;
    }

    /**
     * Initiate Payment Checkout
     * 
     * Creates a checkout session and returns redirect URL for customer
     * 
     * Request Body:
     * {
     *   "orderId": "550e8400-e29b-41d4-a716-446655440000",
     *   "paymentMethod": "VISA",
     *   "customerEmail": "customer@example.com",
     *   "shopperResultUrl": "https://yoursite.com/payment/result"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "checkoutId": "8a829449501d33d301501d3d60d101ca.uat01-vm-tx01",
     *   "redirectUrl": "https://test.oppwa.com/v1/paymentWidgets.js?checkoutId=...",
     *   "expiresInSeconds": 1800,
     *   "message": "Checkout session created successfully"
     * }
     */
    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> initiatePayment(
            @RequestBody Map<String, String> request) {
        
        try {
            String orderId = request.get("orderId");
            String paymentMethod = request.get("paymentMethod");
            String customerEmail = request.get("customerEmail");
            String shopperResultUrl = request.get("shopperResultUrl");
            
            // Validate request
            if (orderId == null || orderId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "orderId is required"
                ));
            }
            
            if (paymentMethod == null || paymentMethod.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "paymentMethod is required (VISA, MASTERCARD, MADA)"
                ));
            }
            
            if (customerEmail == null || customerEmail.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "customerEmail is required"
                ));
            }
            
            logger.info("📝 Initiating payment: orderId={}, method={}, email={}", 
                       orderId, paymentMethod, customerEmail);
            
            // Execute use case
            InitiatePaymentCommand command = new InitiatePaymentCommand(
                new OrderId(orderId),
                paymentMethod.toUpperCase(),
                customerEmail,
                shopperResultUrl
            );
            
            CheckoutResult result = initiatePaymentUseCase.execute(command);
            
            // Build response
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "checkoutId", result.checkoutId() != null ? result.checkoutId() : "",
                "redirectUrl", result.redirectUrl() != null ? result.redirectUrl() : "",
                "expiresInSeconds", result.expiresInSeconds(),
                "message", result.message() != null ? result.message() : ""
            ));
            
        } catch (IllegalArgumentException e) {
            logger.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
            
        } catch (IllegalStateException e) {
            logger.error("Payment checkout failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Unexpected error during payment initiation", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error: " + e.getMessage()
            ));
        }
    }

    /**
     * Verify Payment
     * 
     * Verifies payment status after customer returns from payment gateway
     * 
     * Request Body:
     * {
     *   "checkoutId": "8a829449501d33d301501d3d60d101ca.uat01-vm-tx01"
     * }
     * 
     * Response:
     * {
     *   "success": true,
     *   "status": "SUCCESS",
     *   "transactionId": "8a829449501d33d301501d3d60d101ca",
     *   "message": "Payment verified successfully"
     * }
     */
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyPayment(
            @RequestBody Map<String, String> request) {
        
        try {
            String checkoutId = request.get("checkoutId");
            
            // Validate request
            if (checkoutId == null || checkoutId.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "checkoutId is required"
                ));
            }
            
            logger.info("🔍 Verifying payment: checkoutId={}", checkoutId);
            
            // Execute use case
            VerifyPaymentCommand command = new VerifyPaymentCommand(checkoutId);
            PaymentVerificationResult result = verifyPaymentUseCase.execute(command);
            
            // Build response
            return ResponseEntity.ok(Map.of(
                "success", result.success(),
                "status", result.status().name(),
                "transactionId", result.transactionId() != null ? result.transactionId() : "",
                "message", result.message() != null ? result.message() : ""
            ));
            
        } catch (IllegalStateException e) {
            logger.error("Payment verification failed: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
            
        } catch (Exception e) {
            logger.error("Unexpected error during payment verification", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", "Internal server error: " + e.getMessage()
            ));
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "service", "payment-api",
            "provider", "accept-paymob"
        ));
    }
}
