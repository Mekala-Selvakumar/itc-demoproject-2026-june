package com.telecom.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Telecom Billing Dispute Spring Boot application.
 *
 * Module 2 Demo Project — Building with GitHub Copilot (SR/RPS NIIT)
 *
 * Run: mvn spring-boot:run
 * H2 Console: http://localhost:8080/h2-console
 * API Base:   http://localhost:8080/api/disputes
 */
@SpringBootApplication
public class BillingDisputeApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingDisputeApplication.class, args);
    }
}
