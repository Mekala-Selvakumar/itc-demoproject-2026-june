package com.telecom.billing.model;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Result returned after a dispute is processed by BillingDisputeService.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedDispute {

    private String        disputeId;
    private DisputeStatus status;
    private double        penaltyAmount;
    private double        refundAmount;
    private String        resolutionNote;
    private LocalDateTime processedAt;
}
