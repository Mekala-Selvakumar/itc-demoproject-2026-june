package com.learn.retailordersystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long productId;

    private String productName;

    private Integer quantity;

    private Double unitPrice;

    private Double subTotal;
}