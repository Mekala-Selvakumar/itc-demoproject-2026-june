package com.telecom.billing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "dispute_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DisputeItem {

    @Id
    @Column(name = "item_id")
    private String itemId;

    @Column(nullable = false)
    private String description;

    @Column(name = "charged_amount", nullable = false)
    private double chargedAmount;

    @Column(name = "expected_amount", nullable = false)
    private double expectedAmount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispute_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Dispute dispute;
}
