// tests/feeCalculatorService.test.ts
/**
 * Mock-based tests for FeeCalculatorService.
 * Module 3 Lab — Mocks & Fixtures section (Slide 5)
 *
 * Run: npx jest tests/feeCalculatorService.test.ts --verbose
 */

import { FeeCalculatorService, Customer, FeeRecord, FeeRepository } from '../src/feeCalculatorService';

// ── Fixtures ──────────────────────────────────────────────────────────────────
const tierB_customer: Customer = { customerId: 'C001', name: 'Priya', tier: 'B', isPremium: false };
const tierA_premium:  Customer = { customerId: 'C002', name: 'Arjun', tier: 'A', isPremium: true  };
const tierA_customer: Customer = { customerId: 'C003', name: 'Rahul', tier: 'A', isPremium: false };

// ── Mock Repository Factory ───────────────────────────────────────────────────
function makeMockRepository(): jest.Mocked<FeeRepository> {
  return {
    save:               jest.fn((r: FeeRecord) => r),  // returns what it receives
    findByCustomerId:   jest.fn(() => []),
  };
}

// ══════════════════════════════════════════════════════════════════════════════
describe('FeeCalculatorService — Mock Tests (Slide 5)', () => {

  let mockRepo: jest.Mocked<FeeRepository>;
  let service:  FeeCalculatorService;

  beforeEach(() => {
    mockRepo = makeMockRepository();
    service  = new FeeCalculatorService(mockRepo);
  });

  afterEach(() => {
    jest.clearAllMocks();
  });

  // ── Happy path ──────────────────────────────────────────────────────────────
  test('Returns FeeRecord with fee = 27.0 for Tier B, amount 200', () => {
    const result = service.calculateAndSave(tierB_customer, 200);
    expect(result.fee).toBe(27.0);
  });

  // ── Mock verify (correct) ──────────────────────────────────────────────────
  test('✅ Correct mock: repository.save() called exactly once', () => {
    service.calculateAndSave(tierB_customer, 200);
    expect(mockRepo.save).toHaveBeenCalledTimes(1);
  });

  test('✅ Strict mock: repository.save() called with correct fee', () => {
    service.calculateAndSave(tierB_customer, 200);
    expect(mockRepo.save).toHaveBeenCalledWith(
      expect.objectContaining({ fee: 27.0, customerId: 'C001' })
    );
  });

  // ── Hollow mock (discussion) ───────────────────────────────────────────────
  test('❌ Hollow mock: jest.fn() set up but never verified (Slide 8)', () => {
    service.calculateAndSave(tierB_customer, 200);
    // expect(mockRepo.save).toHaveBeenCalled();  ← missing! passes always
    expect(true).toBe(true);  // discussion example
  });

  // ── More scenarios ─────────────────────────────────────────────────────────
  test('Premium Tier A: 100×0.15×0.80×0.91 = 10.92 persisted', () => {
    service.calculateAndSave(tierA_premium, 100);
    expect(mockRepo.save).toHaveBeenCalledWith(
      expect.objectContaining({ fee: 10.92, isPremium: true })
    );
  });

  test('amount = 4.99 below threshold — fee = 0 is persisted', () => {
    service.calculateAndSave(tierA_customer, 4.99);
    expect(mockRepo.save).toHaveBeenCalledWith(
      expect.objectContaining({ fee: 0 })
    );
  });

  test('Invalid amount prevents repository.save() from being called', () => {
    expect(() => service.calculateAndSave(tierA_customer, -50)).toThrow();
    expect(mockRepo.save).not.toHaveBeenCalled();
  });
});
