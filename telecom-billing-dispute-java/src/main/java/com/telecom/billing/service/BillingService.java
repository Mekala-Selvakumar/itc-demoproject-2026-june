package com.telecom.billing.service;

import com.telecom.billing.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * ⚠️  INTENTIONALLY CONTAINS CODE SMELLS for the Module 2 lab:
 *
 *   SMELL 1 — Duplicate discount logic: getDiscount() duplicated from DisputeCalculator & InvoiceService
 *   SMELL 2 — Magic numbers: 0.18 (tax rate), 500.0 (high-value threshold), 0.05 (late fee), 30 (days)
 *   SMELL 3 — Dead code: archiveOldInvoices() — never called
 *   SMELL 4 — Poor naming: inv, c, amt in method bodies
 *
 * DRY refactor target (Slide 7):
 *   getDiscount() → replace with DiscountCalculator.getDiscountRateForTier()
 */
@Service
@Slf4j
public class BillingService {

    // CODE SMELL: Duplicate tier-discount logic — third copy (see DisputeCalculator & InvoiceService)
    private double getDiscount(CustomerTier tier) {
        if (tier == CustomerTier.A) return 0.20;
        if (tier == CustomerTier.B) return 0.10;
        return 0.00;
    }

    // CODE SMELL: Magic numbers 0.18 (tax), 500 (threshold)
    public Invoice generateInvoice(Customer c, List<InvoiceLineItem> lineItems) {
        double subtotal = lineItems.stream()
                .mapToDouble(InvoiceLineItem::getTotalPrice)
                .sum();

        // CODE SMELL: magic 0.18 — should be BillingConfig.TAX_RATE
        double tax   = round(subtotal * 0.18);
        double total = round(subtotal + tax);

        double disc             = getDiscount(c.getTier());
        double discountedTotal  = round(total * (1 - disc));

        return Invoice.builder()
                .invoiceId("INV-" + UUID.randomUUID())
                .customerId(c.getCustomerId())
                .accountNumber(c.getAccountNumber())
                .billingPeriodStart(LocalDateTime.now().minusMonths(1))
                .billingPeriodEnd(LocalDateTime.now())
                .lineItems(lineItems)
                .subtotal(subtotal)
                .tax(tax)
                .totalAmount(discountedTotal)
                .status(InvoiceStatus.DRAFT)
                .issuedAt(LocalDateTime.now())
                .dueAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    // CODE SMELL: Magic numbers 0.05 (late-fee rate) and 30 (day threshold)
    public Invoice applyLateFee(Invoice inv) {
        long daysPastDue = java.time.temporal.ChronoUnit.DAYS.between(inv.getDueAt(), LocalDateTime.now());
        if (daysPastDue > 30) {
            double lateFee = round(inv.getTotalAmount() * 0.05);
            inv.setTotalAmount(round(inv.getTotalAmount() + lateFee));
            inv.setStatus(InvoiceStatus.OVERDUE);
        }
        return inv;
    }

    // CODE SMELL: Dead code — never called anywhere; left from v0.1
    public void archiveOldInvoices(String customerId) {
        log.warn("archiveOldInvoices called for {} — not implemented", customerId);
    }

    // Referenced by NotificationService as part of circular-dep chain
    public String getServiceInfo() {
        return "BillingService v1.0";
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
