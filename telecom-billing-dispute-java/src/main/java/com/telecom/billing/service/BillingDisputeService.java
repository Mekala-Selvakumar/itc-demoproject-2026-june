package com.telecom.billing.service;

import com.telecom.billing.audit.AuditService;
import com.telecom.billing.model.*;
import com.telecom.billing.notification.NotificationService;
import com.telecom.billing.util.DisputeCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * ⚠️  INTENTIONALLY CONTAINS CODE SMELLS for the Module 2 lab:
 *
 *   SMELL 1 — God class: handles validation, calculation, email, logging, audit, status
 *   SMELL 2 — Long method: processDispute() is ~90 lines doing everything
 *   SMELL 3 — Duplicate discount logic (same map in DisputeCalculator & InvoiceService)
 *   SMELL 4 — Magic numbers: 5.0, 0.15, 0.09, 0.80, 500.0
 *   SMELL 5 — Dead code: oldResolve() — never called from anywhere
 *   SMELL 6 — Poor naming: d, c, res, pen, ref, oa, stat in processDispute()
 *   SMELL 7 — Deep nesting: 4-level if-else for status determination
 *
 * Public methods (Slide 4 — @workspace explain BillingDisputeService and list public methods):
 *   raiseDispute()         — entry point; triggers call chain
 *   processDispute()       — God method; refactor target for Extract Method lab
 *   getDiscount()          — duplicate of DisputeCalculator.getDiscountForTier()
 *   updateDisputeStatus()  — mixes validation with business logic
 *   isHighValueDispute()   — magic number 500
 *   oldResolve()           — dead code
 *   getServiceInfo()       — used by NotificationService (circular dep chain)
 *
 * Call chain for Slide 4 demo:
 *   raiseDispute() → processDispute() → NotificationService.sendResolutionEmail()
 *                                     → AuditService.logDisputeResolution()
 */
@Service
@Slf4j
public class BillingDisputeService {

    private final DisputeCalculator  disputeCalculator;
    private final NotificationService notificationService;
    private final AuditService        auditService;

    // CODE SMELL: Duplicate tier-discount map — also in DisputeCalculator & InvoiceService
    private static final Map<CustomerTier, Double> TIER_DISCOUNTS = Map.of(
        CustomerTier.A, 0.20,
        CustomerTier.B, 0.10,
        CustomerTier.C, 0.00
    );

    public BillingDisputeService(DisputeCalculator disputeCalculator,
                                 NotificationService notificationService,
                                 AuditService auditService) {
        this.disputeCalculator   = disputeCalculator;
        this.notificationService = notificationService;
        this.auditService        = auditService;
    }

    // ── PUBLIC METHODS ────────────────────────────────────────────────────────

    /**
     * Entry point referenced in Slide 4 call-chain demo.
     * Creates and immediately processes a billing dispute.
     */
    public ProcessedDispute raiseDispute(Customer customer, Dispute dispute) {
        log.info("Raising dispute {} for customer {}", dispute.getDisputeId(), customer.getCustomerId());
        return processDispute(customer, dispute);
    }

