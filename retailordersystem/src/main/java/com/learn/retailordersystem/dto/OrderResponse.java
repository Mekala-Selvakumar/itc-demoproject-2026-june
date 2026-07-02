package com.learn.retailordersystem.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long orderId;

    private Long customerId;

    private Double totalAmount;

    private String paymentStatus;

    private LocalDate orderDate;

    private List<OrderItemResponse> items;
}