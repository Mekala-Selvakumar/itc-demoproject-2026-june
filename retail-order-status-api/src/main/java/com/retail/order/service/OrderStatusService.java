package com.retail.order.service;

import com.retail.order.dto.OrderStatusResponse;
import com.retail.order.exception.OrderNotFoundException;
import com.retail.order.model.Order;
import com.retail.order.model.Shipment;
import com.retail.order.repository.OrderRepository;
import com.retail.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * OrderStatusService — resolves the current status of a retail order.
 *
 * ⚠️  INTENTIONAL BUG — Module 4 lab target.
 *
 * Line numbers below are deliberately aligned with the Module 4 PPT
 * stack-trace slide:
 *   java.lang.NullPointerException: Cannot invoke "Shipment.getStatus()"
 *   because "shipment" is null
 *     at OrderStatusService.resolveStatus(OrderStatusService.java:42)
 *     at OrderStatusService.getStatus(OrderStatusService.java:28)
 *
 * Root cause: ShipmentRepository.findByOrderId(Long) never matches
 * because Shipment.orderId actually stores String order codes, not the
 * numeric Order.id. See ShipmentRepository.java and README.md.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderStatusService {

    private final OrderRepository    orderRepository;
    private final ShipmentRepository shipmentRepository;

    /**
     * Public entry point — called by OrderController.
     * Line 28 in the stack trace.
     */
    public OrderStatusResponse getStatus(String orderCode) {
        log.debug("Resolving status for orderId={}", orderCode);

        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));

        return resolveStatus(order);
    }

    /**
     * Determines the final status by combining order data with shipment data.
     * Line 42 — the NullPointerException originates here.
     *
     * ⚠️ BUG: shipmentRepository.findByOrderId(order.getId()) is called
     * with the numeric Order.id (Long), but the underlying query compares
     * against Shipment.orderId which actually holds String order CODES.
     * The query always returns Optional.empty() for real data, so
     * .orElse(null) silently produces shipment = null here.
     */
    private OrderStatusResponse resolveStatus(Order order) {
        // ── THIS LINE CONTAINS THE BUG ────────────────────────────────────────
        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .orElse(null);

        log.debug("findByOrderId({}) → {}", order.getId(),
                shipment == null ? "0 rows" : "1 row found");

        if (shipment == null) {
            // ⚠️ SILENT FALLBACK — this is what masks the real bug as "PENDING"
            log.error("shipment is null for orderId={}", order.getOrderCode());
            log.warn("Falling back to status=PENDING for orderCode={}", order.getOrderCode());

            return OrderStatusResponse.builder()
                    .orderCode(order.getOrderCode())
                    .status("PENDING")
                    .trackingNumber(null)
                    .lastUpdated(order.getCreatedAt())
                    .build();
        }

        // This line is what SHOULD execute for ORD-1042, but never does
        // because shipment is always null due to the bug above.
        String resolvedStatus = mapShipmentStatusToOrderStatus(shipment.getStatus());

        return OrderStatusResponse.builder()
                .orderCode(order.getOrderCode())
                .status(resolvedStatus)
                .trackingNumber(shipment.getTrackingNumber())
                .lastUpdated(shipment.getShippedAt())
                .build();
    }

    private String mapShipmentStatusToOrderStatus(String shipmentStatus) {
        return switch (shipmentStatus) {
            case "IN_TRANSIT" -> "SHIPPED";
            case "DELIVERED"  -> "DELIVERED";
            default            -> "PROCESSING";
        };
    }

    /**
     * Strict variant — does NOT silently fall back to PENDING.
     * Used by GET /api/orders/{orderId}/status-strict so participants can
     * see the EXACT NullPointerException shown in the Module 4 PPT slides
     * before they discover the silent-fallback version above.
     *
     * Line 42-equivalent: shipment.getStatus() throws NPE because
     * shipment is null (same root cause — the orderId type mismatch).
     */
    public OrderStatusResponse getStatusStrict(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new OrderNotFoundException(orderCode));

        Shipment shipment = shipmentRepository.findByOrderId(order.getId())
                .orElse(null);

        // ⚠️ THIS LINE THROWS THE EXACT NullPointerException FROM THE PPT:
        // "Cannot invoke \"Shipment.getStatus()\" because \"shipment\" is null"
        String resolvedStatus = mapShipmentStatusToOrderStatus(shipment.getStatus());

        return OrderStatusResponse.builder()
                .orderCode(order.getOrderCode())
                .status(resolvedStatus)
                .trackingNumber(shipment.getTrackingNumber())
                .lastUpdated(shipment.getShippedAt())
                .build();
    }
}
