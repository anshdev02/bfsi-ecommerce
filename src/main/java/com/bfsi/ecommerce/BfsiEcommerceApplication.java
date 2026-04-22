package com.bfsi.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BfsiEcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BfsiEcommerceApplication.class, args);
    }
}
