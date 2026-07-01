package com.telecom.billing.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "invoices")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Invoice {
    @Id private String invoiceId;
    private String customerId;
    private String accountNumber;
    private LocalDateTime billingPeriodStart;
    private LocalDateTime billingPeriodEnd;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    @ToString.Exclude @EqualsAndHashCode.Exclude
    private List<InvoiceLineItem> lineItems;
    private double subtotal;
    private double tax;
    private double totalAmount;
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime dueAt;
}
