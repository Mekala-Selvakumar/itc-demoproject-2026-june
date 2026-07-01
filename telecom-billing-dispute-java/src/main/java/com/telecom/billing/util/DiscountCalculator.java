package com.telecom.billing.util;

import com.telecom.billing.config.BillingConfig;
import com.telecom.billing.model.CustomerTier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 🎯 THIS IS THE REFACTORED TARGET — created during Lab Task 2 (DRY refactor).
 *
 * After the lab, the three duplicate discount implementations in:
 *   - BillingService.getDiscount()
 *   - InvoiceService.calcDiscount()
 *   - DisputeCalculator.getDiscountForTier()
 *
 * ...should all be replaced with calls to this shared utility bean.
 *
 * The scaffold is provided so participants know where to aim.
 * Participants fill in any gaps discovered during the lab.
 *
 * Slide reference: Slide 7 — DRY: Remove Duplication
 */
@Component
public class DiscountCalculator {

    /**
     * Returns the discount rate for a given customer tier.
     *
     * @param tier the customer tier (A = Premium, B = Standard, C = Basic)
     * @return discount rate as a decimal (e.g. 0.20 = 20%)
     */
    public double getDiscountRateForTier(CustomerTier tier) {
        return switch (tier) {
            case A -> BillingConfig.TIER_A_DISCOUNT_RATE;
            case B -> BillingConfig.TIER_B_DISCOUNT_RATE;
            case C -> BillingConfig.TIER_C_DISCOUNT_RATE;
        };
    }

    /**
     * Calculates the discounted amount for a given base amount and customer tier.
     * Optionally stacks the premium customer surcharge discount on top.
     *
     * @param baseAmount the original amount before discount
     * @param tier       the customer tier
     * @param isPremium  whether to apply the additional premium surcharge discount
     * @return discounted amount rounded to 2 decimal places
     */
    public double calculateDiscountedAmount(double baseAmount, CustomerTier tier, boolean isPremium) {
        double tierDiscount      = getDiscountRateForTier(tier);
        double discountedAmount  = baseAmount * (1 - tierDiscount);

        if (isPremium) {
            discountedAmount = discountedAmount * (1 - BillingConfig.PREMIUM_SURCHARGE_DISCOUNT);
        }

        return round(discountedAmount);
    }

    private double round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
