# tests/test_fee_calculator_parametrized.py
"""
Parameterised & Mock Tests — Module 3 Lab (Slide 4-Section 04, Slide 5)
"""
import pytest
from unittest.mock import MagicMock, call
from src.fee_calculator import calculate_fee
from src.fee_calculator_service import Customer, FeeRecord, FeeCalculatorService, FeeRepository
from tests.conftest import FEE_TEST_DATA


# ══════════════════════════════════════════════════════════════════════════════
# Parameterised Tests
# Prompt: #selection convert these 6 test cases into a single @pytest.mark.parametrize
# ══════════════════════════════════════════════════════════════════════════════

@pytest.mark.parametrize("amount,tier,is_premium,expected", FEE_TEST_DATA)
def test_calculate_fee_parametrized(amount, tier, is_premium, expected):
    """
    Comprehensive parameterised test covering 15 scenarios:
    tier A/B/C × premium true/false × typical and boundary amounts.
    One test method replaces 15 separate test functions.
    """
    assert calculate_fee(amount, tier, is_premium) == expected


# ══════════════════════════════════════════════════════════════════════════════
# Mock Tests — FeeCalculatorService + FeeRepository
# Prompt: #selection mock FeeRepository.save() and add verify assertion
# ══════════════════════════════════════════════════════════════════════════════

class TestFeeCalculatorService:

    @pytest.fixture(autouse=True)
    def setup(self, mock_repository, fee_service):
        self.repo    = mock_repository
        self.service = fee_service

    def test_calculate_and_save_returns_fee_record(self, tier_b_customer):
        """Service returns a FeeRecord with the correct fee."""
        result = self.service.calculate_and_save(tier_b_customer, 200.0)
        assert result.fee == 27.0   # 200 * 0.15 * 0.90

    def test_repository_save_called_once(self, tier_b_customer):
        """
        ✅  CORRECT MOCK: verify save() was called exactly once.
        Contrast with hollow mock (no assert_called_once) — see Slide 8.
        """
        self.service.calculate_and_save(tier_b_customer, 200.0)
        self.repo.save.assert_called_once()

    def test_repository_save_called_with_correct_fee(self, tier_b_customer):
        """
        ✅  STRICT MOCK: verify save() received a FeeRecord with fee=27.0.
        This is what separates a real mock test from a hollow one.
        """
        self.service.calculate_and_save(tier_b_customer, 200.0)
        saved_record: FeeRecord = self.repo.save.call_args[0][0]
        assert saved_record.fee == 27.0
        assert saved_record.customer_id == tier_b_customer.customer_id
        assert saved_record.tier == "B"

    def test_hollow_mock_no_verify(self, tier_b_customer):
        """
        ❌  HOLLOW MOCK EXAMPLE — for class discussion (Slide 8).
        The mock is set up but save() is never verified.
        This test passes even if save() is NEVER called.
        """
        self.service.calculate_and_save(tier_b_customer, 200.0)
        # repo.save.assert_called_once()  ← missing! test passes anyway
        assert True   # placeholder so test shows in output

    def test_premium_tier_a_fee_saved_correctly(self, premium_tier_a_customer):
        """Tier A + premium: 100 * 0.15 * 0.80 * 0.91 = 10.92"""
        self.service.calculate_and_save(premium_tier_a_customer, 100.0)
        record: FeeRecord = self.repo.save.call_args[0][0]
        assert record.fee == 10.92
        assert record.is_premium is True

    def test_below_threshold_saves_zero_fee(self, tier_a_customer):
        """amount=4.99 is below threshold — saved fee must be 0.0"""
        self.service.calculate_and_save(tier_a_customer, 4.99)
        record: FeeRecord = self.repo.save.call_args[0][0]
        assert record.fee == 0.0

    def test_invalid_amount_does_not_call_save(self, tier_a_customer):
        """ValueError from calculate_fee() prevents save() being called."""
        with pytest.raises(ValueError):
            self.service.calculate_and_save(tier_a_customer, -100.0)
        self.repo.save.assert_not_called()
