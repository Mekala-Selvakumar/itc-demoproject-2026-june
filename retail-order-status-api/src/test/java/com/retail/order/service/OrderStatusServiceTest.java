package com.retail.order.service;

import com.retail.order.dto.OrderStatusResponse;
import com.retail.order.exception.OrderNotFoundException;
import com.retail.order.model.Order;
import com.retail.order.model.Shipment;
import com.retail.order.repository.OrderRepository;
import com.retail.order.repository.ShipmentRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Existing regression test suite for OrderStatusService.
 *
 * Module 4 Lab — run `mvn test` after applying your fix to confirm:
 *   1. No regressions (all pre-existing tests still pass)
 *   2. The bug is actually fixed (see the new test added at the bottom)
 *
 * Run: mvn test -Dtest=OrderStatusServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OrderStatusService Tests")
class OrderStatusServiceTest {

    @Mock private OrderRepository    orderRepository;
    @Mock private ShipmentRepository shipmentRepository;
    @InjectMocks private OrderStatusService service;

    private Order makeOrder(Long id, String code) {
        return Order.builder()
                .id(id)
                .orderCode(code)
                .customerId("CUST-501")
                .status("PENDING")
                .createdAt(LocalDateTime.now().minusDays(2))
                .totalAmount(149.99)
                .build();
    }

    private Shipment makeShipment(Long id, String orderIdValue, String status) {
        return Shipment.builder()
                .id(id)
                .orderId(orderIdValue)
                .carrier("BlueDart")
                .trackingNumber("BD9988771122")
                .shippedAt(LocalDateTime.now().minusDays(1))
                .status(status)
                .build();
    }

    // ── Existing passing tests (these must NOT break) ─────────────────────────

    @Test
    @DisplayName("Throws OrderNotFoundException when order code does not exist")
    void throws_when_order_not_found() {
        when(orderRepository.findByOrderCode("ORD-9999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getStatus("ORD-9999"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("ORD-9999");
    }

    @Test
    @DisplayName("Returns PENDING when no shipment exists at all (legitimate case)")
    void returns_pending_when_no_shipment_exists() {
        Order order = makeOrder(2L, "ORD-2001");
        when(orderRepository.findByOrderCode("ORD-2001")).thenReturn(Optional.of(order));
        when(shipmentRepository.findByOrderId(2L)).thenReturn(Optional.empty());

        OrderStatusResponse result = service.getStatus("ORD-2001");

        assertThat(result.getStatus()).isEqualTo("PENDING");
        assertThat(result.getOrderCode()).isEqualTo("ORD-2001");
    }

    @Test
    @DisplayName("Maps shipment status IN_TRANSIT to order status SHIPPED")
    void maps_in_transit_to_shipped() {
        Order order = makeOrder(1L, "ORD-1042");
        Shipment shipment = makeShipment(1L, "1", "IN_TRANSIT"); // correctly-typed test data

        when(orderRepository.findByOrderCode("ORD-1042")).thenReturn(Optional.of(order));
        when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.of(shipment));

        OrderStatusResponse result = service.getStatus("ORD-1042");

        assertThat(result.getStatus()).isEqualTo("SHIPPED");
        assertThat(result.getTrackingNumber()).isEqualTo("BD9988771122");
    }

    @Test
    @DisplayName("Maps shipment status DELIVERED to order status DELIVERED")
    void maps_delivered_to_delivered() {
        Order order = makeOrder(3L, "ORD-3055");
        Shipment shipment = makeShipment(2L, "3", "DELIVERED");

        when(orderRepository.findByOrderCode("ORD-3055")).thenReturn(Optional.of(order));
        when(shipmentRepository.findByOrderId(3L)).thenReturn(Optional.of(shipment));

        OrderStatusResponse result = service.getStatus("ORD-3055");

        assertThat(result.getStatus()).isEqualTo("DELIVERED");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // 🎯 THE BUG-VALIDATION TEST — this test demonstrates the actual production bug.
    //
    // It mirrors the REAL seed data from data.sql: a Shipment row where
    // orderId is stored as the STRING order code "ORD-1042" instead of the
    // Order's numeric id (1L).
    //
    // BEFORE the fix: this test documents the buggy behaviour (status incorrectly
    // falls back to PENDING even though a shipment exists).
    //
    // AFTER the fix: update this test's expectations to assert status == "SHIPPED"
    // and re-run — it should now reflect CORRECT behaviour.
    // ══════════════════════════════════════════════════════════════════════════

    @Test
    @DisplayName("🐛 BUG: shipment exists but is not found due to orderId type mismatch")
    void demonstrates_the_orderId_type_mismatch_bug() {
        Order order = makeOrder(1L, "ORD-1042");

        // This is what the repository actually returns in production:
        // findByOrderId(1L) queries by Long, but real data has orderId
        // stored as the STRING "ORD-1042" — so in production this NEVER matches
        // and the real repository call returns Optional.empty().
        when(orderRepository.findByOrderCode("ORD-1042")).thenReturn(Optional.of(order));
        when(shipmentRepository.findByOrderId(1L)).thenReturn(Optional.empty()); // simulates the bug

        OrderStatusResponse result = service.getStatus("ORD-1042");

        // Documents CURRENT (buggy) behaviour — remove/update this assertion
        // once your fix makes findByOrderId() correctly resolve the shipment.
        assertThat(result.getStatus()).isEqualTo("PENDING");

        // 🎯 LAB GOAL: after fixing ShipmentRepository + data alignment,
        // a corrected version of this test should instead mock the repository
        // to return Optional.of(shipment) for the correct lookup key, and
        // assert result.getStatus() == "SHIPPED".
    }
}
