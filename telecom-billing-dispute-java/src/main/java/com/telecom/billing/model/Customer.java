package com.telecom.billing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @Column(name = "customer_id")
    private String customerId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerTier tier;

    @Column(name = "account_number", nullable = false, unique = true)
    private String accountNumber;

    @Column(name = "is_premium", nullable = false)
    private boolean isPremium;
}
