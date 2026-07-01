package com.telecom.billing.notification;

import com.telecom.billing.model.Customer;
import com.telecom.billing.model.ProcessedDispute;
import com.telecom.billing.service.BillingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * ⚠️  INTENTIONAL CIRCULAR DEPENDENCY for the Module 2 Slide 5 demo:
 *
 *   BillingDisputeService → NotificationService → BillingService
 *
 * NotificationService injects BillingService to fetch service info for email bodies.
 * BillingDisputeService injects NotificationService to send resolution emails.
 * This creates the cycle that participants detect with:
 *
 *   @workspace are there any circular imports in the billing module? show the cycle path
 *
 * NOTE: @Lazy is used here to allow Spring to boot without a circular-dep error.
 * The smell is the design pattern, not the Spring wiring.
 */
@Service
@Slf4j
public class NotificationService {

    // ⚠️ Circular dep: BillingDisputeService → NotificationService → BillingService
    private final BillingService billingService;

    public NotificationService(@Lazy BillingService billingService) {
        this.billingService = billingService;
    }

    /**
     * Sends a dispute resolution email to the customer.
     * Called by BillingDisputeService.processDispute() — part of the call chain.
     */
    public void sendResolutionEmail(Customer customer, ProcessedDispute result) {
        String subject = String.format("Your billing dispute %s has been %s",
                result.getDisputeId(), result.getStatus());
        String body = buildEmailBody(customer, result);

        log.info("[EMAIL] To: {}", customer.getEmail());
        log.info("[EMAIL] Subject: {}", subject);
        log.debug("[EMAIL] Body:\n{}", body);
        // In production: JavaMailSender.send(message)
    }

    public void sendEscalationNotice(Customer customer, String disputeId) {
        log.info("[EMAIL] Escalation notice sent to {} for dispute {}", customer.getEmail(), disputeId);
    }

    // CODE SMELL: reaches back to BillingService — breaks separation of concerns
    private String buildEmailBody(Customer customer, ProcessedDispute result) {
        String info = billingService.getServiceInfo();
        return String.format(
            "Dear %s,%n%n" +
            "Your dispute (%s) has been processed.%n" +
            "Status:          %s%n" +
            "Penalty Applied: %.2f%n" +
            "Refund Approved: %.2f%n" +
            "Note: %s%n%n" +
            "System: %s%n%n" +
            "Regards,%nTelecom Billing Support",
            customer.getName(),
            result.getDisputeId(),
            result.getStatus(),
            result.getPenaltyAmount(),
            result.getRefundAmount(),
            result.getResolutionNote(),
            info
        );
    }
}
