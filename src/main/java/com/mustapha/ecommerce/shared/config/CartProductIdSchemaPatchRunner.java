package com.mustapha.ecommerce.shared.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time schema patch runner for production environments where Flyway is disabled.
 *
 * This runner only executes when explicitly enabled via environment variable:
 * DB_AUTO_PATCH_CART_PRODUCT_ID=true
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

        log.warn("DB auto patch enabled: verifying cart_items/order_cart_items product_id column types");

        patchTableColumnIfNeeded("cart_items", "product_id");
        patchTableColumnIfNeeded("order_cart_items", "product_id");

        log.warn("DB auto patch completed");
    }

    private void patchTableColumnIfNeeded(String tableName, String columnName) {
        try {
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
                log.warn("Schema patch skipped: {}.{} not found", tableName, columnName);
                return;
            }

            if ("varchar".equalsIgnoreCase(currentType)) {
                log.info("Schema patch not needed: {}.{} already VARCHAR", tableName, columnName);
                return;
            }

            log.warn("Applying schema patch: ALTER TABLE {} MODIFY COLUMN {} VARCHAR(255) NOT NULL", tableName, columnName);
            jdbcTemplate.execute("ALTER TABLE " + tableName + " MODIFY COLUMN " + columnName + " VARCHAR(255) NOT NULL");
            log.info("Schema patch applied: {}.{} converted from {} to VARCHAR", tableName, columnName, currentType);
        } catch (Exception ex) {
            log.error("Schema patch failed for {}.{}: {}", tableName, columnName, ex.getMessage(), ex);
            throw ex;
        }
    }
}
