# tests/test_fee_parametrized.py
"""
Parameterised & Mock Tests — Module 3 Lab (Slides 4 & 5)
Run: pytest tests/test_fee_parametrized.py -v
"""
import pytest
from unittest.mock import MagicMock, patch
from src.fee_calculator import calculate_fee
from src.fee_calculator_service import Customer, FeeRecord, FeeCalculatorService, FeeRepository
from tests.conftest import FEE_TEST_DATA


# ── Parameterised Tests (Slide 4 — Section 04) ────────────────────────────────
@pytest.mark.parametrize("amount,tier,is_premium,expected", FEE_TEST_DATA,
    ids=[f"{a}_{t}_{'P' if p else 'NP'}" for a,t,p,_ in FEE_TEST_DATA])
def test_calculate_fee_parametrized(amount, tier, is_premium, expected):
    """
    15 scenarios in one test — all tier/premium/amount combinations.
    Prompt used: #selection convert these 6 tests into @pytest.mark.parametrize
    """
    assert calculate_fee(amount, tier, is_premium) == expected


# ── Mock Tests (Slide 5) ──────────────────────────────────────────────────────
class TestFeeCalculatorServiceMocks:

    @pytest.fixture(autouse=True)
    def setup(self, mock_repository, fee_service):
        self.repo    = mock_repository
        self.service = fee_service

    def test_returns_correct_fee_record(self, tier_b_customer):
        """Service returns a FeeRecord with fee = 27.0 for Tier B, amount 200."""
        result = self.service.calculate_and_save(tier_b_customer, 200.0)
        assert result.fee == 27.0

    def test_repository_save_called_once(self, tier_b_customer):
        """✅ Correct mock: verify save() was called exactly once."""
        self.service.calculate_and_save(tier_b_customer, 200.0)
        self.repo.save.assert_called_once()

    def test_repository_save_called_with_correct_fee(self, tier_b_customer):
        """✅ Strict mock: verify the exact fee passed to save()."""
        self.service.calculate_and_save(tier_b_customer, 200.0)
        saved: FeeRecord = self.repo.save.call_args[0][0]
        assert saved.fee == 27.0
        assert saved.customer_id == tier_b_customer.customer_id

    def test_hollow_mock_no_verify(self, tier_b_customer):
        """❌ Hollow mock example (Slide 8): mock set up but never verified.
        This test passes even if save() is never called.
        """
        self.service.calculate_and_save(tier_b_customer, 200.0)
        # self.repo.save.assert_called_once()  ← missing!
        assert True  # discussion example

    def test_premium_tier_a_fee_saved(self, premium_tier_a_customer):
        """Tier A + premium: 100×0.15×0.80×0.91 = 10.92"""
        self.service.calculate_and_save(premium_tier_a_customer, 100.0)
        saved: FeeRecord = self.repo.save.call_args[0][0]
        assert saved.fee == 10.92
        assert saved.is_premium is True

    def test_below_threshold_saves_zero(self, tier_a_customer):
        """amount=4.99 → fee=0.0 is persisted."""
        self.service.calculate_and_save(tier_a_customer, 4.99)
        saved: FeeRecord = self.repo.save.call_args[0][0]
        assert saved.fee == 0.0

    def test_invalid_amount_does_not_call_save(self, tier_a_customer):
        """ValueError from calculate_fee() must prevent save() from being called."""
        with pytest.raises(ValueError):
            self.service.calculate_and_save(tier_a_customer, -50.0)
        self.repo.save.assert_not_called()
