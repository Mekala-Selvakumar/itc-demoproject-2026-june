package com.telecom.billing.controller;

import com.telecom.billing.model.*;
import com.telecom.billing.service.BillingDisputeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * REST API for the Billing Dispute module.
 *
 * Endpoints:
 *   POST   /api/disputes/raise      — raise and process a new dispute
 *   GET    /api/disputes/health     — service health check
 *   POST   /api/disputes/demo       — pre-built demo scenario for the lab
 *
 * Run the app and test with:
 *   curl -X POST http://localhost:8080/api/disputes/demo | jq
 */
@RestController
@RequestMapping("/api/disputes")
@RequiredArgsConstructor
@Slf4j
public class DisputeController {

    private final BillingDisputeService billingDisputeService;

    /** Raise and immediately process a dispute. */
    @PostMapping("/raise")
    public ResponseEntity<ProcessedDispute> raiseDispute(
            @RequestParam String customerId,
            @RequestParam String customerName,
            @RequestParam String email,
            @RequestParam CustomerTier tier,
            @RequestParam boolean isPremium,
            @RequestParam String accountNumber,
            @RequestParam double chargedAmount,
            @RequestParam double expectedAmount,
            @RequestParam DisputeReason reason) {

        Customer customer = Customer.builder()
                .customerId(customerId)
                .name(customerName)
                .email(email)
                .tier(tier)
                .accountNumber(accountNumber)
                .isPremium(isPremium)
                .build();

        DisputeItem item = DisputeItem.builder()
                .itemId("ITEM-" + UUID.randomUUID())
                .description("Billing dispute item")
                .chargedAmount(chargedAmount)
                .expectedAmount(expectedAmount)
                .build();

        Dispute dispute = Dispute.builder()
                .disputeId("DISP-" + UUID.randomUUID())
                .customerId(customerId)
                .accountNumber(accountNumber)
                .reason(reason)
                .status(DisputeStatus.OPEN)
                .items(List.of(item))
                .totalChargedAmount(chargedAmount)
                .totalExpectedAmount(expectedAmount)
                .raisedAt(LocalDateTime.now())
                .build();

        ProcessedDispute result = billingDisputeService.raiseDispute(customer, dispute);
        return ResponseEntity.ok(result);
    }

    /** Pre-built demo scenario for the Module 2 lab. */
    @PostMapping("/demo")
    public ResponseEntity<ProcessedDispute> runDemo() {
        Customer customer = Customer.builder()
                .customerId("CUST-DEMO-001")
                .name("Priya Sharma")
                .email("priya.sharma@example.com")
                .tier(CustomerTier.B)
                .accountNumber("ACC-1001")
                .isPremium(false)
                .build();

        DisputeItem item = DisputeItem.builder()
                .itemId("ITEM-DEMO-001")
                .description("Data plan overcharge — charged for 10GB, used 6GB")
                .chargedAmount(200.00)
                .expectedAmount(120.00)
                .build();

        Dispute dispute = Dispute.builder()
                .disputeId("DISP-DEMO-001")
                .customerId("CUST-DEMO-001")
                .accountNumber("ACC-1001")
                .reason(DisputeReason.OVERCHARGE)
                .status(DisputeStatus.OPEN)
                .items(List.of(item))
                .totalChargedAmount(200.00)
                .totalExpectedAmount(120.00)
                .raisedAt(LocalDateTime.now())
                .build();

        ProcessedDispute result = billingDisputeService.raiseDispute(customer, dispute);
        log.info("Demo dispute processed: {}", result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok(billingDisputeService.getServiceInfo());
    }
}
