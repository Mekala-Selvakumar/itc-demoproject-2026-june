package com.telecom.billing.audit;

import com.telecom.billing.model.ProcessedDispute;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Audit trail for dispute lifecycle events.
 * Clean service — no code smells. Used as a contrast to BillingDisputeService.
 */
@Service
@Slf4j
public class AuditService {

    public void logDisputeResolution(String disputeId, String customerId, ProcessedDispute result) {
        log.info("[AUDIT] DISPUTE_RESOLVED | disputeId={} customerId={} status={} penalty={} refund={} at={}",
                disputeId, customerId,
                result.getStatus(), result.getPenaltyAmount(),
                result.getRefundAmount(), result.getProcessedAt());
    }

    public void logEscalation(String disputeId, String customerId) {
        log.warn("[AUDIT] DISPUTE_ESCALATED | disputeId={} customerId={}", disputeId, customerId);
    }

    public void logRejection(String disputeId, String reason) {
        log.info("[AUDIT] DISPUTE_REJECTED | disputeId={} reason={}", disputeId, reason);
    }
}
