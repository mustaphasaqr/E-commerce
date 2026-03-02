package com.mustapha.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * E-Commerce Application Bootstrap
 * Responsibility: Application entry point ONLY
 * 
 * Architecture Overview:
 * - order/          : Order Bounded Context (Aggregate)
 * - user/           : User Bounded Context (Aggregate)
 * - product/        : Product Bounded Context (Aggregate)
 * - shared/         : Cross-Cutting Kernel
 * - infrastructure/ : Global Infrastructure
 * - config/         : Composition Root (Dependency Injection)
 */
@SpringBootApplication
@EntityScan(basePackages = {
    "com.mustapha.ecommerce.*.infrastructure.persistence.entity",
    "com.mustapha.ecommerce.*.domain.model"
})
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }

}
