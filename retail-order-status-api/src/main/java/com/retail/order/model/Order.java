package com.retail.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Order entity.
 *
 * NOTE: id is a Long (standard JPA auto-generated primary key).
 * The business-facing order code (e.g. "ORD-1042") is stored separately
 * as orderCode — this is the field customers and the API use.
 */
@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true)
    private String orderCode;          // e.g. "ORD-1042"

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String status;             // "PENDING" | "PROCESSING" | "SHIPPED" | "DELIVERED"

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "total_amount", nullable = false)
    private double totalAmount;
}
