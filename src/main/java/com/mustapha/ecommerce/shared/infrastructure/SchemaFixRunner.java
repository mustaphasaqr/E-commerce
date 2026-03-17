package com.mustapha.ecommerce.shared.infrastructure;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * One-time schema fix: the order_items table may have both 'price' and 'unit_price' columns
 * due to a JPA column mapping change. This ensures the legacy 'price' column won't block INSERTs.
 */
@Component
public class SchemaFixRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(SchemaFixRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public SchemaFixRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        fixOrderItemsPriceColumn();
    }

    private void fixOrderItemsPriceColumn() {
        try {
            // Check if the legacy 'price' column exists in order_items
            var columns = jdbcTemplate.queryForList(
                "SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_NAME = 'order_items' AND COLUMN_NAME = 'price' AND TABLE_SCHEMA = DATABASE()"
            );
            if (!columns.isEmpty()) {
                // Make the legacy column nullable with a default so it doesn't block INSERTs
                jdbcTemplate.execute(
                    "ALTER TABLE order_items MODIFY COLUMN price DECIMAL(19,2) NULL DEFAULT 0.00"
                );
                logger.info("Schema fix: made order_items.price column nullable with default 0.00");
            }
        } catch (Exception e) {
            logger.warn("Schema fix for order_items.price skipped: {}", e.getMessage());
        }
    }
}
