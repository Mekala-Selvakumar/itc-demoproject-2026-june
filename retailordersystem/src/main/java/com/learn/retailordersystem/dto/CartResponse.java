package com.learn.retailordersystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long cartId;

    private Long customerId;

    private Long productId;

    private String productName;

    private Double price;

    private Integer quantity;

    private Double totalPrice;
}