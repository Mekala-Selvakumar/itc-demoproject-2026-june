package com.retail.order.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Shipment entity.
 *
 * ⚠️  INTENTIONAL BUG SOURCE — Module 4 lab target.
 *
 * orderId here stores the Order's ORDER CODE as a String (e.g. "ORD-1042"),
 * NOT the Order's numeric primary key (Long id).
 *
 * This mismatch is the root cause of the bug:
 *   ShipmentRepository.findByOrderId(Long orderId) — queries by Order.id (Long)
 *   but Shipment.orderId is actually storing Order.orderCode (String) data
 *   in a column that LOOKS like it should match Order.id.
 *
 * The result: the JPA query NEVER finds a matching row because it is
 * comparing a Long parameter against a column holding String "ORD-1042"
 * style values that don't parse to the same Long value as Order.id.
 */
@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ⚠️ BUG: this column actually stores order CODES like "ORD-1042" as text,
    // but is typed/queried as if it stores the numeric Order.id.
    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Column(name = "carrier", nullable = false)
    private String carrier;

    @Column(name = "tracking_number", nullable = false)
    private String trackingNumber;

    @Column(name = "shipped_at", nullable = false)
    private LocalDateTime shippedAt;

    @Column(name = "status", nullable = false)
    private String status;     // "IN_TRANSIT" | "DELIVERED"
}
