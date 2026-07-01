// src/feeCalculatorService.ts
import { calculateFee } from './feeCalculator';

export interface Customer {
  customerId: string;
  name: string;
  tier: string;
  isPremium: boolean;
}

export interface FeeRecord {
  customerId: string;
  amount: number;
  tier: string;
  isPremium: boolean;
  fee: number;
}

export interface FeeRepository {
  save(record: FeeRecord): FeeRecord;
  findByCustomerId(customerId: string): FeeRecord[];
}

export class FeeCalculatorService {
  constructor(private readonly repository: FeeRepository) {}

  calculateAndSave(customer: Customer, amount: number): FeeRecord {
    const fee = calculateFee(amount, customer.tier, customer.isPremium);
    const record: FeeRecord = {
      customerId: customer.customerId,
      amount,
      tier: customer.tier,
      isPremium: customer.isPremium,
      fee,
    };
    return this.repository.save(record);
  }
}
