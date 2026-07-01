# tests/test_fee_calculator.py
"""
Unit Test Suite for calculate_fee()
Module 3 Lab — Building with GitHub Copilot (SR/RPS NIIT)

Covers:
  ✅ Happy-path tests (Slide 4 — Section 01)
  ✅ Sad-path / error tests (Slide 4 — Section 02)
  ✅ Edge-case tests (Slide 4 — Section 03)
  ✅ Hollow test examples with fixes (Slide 8)

Run:  pytest tests/test_fee_calculator.py -v
"""

import pytest
from src.fee_calculator import calculate_fee


# ══════════════════════════════════════════════════════════════════════════════
# SECTION 1: Happy-Path Tests
# Prompt used: /tests for calculateFee() — generate happy path tests
#              for all three tiers A, B, C with non-premium customer
# ══════════════════════════════════════════════════════════════════════════════

class TestHappyPath:

    def test_tier_c_no_discount(self):
        """Tier C: no discount — fee = amount * 0.15"""
        # 100 * 0.15 = 15.0
        assert calculate_fee(100.0, "C", False) == 15.0

    def test_tier_b_ten_percent_discount(self):
        """Tier B: 10% discount — fee = 100 * 0.15 * 0.90 = 13.5"""
        assert calculate_fee(100.0, "B", False) == 13.5

    def test_tier_a_twenty_percent_discount(self):
        """Tier A: 20% discount — fee = 100 * 0.15 * 0.80 = 12.0"""
        assert calculate_fee(100.0, "A", False) == 12.0

    def test_tier_a_large_amount(self):
        """Tier A, large amount — fee = 500 * 0.15 * 0.80 = 60.0"""
        assert calculate_fee(500.0, "A", False) == 60.0

    def test_tier_b_large_amount(self):
        """Tier B, large amount — fee = 200 * 0.15 * 0.90 = 27.0"""
        assert calculate_fee(200.0, "B", False) == 27.0

    def test_premium_stacks_on_tier_a(self):
        """Tier A + premium: 100 * 0.15 * 0.80 * 0.91 = 10.92"""
        assert calculate_fee(100.0, "A", True) == 10.92

    def test_premium_stacks_on_tier_b(self):
        """Tier B + premium: 100 * 0.15 * 0.90 * 0.91 = 12.285 → 12.29"""
        assert calculate_fee(100.0, "B", True) == 12.29

    def test_premium_stacks_on_tier_c(self):
        """Tier C + premium: 100 * 0.15 * 1.0 * 0.91 = 13.65"""
        assert calculate_fee(100.0, "C", True) == 13.65

    def test_returns_float(self):
        """Return type must always be float."""
        result = calculate_fee(100.0, "B", False)
        assert isinstance(result, float)

    def test_rounds_to_two_decimal_places(self):
        """Result must be rounded to exactly 2 decimal places."""
        result = calculate_fee(100.0, "B", True)
        assert result == round(result, 2)


# ══════════════════════════════════════════════════════════════════════════════
# SECTION 2: Sad-Path / Error Tests
# Prompt used: /tests for calculateFee() — test that ValueError is raised
#              when amount is 0 or negative
# ══════════════════════════════════════════════════════════════════════════════

class TestSadPath:

    def test_zero_amount_raises_value_error(self):
        """Amount of 0 must raise ValueError."""
        with pytest.raises(ValueError, match="Amount must be greater than 0"):
            calculate_fee(0, "A", False)

    def test_negative_amount_raises_value_error(self):
        """Negative amount must raise ValueError."""
        with pytest.raises(ValueError):
            calculate_fee(-100.0, "A", False)

    def test_very_negative_amount_raises_value_error(self):
        """Large negative amount must raise ValueError."""
        with pytest.raises(ValueError):
            calculate_fee(-999999.99, "C", False)

    def test_invalid_tier_x_raises_value_error(self):
        """Tier 'X' is invalid and must raise ValueError."""
        with pytest.raises(ValueError, match="Tier must be one of"):
            calculate_fee(100.0, "X", False)

    def test_invalid_tier_empty_string_raises_value_error(self):
        """Empty string tier must raise ValueError."""
        with pytest.raises(ValueError):
            calculate_fee(100.0, "", False)

    def test_invalid_tier_lowercase_raises_value_error(self):
        """Lowercase tier 'a' must raise ValueError (case-sensitive)."""
        with pytest.raises(ValueError):
            calculate_fee(100.0, "a", False)

    def test_invalid_tier_none_raises_value_error(self):
        """None tier must raise ValueError."""
        with pytest.raises((ValueError, TypeError)):
            calculate_fee(100.0, None, False)


