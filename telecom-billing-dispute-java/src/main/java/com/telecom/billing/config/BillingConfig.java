package com.telecom.billing.config;

import org.springframework.context.annotation.Configuration;

/**
 * Centralised billing configuration constants.
 *
 * 🎯 Refactor destination — magic numbers in DisputeCalculator and
 *    BillingDisputeService should reference these constants after the lab.
 */
@Configuration
public class BillingConfig {

    // Discount rates per customer tier
    public static final double TIER_A_DISCOUNT_RATE   = 0.20;   // Premium  — 20%
    public static final double TIER_B_DISCOUNT_RATE   = 0.10;   // Standard — 10%
    public static final double TIER_C_DISCOUNT_RATE   = 0.00;   // Basic    — 0%

    // Additional discount for premium customers (stacked on top of tier discount)
    public static final double PREMIUM_SURCHARGE_DISCOUNT = 0.09;

    // Maximum refund as a fraction of total charged amount
    public static final double MAX_REFUND_RATE = 0.80;

    // Penalty rate applied to overcharge amount
    public static final double BASE_PENALTY_RATE = 0.15;

    // Minimum dispute amount — disputes below this threshold are auto-rejected
    public static final double MIN_DISPUTE_AMOUNT = 5.00;

    // High-value dispute threshold — triggers senior agent assignment
    public static final double HIGH_VALUE_THRESHOLD = 500.00;

    // SLA resolution days per tier
    public static final int TIER_A_SLA_DAYS = 2;
    public static final int TIER_B_SLA_DAYS = 5;
    public static final int TIER_C_SLA_DAYS = 10;

    // Tax rate used across billing calculations
    public static final double TAX_RATE = 0.18;

    // Late payment penalty rate (applied after 30 days past due)
    public static final double LATE_FEE_RATE   = 0.05;
    public static final int    LATE_FEE_DAYS   = 30;

    // Email addresses
    public static final String BILLING_FROM_EMAIL   = "billing@telecom.example.com";
    public static final String BILLING_SUPPORT_EMAIL = "billing-support@telecom.example.com";
}
