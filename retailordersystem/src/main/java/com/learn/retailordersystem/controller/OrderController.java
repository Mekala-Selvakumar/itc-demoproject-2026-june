package com.learn.retailordersystem.controller;

 import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.learn.retailordersystem.dto.OrderRequest;
import com.learn.retailordersystem.dto.OrderResponse;
import com.learn.retailordersystem.service.OrderService;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse placeOrder(@Valid @RequestBody OrderRequest request) {
        return orderService.placeOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping("/history/{customerId}")
    public List<OrderResponse> getOrderHistory(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomer(customerId);
    }

    @DeleteMapping("/{id}")
    public String cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return "Order cancelled successfully";
    }
}