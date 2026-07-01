# tests/conftest.py
"""
Shared fixtures for the fee-calculator test suite.

Lab Section: Mocks, Fixtures & Test Data (Slide 5)
  - @pytest.fixture: reusable Customer objects
  - Fixture scope: function (default) — fresh state per test
"""

import pytest
from unittest.mock import MagicMock
from src.fee_calculator_service import Customer, FeeRepository, FeeCalculatorService


# ── Customer Fixtures ─────────────────────────────────────────────────────────

@pytest.fixture
def tier_a_customer():
    """Premium Tier A customer — 20% tier discount + eligible for premium stack."""
    return Customer(customer_id="CUST-A01", name="Arjun Mehta", tier="A", is_premium=False)


@pytest.fixture
def tier_b_customer():
    """Standard Tier B customer — 10% tier discount."""
    return Customer(customer_id="CUST-B01", name="Priya Sharma", tier="B", is_premium=False)


@pytest.fixture
def tier_c_customer():
    """Basic Tier C customer — no discount."""
    return Customer(customer_id="CUST-C01", name="Rahul Singh", tier="C", is_premium=False)


@pytest.fixture
def premium_tier_a_customer():
    """Premium Tier A customer with isPremium=True — 20% tier + 9% premium stacked."""
    return Customer(customer_id="CUST-PA01", name="Sneha Kapoor", tier="A", is_premium=True)


# ── Mock Repository ───────────────────────────────────────────────────────────

@pytest.fixture
def mock_repository():
    """
    MagicMock for FeeRepository.
    Lab: verify .save() is called with the correct FeeRecord.
    """
    repo = MagicMock(spec=FeeRepository)
    # Default: save() returns whatever it receives
    repo.save.side_effect = lambda record: record
    return repo


@pytest.fixture
def fee_service(mock_repository):
    """FeeCalculatorService wired with a mock repository."""
    return FeeCalculatorService(repository=mock_repository)


# ── Parameterised Test Data ───────────────────────────────────────────────────

# (amount, tier, is_premium, expected_fee)
# Used with @pytest.mark.parametrize in test_fee_calculator_parametrized.py
FEE_TEST_DATA = [
    # Tier C — no discount
    (100.0,  "C", False, 15.0),
    (200.0,  "C", False, 30.0),
    (500.0,  "C", False, 75.0),
    # Tier B — 10% discount
    (100.0,  "B", False, 13.5),
    (200.0,  "B", False, 27.0),
    (500.0,  "B", False, 67.5),
    # Tier A — 20% discount
    (100.0,  "A", False, 12.0),
    (200.0,  "A", False, 24.0),
    (500.0,  "A", False, 60.0),
    # Premium stacking on Tier A (20% tier + 9% premium)
    (100.0,  "A", True,  10.92),   # 15 * 0.80 * 0.91 = 10.92
    (200.0,  "A", True,  21.84),
    # Premium stacking on Tier B (10% tier + 9% premium)
    (100.0,  "B", True,  12.29),   # 15 * 0.90 * 0.91 = 12.285 → 12.29
    # Below minimum threshold → always 0.0
    (4.99,   "A", False,  0.0),
    (4.99,   "B", True,   0.0),
    (0.01,   "C", False,  0.0),
]
