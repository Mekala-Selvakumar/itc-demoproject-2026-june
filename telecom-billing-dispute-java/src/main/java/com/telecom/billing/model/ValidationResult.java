package com.telecom.billing.model;

import lombok.*;
import java.util.List;

/**
 * Result of dispute validation — returned by validateDispute() after refactoring.
 * 🎯 Used as the return type in the Extract Method lab task.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResult {
    private boolean      isValid;
    private List<String> errors;
}
