package com.mustapha.ecommerce.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time schema patch runner for environments where Flyway is disabled.
 *
 * Enable with: DB_AUTO_PATCH_CART_PRODUCT_ID=true
 */
@Component
public class CartProductIdSchemaPatchRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(CartProductIdSchemaPatchRunner.class);

    private final JdbcTemplate jdbcTemplate;

    @Value("${db.auto-patch.cart-product-id:false}")
    private boolean enabled;

    public CartProductIdSchemaPatchRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }

        log.warn("DB auto patch enabled: validating cart/product identifier column types");

        patchVarcharColumnIfNeeded("cart_items", "product_id", false);
        patchVarcharColumnIfNeeded("order_cart_items", "product_id", false);
        patchVarcharColumnIfNeeded("carts", "user_id", true);
        patchVarcharColumnIfNeeded("product_reviews", "product_id", false);
        patchVarcharColumnIfNeeded("product_reviews", "customer_id", false);

        log.warn("DB auto patch completed");
    }

    private void patchVarcharColumnIfNeeded(String tableName, String columnName, boolean nullable) {
        try {
            Integer tableExists = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.TABLES
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                """,
                Integer.class,
                tableName
            );

            if (tableExists == null || tableExists == 0) {
                log.warn("Schema patch skipped: table '{}' not found", tableName);
                return;
            }

            String currentType = jdbcTemplate.queryForObject(
                """
                SELECT DATA_TYPE
                FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = ?
                  AND COLUMN_NAME = ?
                """,
                String.class,
                tableName,
                columnName
            );

            if (currentType == null) {
                log.warn("Schema patch skipped: column '{}.{}' not found", tableName, columnName);
                return;
            }

            if ("varchar".equalsIgnoreCase(currentType)) {
                log.info("Schema patch not needed: {}.{} already VARCHAR", tableName, columnName);
                return;
            }

            String nullability = nullable ? "NULL" : "NOT NULL";
            log.warn("Applying schema patch: ALTER TABLE {} MODIFY COLUMN {} VARCHAR(255) {}", tableName, columnName, nullability);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " VARCHAR(255) " + nullability);
            log.info("Schema patch applied: {}.{} converted from {} to VARCHAR", tableName, columnName, currentType);

        } catch (Exception ex) {
            log.error("Schema patch failed for {}.{}: {}", tableName, columnName, ex.getMessage(), ex);
            throw ex;
        }
    }
}
