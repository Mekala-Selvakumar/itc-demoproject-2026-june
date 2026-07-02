package com.retail.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Retail Order Status API — Module 4 Demo Project
 * Building with GitHub Copilot (SR/RPS NIIT)
 *
 * Run: mvn spring-boot:run
 * H2 Console: http://localhost:8090/h2-console  (JDBC URL: jdbc:h2:mem:orderdb)
 *
 * Try the bug:
 *   curl http://localhost:8090/api/orders/ORD-1042/status          → returns PENDING (wrong!)
 *   curl http://localhost:8090/api/orders/ORD-1042/status-strict   → throws NullPointerException
 *   curl http://localhost:8090/api/orders/ORD-2001/status          → returns correctly (control case)
 */

@SpringBootApplication
public class OrderStatusApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderStatusApiApplication.class, args);
    }
}
