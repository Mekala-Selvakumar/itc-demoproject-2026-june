// src/feeCalculator.ts
/**
 * Finance Fee Calculator — Module 3 Demo (SR/RPS NIIT)
 *
 * Business Rules:
 *   calculateFee(amount, tier, isPremium)
 *   Tier A → 20% discount | Tier B → 10% | Tier C → 0%
 *   Base fee rate : 15%
 *   Premium stack : additional 9% off on top of tier discount
 *   Min threshold : amount < 5.0 → return 0
 *   Guard         : amount <= 0  → throw Error
 *   Guard         : tier ∉ {A,B,C} → throw Error
 *   Rounding      : result rounded to 2 decimal places (HALF_UP)
 */

export type CustomerTier = 'A' | 'B' | 'C';

const VALID_TIERS   = new Set<string>(['A', 'B', 'C']);
const BASE_FEE_RATE  = 0.15;
const TIER_DISCOUNTS: Record<CustomerTier, number> = { A: 0.20, B: 0.10, C: 0.00 };
const PREMIUM_DISC   = 0.09;
const MIN_THRESHOLD  = 5.00;

function roundHalfUp(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100;
}

export function calculateFee(
  amount: number,
  tier: string,
  isPremium: boolean = false
): number {
  if (amount <= 0)
    throw new Error(`Amount must be > 0, received: ${amount}`);
  if (!VALID_TIERS.has(tier))
    throw new Error(`Tier must be A, B or C, received: '${tier}'`);

  if (amount < MIN_THRESHOLD) return 0;

  const baseFee      = amount * BASE_FEE_RATE;
  const tierDiscount = TIER_DISCOUNTS[tier as CustomerTier];
  let fee            = baseFee * (1 - tierDiscount);

  if (isPremium) fee = fee * (1 - PREMIUM_DISC);

  return roundHalfUp(fee);
}
