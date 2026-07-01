package com.telecom.billing.service;

import com.telecom.billing.audit.AuditService;
import com.telecom.billing.model.*;
import com.telecom.billing.notification.NotificationService;
import com.telecom.billing.util.DisputeCalculator;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BillingDisputeService.
 *
 * Run these after every Copilot refactor step to confirm behaviour is preserved.
 * (Slide 9 Checklist Item 1 — Behaviour Preserved?)
 *
 * Run: mvn test -Dtest=BillingDisputeServiceTest
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BillingDisputeService Tests")
class BillingDisputeServiceTest {

    @Mock private DisputeCalculator   disputeCalculator;
    @Mock private NotificationService notificationService;
    @Mock private AuditService        auditService;

    @InjectMocks
    private BillingDisputeService service;

    private Customer makeCustomer(CustomerTier tier, boolean isPremium) {
        return Customer.builder()
                .customerId("CUST-42")
                .name("Priya Sharma")
                .email("priya@example.com")
                .tier(tier)
                .accountNumber("ACC-1001")
                .isPremium(isPremium)
                .build();
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

    // ── processDispute — happy path ──────────────────────────────────────────

    @Test
    @DisplayName("Returns ProcessedDispute with correct disputeId")
    void returns_processed_dispute_with_dispute_id() {
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.B, false),
                makeDispute(200.0, 100.0));

        assertThat(result.getDisputeId()).isEqualTo("DISP-001");
    }

    @Test
    @DisplayName("Resolves valid dispute as RESOLVED")
    void resolves_valid_dispute_as_resolved() {
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.B, false),
                makeDispute(200.0, 100.0));

        assertThat(result.getStatus()).isEqualTo(DisputeStatus.RESOLVED);
    }

    @Test
    @DisplayName("Applies tier B discount (10%) to penalty calculation")
    void applies_tier_b_discount_to_penalty() {
        // overcharge=100; penalty=15; 10% tier discount → 13.5
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.B, false),
                makeDispute(200.0, 100.0));

        assertThat(result.getPenaltyAmount()).isEqualTo(13.5);
    }

    @Test
    @DisplayName("Applies tier A discount (20%) to penalty calculation")
    void applies_tier_a_discount_to_penalty() {
        // overcharge=100; penalty=15; 20% tier discount → 12.0
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.A, false),
                makeDispute(200.0, 100.0));

        assertThat(result.getPenaltyAmount()).isEqualTo(12.0);
    }

    @Test
    @DisplayName("Applies additional premium discount when isPremium = true")
    void applies_premium_discount_on_top_of_tier_discount() {
        // overcharge=100; penalty=15; tier A: *0.80=12; premium: *0.91=10.92 → 10.92
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.A, true),
                makeDispute(200.0, 100.0));

        assertThat(result.getPenaltyAmount()).isLessThan(12.0);
    }

    @Test
    @DisplayName("Caps refund at 80% of total charged amount")
    void caps_refund_at_80_percent() {
        ProcessedDispute result = service.processDispute(
                makeCustomer(CustomerTier.C, false),
                makeDispute(100.0, 5.0));   // overcharge=95; maxRef=80

        assertThat(result.getRefundAmount()).isLessThanOrEqualTo(80.0);
    }

    // ── processDispute — validation errors ──────────────────────────────────

    @Test
    @DisplayName("Throws when disputeId is null")
    void throws_when_dispute_id_is_null() {
        Dispute dispute = makeDispute(200.0, 100.0);
        dispute.setDisputeId(null);

        assertThatThrownBy(() -> service.processDispute(makeCustomer(CustomerTier.B, false), dispute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dispute ID is required");
    }

    @Test
    @DisplayName("Throws when disputeId is blank")
    void throws_when_dispute_id_is_blank() {
        Dispute dispute = makeDispute(200.0, 100.0);
        dispute.setDisputeId("  ");

        assertThatThrownBy(() -> service.processDispute(makeCustomer(CustomerTier.B, false), dispute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dispute ID is required");
    }

    @Test
    @DisplayName("Throws when items list is empty")
    void throws_when_items_is_empty() {
        Dispute dispute = makeDispute(200.0, 100.0);
        dispute.setItems(List.of());

        assertThatThrownBy(() -> service.processDispute(makeCustomer(CustomerTier.B, false), dispute))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("Throws when charged amount is negative")
    void throws_when_charged_amount_is_negative() {
        assertThatThrownBy(() ->
                service.processDispute(makeCustomer(CustomerTier.B, false), makeDispute(-10.0, -20.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid charged amount");
    }

    @Test
    @DisplayName("Throws when expected amount exceeds charged amount")
    void throws_when_expected_exceeds_charged() {
        assertThatThrownBy(() ->
                service.processDispute(makeCustomer(CustomerTier.B, false), makeDispute(100.0, 300.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Expected amount cannot exceed");
    }

    @Test
    @DisplayName("Throws when overcharge is below minimum threshold (5.0)")
    void throws_when_overcharge_below_minimum() {
        assertThatThrownBy(() ->
                service.processDispute(makeCustomer(CustomerTier.B, false), makeDispute(104.0, 101.0)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("minimum threshold");
    }

    // ── raiseDispute ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("raiseDispute delegates to processDispute and returns a result")
    void raise_dispute_delegates_to_process_dispute() {
        ProcessedDispute result = service.raiseDispute(
                makeCustomer(CustomerTier.B, false),
                makeDispute(200.0, 100.0));

        assertThat(result).isNotNull();
        assertThat(result.getDisputeId()).isEqualTo("DISP-001");
    }

    // ── isHighValueDispute ────────────────────────────────────────────────────

    @Test @DisplayName("isHighValueDispute returns true for amount over 500")
    void is_high_value_above_500() {
        assertThat(service.isHighValueDispute(makeDispute(600.0, 100.0))).isTrue();
    }

    @Test @DisplayName("isHighValueDispute returns false for amount at exactly 500")
    void is_high_value_at_500() {
        assertThat(service.isHighValueDispute(makeDispute(500.0, 100.0))).isFalse();
    }

    // ── Side-effect verification ──────────────────────────────────────────────

    @Test
    @DisplayName("Calls AuditService.logDisputeResolution after processing")
    void calls_audit_service_after_processing() {
        service.processDispute(makeCustomer(CustomerTier.B, false), makeDispute(200.0, 100.0));
        verify(auditService, times(1))
                .logDisputeResolution(eq("DISP-001"), eq("CUST-42"), any(ProcessedDispute.class));
    }

    @Test
    @DisplayName("Calls NotificationService.sendResolutionEmail after processing")
    void calls_notification_service_after_processing() {
        service.processDispute(makeCustomer(CustomerTier.B, false), makeDispute(200.0, 100.0));
        verify(notificationService, times(1))
                .sendResolutionEmail(any(Customer.class), any(ProcessedDispute.class));
    }
}
