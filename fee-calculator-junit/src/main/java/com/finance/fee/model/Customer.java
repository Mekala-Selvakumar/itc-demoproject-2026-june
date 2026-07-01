package com.finance.fee.model;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Customer {
    private String  customerId;
    private String  name;
    private String  tier;
    private boolean isPremium;
}
