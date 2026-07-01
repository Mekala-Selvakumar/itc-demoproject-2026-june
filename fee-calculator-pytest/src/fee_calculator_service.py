# src/fee_calculator_service.py
"""
FeeCalculatorService — wraps the core calculate_fee() function and
persists results via FeeRepository.

Used in the Mocks & Fixtures lab section to demonstrate:
  - Mocking FeeRepository with unittest.mock
  - Verifying mock.assert_called_once_with()
  - Fixture-based Customer setup
"""

from dataclasses import dataclass
from src.fee_calculator import calculate_fee


@dataclass
class Customer:
    customer_id: str
    name: str
    tier: str        # 'A' | 'B' | 'C'
    is_premium: bool = False


@dataclass
class FeeRecord:
    customer_id: str
    amount: float
    tier: str
    is_premium: bool
    fee: float


class FeeRepository:
    """Simulated persistence layer — real impl would use a DB."""
    def save(self, record: FeeRecord) -> FeeRecord:
        print(f"[DB] Saved fee record: {record}")
        return record

    def find_by_customer(self, customer_id: str) -> list[FeeRecord]:
        return []


class FeeCalculatorService:
    """
    Service layer: orchestrates fee calculation and persistence.

    Lab:
      - Mock FeeRepository.save() so tests don't hit a real DB
      - Verify save() is called with the correct FeeRecord
    """
    def __init__(self, repository: FeeRepository):
        self._repo = repository

    def calculate_and_save(self, customer: Customer, amount: float) -> FeeRecord:
        fee = calculate_fee(amount, customer.tier, customer.is_premium)
        record = FeeRecord(
            customer_id=customer.customer_id,
            amount=amount,
            tier=customer.tier,
            is_premium=customer.is_premium,
            fee=fee,
        )
        return self._repo.save(record)

    def get_fee_history(self, customer: Customer) -> list[FeeRecord]:
        return self._repo.find_by_customer(customer.customer_id)
