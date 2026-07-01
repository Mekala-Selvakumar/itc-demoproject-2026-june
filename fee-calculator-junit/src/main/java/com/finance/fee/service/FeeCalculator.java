package com.finance.fee.service;

import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

/**
 * Finance Fee Calculator — Module 3 Demo (SR/RPS NIIT)
 *
 * Business Rules:
 *   calculateFee(amount, tier, isPremium)
 *   Tier A → 20% discount | Tier B → 10% | Tier C → 0%
 *   Base fee rate: 15%
 *   Premium stacking: additional 9% off
 *   Min threshold: amount < 5.0 → return 0.0
 *   Guards: amount <= 0  → IllegalArgumentException
 *           invalid tier → IllegalArgumentException
 */
@Component
public class FeeCalculator {

    private static final Set<String> VALID_TIERS  = Set.of("A", "B", "C");
    private static final double BASE_FEE_RATE     = 0.15;
    private static final double TIER_A_DISCOUNT   = 0.20;
    private static final double TIER_B_DISCOUNT   = 0.10;
    private static final double PREMIUM_DISCOUNT  = 0.09;
    private static final double MIN_THRESHOLD     = 5.00;

    public double calculateFee(double amount, String tier, boolean isPremium) {
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be > 0, got " + amount);
        if (!VALID_TIERS.contains(tier))
            throw new IllegalArgumentException("Tier must be A, B or C, got: " + tier);

        if (amount < MIN_THRESHOLD) return 0.0;

        double baseFee = amount * BASE_FEE_RATE;

        double tierDiscount = switch (tier) {
            case "A" -> TIER_A_DISCOUNT;
            case "B" -> TIER_B_DISCOUNT;
            default  -> 0.0;
        };

        double fee = baseFee * (1 - tierDiscount);
        if (isPremium) fee = fee * (1 - PREMIUM_DISCOUNT);

        return BigDecimal.valueOf(fee).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
