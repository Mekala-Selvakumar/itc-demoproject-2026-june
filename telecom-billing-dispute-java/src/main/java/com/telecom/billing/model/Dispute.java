package com.telecom.billing.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "disputes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dispute {

    @Id
    @Column(name = "dispute_id")
    private String disputeId;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "account_number", nullable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeReason reason;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DisputeStatus status;

    @OneToMany(mappedBy = "dispute", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DisputeItem> items;

    @Column(name = "total_charged_amount", nullable = false)
    private double totalChargedAmount;

    @Column(name = "total_expected_amount", nullable = false)
    private double totalExpectedAmount;

    @Column(name = "raised_at", nullable = false)
    private LocalDateTime raisedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "agent_id")
    private String agentId;

    @Column(length = 1000)
    private String notes;
}
