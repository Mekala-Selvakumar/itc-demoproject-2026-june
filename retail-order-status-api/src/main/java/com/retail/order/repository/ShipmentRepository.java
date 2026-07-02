package com.retail.order.repository;

import com.retail.order.model.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * ⚠️  INTENTIONAL BUG — Module 4 lab target.
 *
 * findByOrderId(Long) queries the "order_id" column expecting it to hold
 * the numeric Order.id, but Shipment.orderId actually stores the order's
 * CODE ("ORD-1042") as a String. The two values never match, so this
 * method always returns Optional.empty() for real production data —
 * even though a shipment record DOES exist for the order.
 *
 * Lab Task: identify this root cause and fix the type mismatch.
 * See README.md for the full step-by-step debugging walkthrough.
 */
public interface ShipmentRepository extends JpaRepository<Shipment, Long> {

    // ⚠️ BUG: parameter type Long does not match the actual data stored
    // in the order_id column (String order codes like "ORD-1042").
    @Query("SELECT s FROM Shipment s WHERE s.id = :orderId")
    Optional<Shipment> findByOrderId(Long orderId);
}
