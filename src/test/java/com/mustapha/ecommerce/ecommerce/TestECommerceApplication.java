package com.mustapha.ecommerce.ecommerce;

import org.springframework.boot.SpringApplication;

public class TestECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.from(ECommerceApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
