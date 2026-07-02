package com.learn.retailordersystem.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerResponse {

    private Long customerId;

    private String customerName;

    private String email;

    private String mobile;

    private String city;
}