    /**
     * CODE SMELL: Long method (~90 lines) — does validation, calculation,
     * status logic, notification, and audit all in one method.
     *
     * Lab Task 2a — extract validateDispute()
     * Lab Task 2b — extract calculatePenalty()
     * Lab Task 3b — rename all single-letter variables
     */
    public ProcessedDispute processDispute(Customer c, Dispute d) {
        log.info("Processing dispute: {}", d.getDisputeId());

        // ── Validation block ─────────────────────────────────────────────────
        // CODE SMELL: inline validation; should be validateDispute() → ValidationResult
        if (d.getDisputeId() == null || d.getDisputeId().isBlank()) {
            throw new IllegalArgumentException("Dispute ID is required");
        }
        if (d.getItems() == null || d.getItems().isEmpty()) {
            throw new IllegalArgumentException("Dispute must contain at least one item");
        }
        if (d.getTotalChargedAmount() < 0) {
            throw new IllegalArgumentException("Invalid charged amount");
        }
        if (d.getTotalExpectedAmount() < 0) {
            throw new IllegalArgumentException("Invalid expected amount");
        }
        if (d.getTotalExpectedAmount() > d.getTotalChargedAmount()) {
            throw new IllegalArgumentException("Expected amount cannot exceed charged amount");
        }
        // CODE SMELL: magic number 5.0 — should be BillingConfig.MIN_DISPUTE_AMOUNT
        double oa = d.getTotalChargedAmount() - d.getTotalExpectedAmount();
        if (oa < 5.0) {
            throw new IllegalArgumentException("Dispute amount below minimum threshold");
        }

        // ── Calculation block ─────────────────────────────────────────────────
        // CODE SMELL: duplicate discount logic
        double disc = TIER_DISCOUNTS.getOrDefault(c.getTier(), 0.0);
        // CODE SMELL: magic numbers 0.15, 0.09, 0.80
        double pen  = oa * 0.15;
        pen = pen * (1 - disc);
        if (c.isPremium()) pen = pen * (1 - 0.09);
        pen = round(pen);

        double maxRef = d.getTotalChargedAmount() * 0.80;
        double ref    = round(Math.min(oa, maxRef));

        // ── Status determination (deep nesting — code smell) ──────────────────
        DisputeStatus stat;
        if (pen > 0) {
            if (ref > 0) {
                if (d.getItems() != null) {
                    if (!d.getItems().isEmpty()) {
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

        ProcessedDispute res = ProcessedDispute.builder()
                .disputeId(d.getDisputeId())
                .status(stat)
                .penaltyAmount(pen)
                .refundAmount(ref)
                .resolutionNote(String.format("Penalty %.2f applied. Refund %.2f approved.", pen, ref))
                .processedAt(LocalDateTime.now())
                .build();

        // ── Notification (direct call inside business logic — code smell) ─────
        try {
            notificationService.sendResolutionEmail(c, res);
        } catch (Exception e) {
            log.error("Failed to send resolution email for {}", d.getDisputeId(), e);
        }

        // ── Audit ─────────────────────────────────────────────────────────────
        auditService.logDisputeResolution(d.getDisputeId(), c.getCustomerId(), res);
        log.info("Dispute {} resolved with status {}", d.getDisputeId(), stat);

        return res;
    }

    // CODE SMELL: Duplicate of DisputeCalculator.getDiscountForTier()
    public double getDiscount(CustomerTier tier) {
        if (tier == CustomerTier.A) return 0.20;
        if (tier == CustomerTier.B) return 0.10;
        return 0.00;
    }

    // CODE SMELL: Dead code — never called anywhere in the codebase
    public void oldResolve(String disputeId) {
        log.warn("oldResolve called for {} — deprecated, no-op", disputeId);
    }

    // CODE SMELL: Mixes input validation with business logic
    public boolean updateDisputeStatus(Dispute d, String newStatus, Customer c) {
        if (d == null || c == null) return false;
        if (newStatus == null || newStatus.isBlank()) return false;

        if ("ESCALATED".equals(newStatus) && c.getTier() == CustomerTier.A) {
            log.info("Escalating {} to senior agent for premium customer", d.getDisputeId());
            auditService.logEscalation(d.getDisputeId(), c.getCustomerId());
            return true;
        }

        log.info("Status updated for {} to {}", d.getDisputeId(), newStatus);
        return true;
    }

    // CODE SMELL: Magic number 500.0 — should be BillingConfig.HIGH_VALUE_THRESHOLD
    public boolean isHighValueDispute(Dispute dispute) {
        return dispute.getTotalChargedAmount() > 500.0;
    }

    // Used by NotificationService (part of circular-dep chain for Slide 5 demo)
    public String getServiceInfo() {
        return "BillingDisputeService v1.0 — telecom billing module";
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