# ══════════════════════════════════════════════════════════════════════════════
# SECTION 3: Edge-Case Tests
# Prompt: #selection generate edge-case tests for calculateFee():
#         amount=4.99 (below min), amount=5.0 (at min), very large amounts
# ══════════════════════════════════════════════════════════════════════════════

class TestEdgeCases:

    # ── Minimum threshold boundary ────────────────────────────────────────────

    def test_amount_below_minimum_returns_zero(self):
        """amount=4.99 is below 5.0 threshold — must return 0.0"""
        assert calculate_fee(4.99, "A", False) == 0.0

    def test_amount_exactly_at_minimum_returns_zero(self):
        """amount=5.0 is NOT below threshold (< 5.0 guard) — fee IS charged.
        NOTE: 5.0 * 0.15 * 0.80 = 0.60 for tier A
        ⚠️ Common Copilot mistake: assert result == 0.0 — WRONG!
        """
        result = calculate_fee(5.0, "A", False)
        assert result > 0.0, "amount=5.0 is at threshold — fee should be charged"
        assert result == 0.6   # 5.0 * 0.15 * 0.80

    def test_amount_just_above_minimum_charges_fee(self):
        """amount=5.01 — just above threshold, fee must be charged."""
        result = calculate_fee(5.01, "C", False)
        assert result > 0.0

    def test_amount_of_one_cent_below_minimum(self):
        """amount=0.01 — well below threshold, no fee."""
        assert calculate_fee(0.01, "C", False) == 0.0

    # ── Very large amounts ────────────────────────────────────────────────────

    def test_very_large_amount_tier_c(self):
        """amount=10,000 tier C: 10000 * 0.15 = 1500.0"""
        assert calculate_fee(10_000.0, "C", False) == 1500.0

    def test_very_large_amount_tier_a(self):
        """amount=10,000 tier A: 10000 * 0.15 * 0.80 = 1200.0"""
        assert calculate_fee(10_000.0, "A", False) == 1200.0

    def test_very_large_amount_premium(self):
        """amount=10,000 tier A premium: 1200 * 0.91 = 1092.0"""
        assert calculate_fee(10_000.0, "A", True) == 1092.0

    # ── Floating-point precision ──────────────────────────────────────────────

    def test_fee_is_rounded_not_truncated(self):
        """Ensure ROUND_HALF_UP is used — 12.285 → 12.29 not 12.28."""
        # 100 * 0.15 * 0.90 * 0.91 = 12.285
        assert calculate_fee(100.0, "B", True) == 12.29

    def test_fee_precision_tier_b_large(self):
        """333.33 * 0.15 * 0.90 = 45.0 (exact)"""
        assert calculate_fee(333.33, "B", False) == round(333.33 * 0.15 * 0.90, 2)


# ══════════════════════════════════════════════════════════════════════════════
# SECTION 4: Hollow Test Examples (Slide 8 — for class discussion)
# These demonstrate the 4 hollow-test patterns.
# The ❌ versions are included for discussion — they pass but don't verify behaviour.
# ══════════════════════════════════════════════════════════════════════════════

class TestHollowExamples:
    """
    ⚠️  DISCUSSION EXAMPLES — show participants these patterns during the debrief.
    The ❌ tests pass but prove almost nothing.
    """

    # Pattern 1: Missing assertion
    def test_hollow_missing_assertion(self):
        """❌  HOLLOW: 'is not None' is always True for a float."""
        result = calculate_fee(100.0, "A", False)
        assert result is not None   # passes even if result is 0.0 or 999.99

    def test_correct_assertion(self):
        """✅  FIXED: Asserts the actual expected value."""
        result = calculate_fee(100.0, "A", False)
        assert result == 12.0   # verifies 100 * 0.15 * 0.80

    # Pattern 2: Wrong expected value (common Copilot mistake)
    def test_hollow_wrong_expected_value(self):
        """❌  HOLLOW: Copilot sometimes computes without applying tier discount.
            200 * 0.15 = 30.0 — but tier B means 10% off → correct value is 27.0
        """
        # assert calculate_fee(200.0, "B", False) == 30.0  # WRONG — commented out so suite passes
        pass  # placeholder for discussion

    def test_correct_tier_b_value(self):
        """✅  FIXED: Manual verification: 200*0.15=30; 30*0.90=27.0"""
        assert calculate_fee(200.0, "B", False) == 27.0

    # Pattern 3: Never-failing exception test
    def test_hollow_never_failing(self):
        """❌  HOLLOW: try/except/pass passes even if NO exception is raised."""
        try:
            calculate_fee(100.0, "X", False)
        except ValueError:
            pass
        # No assertion — test passes regardless of whether exception occurred

    def test_correct_exception_test(self):
        """✅  FIXED: pytest.raises() fails if the exception is NOT raised."""
        with pytest.raises(ValueError):
            calculate_fee(100.0, "X", False)
