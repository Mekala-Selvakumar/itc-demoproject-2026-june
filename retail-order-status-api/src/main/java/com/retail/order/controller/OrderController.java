package com.retail.order.controller;

import com.retail.order.dto.OrderStatusResponse;
import com.retail.order.service.OrderStatusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST API for retail order status.
 *
 * Endpoints:
 *   GET /api/orders/{orderCode}/status         — production endpoint (silent fallback to PENDING)
 *   GET /api/orders/{orderCode}/status-strict   — debug endpoint (throws the real NullPointerException)
 *
 * Module 4 Lab — Debugging and Error Resolution.
 * Try:
 *   curl http://localhost:8080/api/orders/ORD-1042/status
 *   curl http://localhost:8080/api/orders/ORD-1042/status-strict
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderStatusService orderStatusService;

    /** Production endpoint — line 31 in the Module 4 stack-trace slide refers to this method. */
    @GetMapping("/{orderCode}/status")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable String orderCode) {
        OrderStatusResponse response = orderStatusService.getStatus(orderCode);
        return ResponseEntity.ok(response);
    }

    /** Debug endpoint — throws the exact NullPointerException shown in the PPT. */
    @GetMapping("/{orderCode}/status-strict")
    public ResponseEntity<OrderStatusResponse> getOrderStatusStrict(@PathVariable String orderCode) {
        OrderStatusResponse response = orderStatusService.getStatusStrict(orderCode);
        return ResponseEntity.ok(response);
    }
}
