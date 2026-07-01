package com.finance.fee.model;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FeeRecord {
    private String  customerId;
    private double  amount;
    private String  tier;
    private boolean isPremium;
    private double  fee;
}
