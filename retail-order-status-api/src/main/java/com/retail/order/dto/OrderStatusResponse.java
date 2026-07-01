package com.retail.order.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusResponse {
    private String orderCode;
    private String status;
    private String trackingNumber;
    private LocalDateTime lastUpdated;
}
