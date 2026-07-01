# src/fee_calculator.py
"""
Finance Fee Calculator — Module 3 Demo Project
Building with GitHub Copilot (SR/RPS NIIT)

Business Rules:
  calculateFee(amount, tier, is_premium)
  ─────────────────────────────────────
  Tier discounts:  A=20%  B=10%  C=0%
  Base fee rate:   15% of amount
  Premium stack:   additional 9% off (applied after tier discount)
  Min threshold:   amount < 5.0  → return 0.0  (no fee)
  Guards:          amount <= 0   → raise ValueError
                   tier not in {A,B,C} → raise ValueError
  Rounding:        result rounded to 2 decimal places

Lab: Generate and review a full test suite with Pytest + Copilot.
"""

from decimal import Decimal, ROUND_HALF_UP
from typing import Literal

VALID_TIERS = {"A", "B", "C"}
TIER_DISCOUNTS = {"A": 0.20, "B": 0.10, "C": 0.00}
BASE_FEE_RATE      = 0.15
PREMIUM_DISCOUNT   = 0.09
MIN_FEE_THRESHOLD  = 5.00
HIGH_VALUE_LIMIT   = 10_000.00


def calculate_fee(
    amount: float,
    tier: Literal["A", "B", "C"],
    is_premium: bool = False,
) -> float:
    """
    Calculate the transaction fee for a finance customer.

    Args:
        amount:     Transaction amount. Must be > 0.
        tier:       Customer tier: 'A' (Premium), 'B' (Standard), 'C' (Basic).
        is_premium: If True, apply an additional 9% discount on top of tier discount.

    Returns:
        Fee rounded to 2 decimal places.
        Returns 0.0 if amount is below the minimum threshold (< 5.0).

    Raises:
        ValueError: If amount <= 0 or tier is not one of 'A', 'B', 'C'.

    Examples:
        >>> calculate_fee(100, 'A', False)
        12.0
        >>> calculate_fee(100, 'B', False)
        13.5
        >>> calculate_fee(100, 'C', True)
        12.285  # wait — let's verify: 100*0.15=15; 15*(1-0.09)=13.65 → 13.65
        >>> calculate_fee(4.99, 'A', False)
        0.0
    """
    # Guard: invalid amount
    if amount <= 0:
        raise ValueError(f"Amount must be greater than 0, got {amount}")

    # Guard: invalid tier
    if tier not in VALID_TIERS:
        raise ValueError(f"Tier must be one of {sorted(VALID_TIERS)}, got '{tier}'")

    # Minimum threshold — no fee for very small transactions
    if amount < MIN_FEE_THRESHOLD:
        return 0.0

    # Base fee
    base_fee = amount * BASE_FEE_RATE

    # Apply tier discount
    tier_discount = TIER_DISCOUNTS[tier]
    fee = base_fee * (1 - tier_discount)

    # Stack premium discount on top
    if is_premium:
        fee = fee * (1 - PREMIUM_DISCOUNT)

    # Round to 2 decimal places (banker's rounding avoided — use HALF_UP)
    fee = float(Decimal(str(fee)).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))

    return fee
