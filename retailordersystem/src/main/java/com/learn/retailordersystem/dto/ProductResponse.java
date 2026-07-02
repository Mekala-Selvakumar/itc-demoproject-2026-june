package com.learn.retailordersystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long productId;

    private String productName;

    private String category;

    private Double price;

    private Integer stock;
}