package com.telecom.billing.util;

import com.telecom.billing.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit tests for DisputeCalculator.
 *
 * Run these after EVERY Copilot refactor step to confirm behaviour is preserved.
 * (Slide 9 Checklist Item 1 — Behaviour Preserved?)
 *
 * Run: mvn test -Dtest=DisputeCalculatorTest
 */
@DisplayName("DisputeCalculator — applyPenalty()")
class DisputeCalculatorTest {

    private DisputeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new DisputeCalculator();
    }

    private Dispute makeDispute(double charged, double expected) {
        DisputeItem item = DisputeItem.builder()
                .itemId("ITEM-1")
                .description("Data plan overcharge")
                .chargedAmount(charged)
                .expectedAmount(expected)
                .build();
        return Dispute.builder()
                .disputeId("DISP-001")
                .customerId("CUST-42")
                .accountNumber("ACC-1001")
                .reason(DisputeReason.OVERCHARGE)
                .status(DisputeStatus.OPEN)
                .items(List.of(item))
                .totalChargedAmount(charged)
                .totalExpectedAmount(expected)
                .raisedAt(LocalDateTime.now())
                .build();
    }

    // ── Rejection threshold ───────────────────────────────────────────────────

    @Test
    @DisplayName("Returns REJECTED when overcharge is at or below 5.0 (minimum threshold)")
    void returns_rejected_when_overcharge_below_minimum() {
        Dispute dispute = makeDispute(105.0, 101.0);   // overcharge = 4.0
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getStatus()).isEqualTo(DisputeStatus.REJECTED);
        assertThat(result.getPenaltyAmount()).isZero();
        assertThat(result.getRefundAmount()).isZero();
    }

    @Test
    @DisplayName("Returns REJECTED when overcharge exactly equals 5.0")
    void returns_rejected_when_overcharge_exactly_at_threshold() {
        Dispute dispute = makeDispute(105.0, 100.0);   // overcharge = 5.0
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getStatus()).isEqualTo(DisputeStatus.REJECTED);
    }

    // ── Penalty calculation ───────────────────────────────────────────────────

    @Test
    @DisplayName("Calculates correct penalty for tier C, non-premium — base rate 15%")
    void calculates_penalty_tier_c_non_premium() {
        Dispute dispute = makeDispute(200.0, 100.0);   // overcharge = 100; penalty = 100 * 0.15 = 15
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getPenaltyAmount()).isEqualTo(15.0);
        assertThat(result.getStatus()).isEqualTo(DisputeStatus.RESOLVED);
    }

    @Test
    @DisplayName("Applies tier A discount (20%) to base penalty")
    void applies_tier_a_discount_to_penalty() {
        Dispute dispute = makeDispute(200.0, 100.0);   // overcharge=100; penalty=15; *0.80=12
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.A, false);

        assertThat(result.getPenaltyAmount()).isEqualTo(12.0);
    }

    @Test
    @DisplayName("Applies tier B discount (10%) to base penalty")
    void applies_tier_b_discount_to_penalty() {
        Dispute dispute = makeDispute(200.0, 100.0);   // overcharge=100; penalty=15; *0.90=13.5
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.B, false);

        assertThat(result.getPenaltyAmount()).isEqualTo(13.5);
    }

    @Test
    @DisplayName("Applies premium surcharge discount on top of tier A discount")
    void applies_premium_discount_stacked_on_tier_a() {
        Dispute dispute = makeDispute(200.0, 100.0);
        // overcharge=100; penalty=15; tier A: *0.80=12; premium: *0.90=10.8
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.A, true);

        assertThat(result.getPenaltyAmount()).isEqualTo(10.8);
    }

    // ── Refund capping ───────────────────────────────────────────────────────

    @Test
    @DisplayName("Caps refund at 80% of total charged amount")
    void caps_refund_at_80_percent_of_charged_amount() {
        Dispute dispute = makeDispute(100.0, 5.0);     // overcharge=95; maxRef=80; refund=80
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getRefundAmount()).isEqualTo(80.0);
    }

    @Test
    @DisplayName("Refund equals overcharge when below the 80% cap")
    void refund_equals_overcharge_when_below_cap() {
        Dispute dispute = makeDispute(200.0, 100.0);   // overcharge=100; maxRef=160; refund=100
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getRefundAmount()).isEqualTo(100.0);
    }

    // ── Status determination ─────────────────────────────────────────────────

    @Test
    @DisplayName("Returns ESCALATED when dispute has no items")
    void returns_escalated_when_no_items() {
        Dispute dispute = Dispute.builder()
                .disputeId("DISP-001")
                .customerId("CUST-42")
                .accountNumber("ACC-1001")
                .reason(DisputeReason.OVERCHARGE)
                .status(DisputeStatus.OPEN)
                .items(List.of())
                .totalChargedAmount(200.0)
                .totalExpectedAmount(100.0)
                .raisedAt(LocalDateTime.now())
                .build();

        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);
        assertThat(result.getStatus()).isEqualTo(DisputeStatus.ESCALATED);
    }

    // ── Result fields ────────────────────────────────────────────────────────

    @Test
    @DisplayName("DisputeId is preserved in result")
    void dispute_id_is_preserved_in_result() {
        Dispute dispute = makeDispute(200.0, 100.0);
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getDisputeId()).isEqualTo("DISP-001");
    }

    @Test
    @DisplayName("ProcessedAt timestamp is set")
    void processed_at_is_set() {
        Dispute dispute = makeDispute(200.0, 100.0);
        ProcessedDispute result = calculator.applyPenalty(dispute, CustomerTier.C, false);

        assertThat(result.getProcessedAt()).isNotNull();
    }

    // ── getDiscountForTier ───────────────────────────────────────────────────

    @Test @DisplayName("getDiscountForTier returns 0.20 for tier A")
    void discount_tier_a() { assertThat(calculator.getDiscountForTier(CustomerTier.A)).isEqualTo(0.20); }

    @Test @DisplayName("getDiscountForTier returns 0.10 for tier B")
    void discount_tier_b() { assertThat(calculator.getDiscountForTier(CustomerTier.B)).isEqualTo(0.10); }

    @Test @DisplayName("getDiscountForTier returns 0.00 for tier C")
    void discount_tier_c() { assertThat(calculator.getDiscountForTier(CustomerTier.C)).isEqualTo(0.00); }
}
