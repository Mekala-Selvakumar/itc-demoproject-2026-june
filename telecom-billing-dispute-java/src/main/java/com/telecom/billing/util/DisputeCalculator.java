package com.telecom.billing.util;

import com.telecom.billing.model.CustomerTier;
import com.telecom.billing.model.Dispute;
import com.telecom.billing.model.ProcessedDispute;
import com.telecom.billing.model.DisputeStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ⚠️  INTENTIONALLY CONTAINS CODE SMELLS for the Module 2 lab:
 *
 *   SMELL 1 — Magic numbers: 0.15, 0.09, 0.80, 5.0 scattered everywhere
 *   SMELL 2 — Poor naming: calc(), x, d, f, tmp, t
 *   SMELL 3 — Duplicate discount logic (same logic in BillingService & InvoiceService)
 *   SMELL 4 — Long method: applyPenalty() does validation + calc + status + result build
 *   SMELL 5 — Dead code: legacyCalcPenalty() — never called
 *   SMELL 6 — Deep nesting: 4-level if-else for status determination
 *
 * Lab Slide References:
 *   Slide 4 — @workspace which files import or call DisputeCalculator.applyPenalty()?
 *   Slide 5 — @workspace which internal modules does DisputeCalculator depend on?
 *   Slide 6 — Ask Copilot to identify all 6 code smells above
 *   Slide 7 — Refactor: extract calculatePenalty(), replace magic numbers, rename vars
 */
@Component
public class DisputeCalculator {

    // CODE SMELL: Duplicate tier-discount map — identical copy in BillingService & InvoiceService
    private static final Map<CustomerTier, Double> t = Map.of(
        CustomerTier.A, 0.20,
        CustomerTier.B, 0.10,
        CustomerTier.C, 0.00
    );

    /**
     * CODE SMELL: Poor naming — what do x, d, f mean?
     * Applies discount (d) to amount (x), with optional premium flag (f).
     */
    private double calc(double x, double d, boolean f) {
        double tmp = x * d;
        if (f) tmp = tmp * 0.9;    // CODE SMELL: magic 0.9; f = isPremium?
        return tmp;
    }

    /**
     * CODE SMELL: Long method — does validation, calculation, status logic, and result building.
     * Should be split into: validateDispute(), calculatePenalty(), determineStatus(), buildResult()
     *
     * @param dispute the billing dispute to process
     * @param tier    the customer's tier
     * @param flag1   whether the customer is premium (CODE SMELL: poor parameter name)
     */
    public ProcessedDispute applyPenalty(Dispute dispute, CustomerTier tier, boolean flag1) {

        // Step 1 — guard (CODE SMELL: magic number 5.0)
        double oa = dispute.getTotalChargedAmount() - dispute.getTotalExpectedAmount();
        if (oa <= 5.0) {
            return ProcessedDispute.builder()
                    .disputeId(dispute.getDisputeId())
                    .status(DisputeStatus.REJECTED)
                    .penaltyAmount(0)
                    .refundAmount(0)
                    .resolutionNote("Overcharge amount too small to process")
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        // Step 2 — calculate penalty (CODE SMELL: magic number 0.15)
        double pen = oa * 0.15;

        // Step 3 — apply tier discount via poorly-named calc()
        double disc = t.getOrDefault(tier, 0.0);
        pen = calc(pen, 1 - disc, flag1);

        // Step 4 — cap refund (CODE SMELL: magic number 0.80)
        double maxRef   = dispute.getTotalChargedAmount() * 0.80;
        double ref      = Math.min(oa, maxRef);

        // Step 5 — determine status (CODE SMELL: deep 4-level nesting)
        DisputeStatus stat;
        if (pen > 0) {
            if (ref > 0) {
                if (dispute.getItems() != null) {
                    if (!dispute.getItems().isEmpty()) {
                        stat = DisputeStatus.RESOLVED;
                    } else {
                        stat = DisputeStatus.ESCALATED;
                    }
                } else {
                    stat = DisputeStatus.ESCALATED;
                }
            } else {
                stat = DisputeStatus.ESCALATED;
            }
        } else {
            stat = DisputeStatus.ESCALATED;
        }

        // Step 6 — round and build result
        pen = round(pen);
        ref = round(ref);

        return ProcessedDispute.builder()
                .disputeId(dispute.getDisputeId())
                .status(stat)
                .penaltyAmount(pen)
                .refundAmount(ref)
                .resolutionNote(String.format("Penalty %.2f applied. Refund %.2f approved.", pen, ref))
                .processedAt(LocalDateTime.now())
                .build();
    }

    // CODE SMELL: Dead code — this method is never called anywhere
    public double legacyCalcPenalty(double amount) {
        return amount * 0.12;   // old rate, superseded by applyPenalty
    }

    // CODE SMELL: Duplicate of getDiscountForTier() in BillingService & InvoiceService
    public double getDiscountForTier(CustomerTier tier) {
        if (tier == CustomerTier.A) return 0.20;
        if (tier == CustomerTier.B) return 0.10;
        return 0.00;
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
