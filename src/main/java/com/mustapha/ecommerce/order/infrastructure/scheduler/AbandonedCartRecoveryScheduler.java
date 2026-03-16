package com.mustapha.ecommerce.order.infrastructure.scheduler;

import com.mustapha.ecommerce.user.application.port.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Abandoned Cart Recovery System
 * Sends reminder emails to customers who abandoned their cart
 * Typical recovery rate: 10-30% of abandoned carts
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AbandonedCartRecoveryScheduler {

    private final EntityManager entityManager;
    private final EmailService emailService;

    /**
     * Run every hour to find and recover abandoned carts
     */
    @Scheduled(fixedRate = 3600000) // Every 1 hour
    @Transactional
    public void recoverAbandonedCarts() {
        log.info("🛒 Starting abandoned cart recovery process...");

        if (!isReminderTableAvailable()) {
            log.warn("⚠️ Skipping abandoned cart recovery: table 'abandoned_cart_reminders' does not exist");
            return;
        }

        try {
            // Find carts abandoned more than 1 hour ago but less than 24 hours
            List<AbandonedCartInfo> abandonedCarts = findAbandonedCarts();

            if (abandonedCarts.isEmpty()) {
                log.info("✅ No abandoned carts to recover");
                return;
            }

            log.info("📧 Found {} abandoned carts. Sending recovery emails...", abandonedCarts.size());

            int emailsSent = 0;
            for (AbandonedCartInfo cart : abandonedCarts) {
                try {
                    sendAbandonedCartEmail(cart);
                    markReminderSent(cart.customerId());
                    emailsSent++;
                } catch (Exception e) {
                    log.error("❌ Failed to send abandoned cart email to customer {}: {}", 
                        cart.customerId(), e.getMessage());
                }
            }

            log.info("✅ Sent {} abandoned cart recovery emails", emailsSent);

        } catch (Exception e) {
            log.error("❌ Error in abandoned cart recovery: {}", e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private List<AbandonedCartInfo> findAbandonedCarts() {
        String sql = """
            SELECT 
                c.user_id,
                cu.email,
                cu.username,
                COUNT(ci.id) as item_count,
                SUM(ci.quantity * p.price) as cart_total,
                c.last_updated_at as last_updated
            FROM carts c
            JOIN cart_items ci ON c.id = ci.cart_id
            JOIN products p ON ci.product_id = p.id
            JOIN users cu ON c.user_id = cu.id
            LEFT JOIN abandoned_cart_reminders acr ON c.user_id = acr.customer_id 
                AND acr.sent_at > DATE_SUB(NOW(), INTERVAL 7 DAY)
            WHERE c.last_updated_at BETWEEN DATE_SUB(NOW(), INTERVAL 24 HOUR)
                                        AND DATE_SUB(NOW(), INTERVAL 1 HOUR)
              AND acr.id IS NULL
              AND cu.email IS NOT NULL
            GROUP BY c.user_id, cu.email, cu.username, c.last_updated_at
            HAVING item_count > 0
            ORDER BY cart_total DESC
            LIMIT 100
            """;

        Query query = entityManager.createNativeQuery(sql);
        List<Object[]> results = query.getResultList();

        return results.stream()
            .map(row -> new AbandonedCartInfo(
                ((Number) row[0]).longValue(),  // customerId
                (String) row[1],                 // email
                (String) row[2],                 // username (used as firstName)
                ((Number) row[3]).intValue(),    // itemCount
                ((Number) row[4]).doubleValue(), // cartTotal
                ((java.sql.Timestamp) row[5]).toLocalDateTime() // lastUpdated
            ))
            .toList();
    }

    private void sendAbandonedCartEmail(AbandonedCartInfo cart) {
        log.info("📧 Sending abandoned cart email to {} (cart value: {} EGP)", 
            cart.email(), cart.cartTotal());

        String subject = "🛒 Your cart is waiting! Complete your order now";
        
        String htmlContent = String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <h2 style="color: #2c5282;">Hi %s! 👋</h2>
                    
                    <p>You left <strong>%d item(s)</strong> in your cart worth <strong>%.2f EGP</strong>.</p>
                    
                    <p>Your items are still available, but they won't last forever! Complete your order now before they're gone.</p>
                    
                    <div style="background-color: #f7fafc; padding: 20px; border-radius: 8px; margin: 20px 0;">
                        <h3 style="margin-top: 0; color: #2c5282;">Special Offer</h3>
                        <p style="font-size: 18px; color: #e53e3e; font-weight: bold;">
                            🎉 Get 10%% OFF if you complete your order today!
                        </p>
                        <p style="font-size: 14px; color: #718096;">
                            Use code: <strong>COMEBACK10</strong> at checkout
                        </p>
                    </div>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="https://your-store.com/cart" 
                           style="display: inline-block; padding: 15px 40px; background-color: #3182ce; 
                                  color: white; text-decoration: none; border-radius: 5px; font-weight: bold;">
                            Complete Your Order →
                        </a>
                    </div>
                    
                    <p style="font-size: 12px; color: #718096; margin-top: 30px;">
                        This offer expires in 24 hours. Don't miss out!
                    </p>
                    
                    <hr style="border: none; border-top: 1px solid #e2e8f0; margin: 30px 0;">
                    
                    <p style="font-size: 12px; color: #718096;">
                        Need help? Reply to this email or contact our support team.<br>
                        Not interested? <a href="#" style="color: #3182ce;">Unsubscribe</a>
                    </p>
                </div>
            </body>
            </html>
            """, cart.firstName(), cart.itemCount(), cart.cartTotal());

        emailService.sendTransactionalEmail(
            cart.email(),
            subject,
            htmlContent
        );
    }

    private void markReminderSent(Long customerId) {
        // H2-compatible INSERT (no upsert needed - we only insert once per 7 days per customer)
        // MySQL version would use: ON DUPLICATE KEY UPDATE sent_at = NOW()
        String sql = """
            INSERT INTO abandoned_cart_reminders (customer_id, sent_at)
            VALUES (:customerId, NOW())
            """;

        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("customerId", customerId);
        query.executeUpdate();
    }

    private boolean isReminderTableAvailable() {
        String sql = """
            SELECT COUNT(*)
            FROM information_schema.tables
            WHERE table_schema = DATABASE()
              AND table_name = 'abandoned_cart_reminders'
            """;

        try {
            Number count = (Number) entityManager.createNativeQuery(sql).getSingleResult();
            return count != null && count.longValue() > 0;
        } catch (Exception ex) {
            log.warn("Unable to verify 'abandoned_cart_reminders' table availability. Skipping recovery. Cause: {}", ex.getMessage());
            return false;
        }
    }

    private record AbandonedCartInfo(
        Long customerId,
        String email,
        String firstName,
        int itemCount,
        double cartTotal,
        LocalDateTime lastUpdated
    ) {}
}
