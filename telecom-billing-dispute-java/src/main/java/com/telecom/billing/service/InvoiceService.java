package com.telecom.billing.service;

import com.telecom.billing.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * ⚠️  INTENTIONALLY CONTAINS CODE SMELLS for the Module 2 lab:
 *
 *   SMELL 1 — Duplicate discount logic: calcDiscount() — third copy
 *   SMELL 2 — Poor naming: d, t, amt, res in method bodies
 *   SMELL 3 — Magic numbers: 0.18 (tax), 30 (days), 0.05 (late fee)
 *   SMELL 4 — Method does too much: formatInvoiceSummary() validates + calculates + formats
 *
 * DRY refactor target (Slide 7):
 *   calcDiscount() → replace with DiscountCalculator.getDiscountRateForTier()
 */
@Service
@Slf4j
public class InvoiceService {

    // CODE SMELL: Third copy of the same discount logic
    // See also: BillingService.getDiscount(), DisputeCalculator.getDiscountForTier()
    private double calcDiscount(CustomerTier t) {
        if (t == CustomerTier.A)      return 0.20;
        else if (t == CustomerTier.B) return 0.10;
        else                          return 0.00;
    }

    // CODE SMELL: Poor naming — d, t, amt, res
    public Invoice applyDiscount(Invoice d, CustomerTier t) {
        double disc = calcDiscount(t);
        double amt  = d.getTotalAmount();
        double res  = round(amt * (1 - disc));
        d.setTotalAmount(res);
        return d;
    }

    // CODE SMELL: Does three things — validates, calculates, and formats
    public String formatInvoiceSummary(Invoice inv, CustomerTier tier) {
        if (inv == null) throw new IllegalArgumentException("Invoice is required");
        if (inv.getLineItems() == null || inv.getLineItems().isEmpty())
            throw new IllegalArgumentException("Invoice has no line items");

        double disc    = calcDiscount(tier);
        double savings = round(inv.getTotalAmount() * disc);

        // CODE SMELL: magic 0.18 — same tax rate duplicated from BillingService
        double taxAmount = round(inv.getSubtotal() * 0.18);

        return String.format(
            "Invoice: %s%nSubtotal: %.2f%nTax (18%%): %.2f%nDiscount (%.0f%%): -%.2f%nTotal: %.2f",
            inv.getInvoiceId(),
            inv.getSubtotal(),
            taxAmount,
            disc * 100,
            savings,
            inv.getTotalAmount()
        );
    }

    // CODE SMELL: Duplicate of BillingService.applyLateFee()
    public Invoice addPenaltyForLatePayment(Invoice inv) {
        long daysPastDue = ChronoUnit.DAYS.between(inv.getDueAt(), LocalDateTime.now());
        // CODE SMELL: magic 30 and 0.05
        if (daysPastDue > 30) {
            inv.setTotalAmount(round(inv.getTotalAmount() * 1.05));
            inv.setStatus(InvoiceStatus.OVERDUE);
        }
        return inv;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
