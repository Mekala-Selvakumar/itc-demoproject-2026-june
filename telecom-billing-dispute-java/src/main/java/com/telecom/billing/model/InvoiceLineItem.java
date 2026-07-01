package com.telecom.billing.model;

import jakarta.persistence.*;
import lombok.*;

@Entity @Table(name = "invoice_line_items")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class InvoiceLineItem {
    @Id private String lineId;
    private String description;
    private int    quantity;
    private double unitPrice;
    private double totalPrice;
}
