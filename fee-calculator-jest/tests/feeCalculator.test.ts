// tests/feeCalculator.test.ts
/**
 * Full Jest test suite for calculateFee()
 * Module 3 Lab — Building with GitHub Copilot (SR/RPS NIIT)
 *
 * Run:  npx jest tests/feeCalculator.test.ts --verbose
 * Cov:  npx jest --coverage
 */

import { calculateFee } from '../src/feeCalculator';

// ══════════════════════════════════════════════════════════════════════════════
// Section 1: Happy Path Tests
// Prompt: /tests #file:feeCalculator.ts — generate happy path tests
// ══════════════════════════════════════════════════════════════════════════════
describe('calculateFee() — Happy Path', () => {

  test('Tier C: no discount — 100 × 0.15 = 15.00', () => {
    expect(calculateFee(100, 'C', false)).toBe(15.0);
  });

  test('Tier B: 10% discount — 100 × 0.15 × 0.90 = 13.50', () => {
    expect(calculateFee(100, 'B', false)).toBe(13.5);
  });

  test('Tier A: 20% discount — 100 × 0.15 × 0.80 = 12.00', () => {
    expect(calculateFee(100, 'A', false)).toBe(12.0);
  });

  test('Tier A + premium: 100 × 0.15 × 0.80 × 0.91 = 10.92', () => {
    expect(calculateFee(100, 'A', true)).toBe(10.92);
  });

  test('Tier B + premium: 100 × 0.15 × 0.90 × 0.91 = 12.29', () => {
    expect(calculateFee(100, 'B', true)).toBe(12.29);
  });

  test('Tier C + premium: 100 × 0.15 × 0.91 = 13.65', () => {
    expect(calculateFee(100, 'C', true)).toBe(13.65);
  });

  test('Large amount Tier A: 500 × 0.15 × 0.80 = 60.00', () => {
    expect(calculateFee(500, 'A', false)).toBe(60.0);
  });

  test('Large amount Tier B: 200 × 0.15 × 0.90 = 27.00', () => {
    expect(calculateFee(200, 'B', false)).toBe(27.0);
  });

  test('Return type is number', () => {
    expect(typeof calculateFee(100, 'B', false)).toBe('number');
  });
});

// ══════════════════════════════════════════════════════════════════════════════
// Section 2: Sad Path / Error Tests
// Prompt: /tests — test Error thrown when amount <= 0 or tier is invalid
// ══════════════════════════════════════════════════════════════════════════════
describe('calculateFee() — Sad Path / Errors', () => {

  test('amount = 0 throws Error', () => {
    expect(() => calculateFee(0, 'A', false)).toThrow('Amount must be > 0');
  });

  test('negative amount throws Error', () => {
    expect(() => calculateFee(-100, 'A', false)).toThrow();
  });

  test('large negative amount throws Error', () => {
    expect(() => calculateFee(-999999, 'C', false)).toThrow();
  });

  test.each([['X'], [''], ['a'], ['AA'], [' A']])(
    "invalid tier '%s' throws Error",
    (invalidTier) => {
      expect(() => calculateFee(100, invalidTier, false)).toThrow("Tier must be A, B or C");
    }
  );
});

// ══════════════════════════════════════════════════════════════════════════════
// Section 3: Edge Case Tests
// Prompt: #selection generate edge cases — boundary values for calculateFee()
// ══════════════════════════════════════════════════════════════════════════════
describe('calculateFee() — Edge Cases', () => {

  test('amount = 4.99 (below threshold) → fee = 0', () => {
    expect(calculateFee(4.99, 'A', false)).toBe(0);
  });

  test('amount = 5.0 (AT threshold) → fee IS charged — NOT zero!', () => {
    // 5.0 × 0.15 × 0.80 = 0.60 for Tier A
    // ⚠ Common Copilot mistake: toBe(0) — WRONG
    const result = calculateFee(5.0, 'A', false);
    expect(result).toBeGreaterThan(0);
    expect(result).toBe(0.6);
  });

  test('amount = 5.01 (just above threshold) → fee charged', () => {
    expect(calculateFee(5.01, 'C', false)).toBeGreaterThan(0);
  });

  test('amount = 0.01 (well below threshold) → fee = 0', () => {
    expect(calculateFee(0.01, 'C', false)).toBe(0);
  });

  test('very large amount Tier A: 10000 × 0.15 × 0.80 = 1200', () => {
    expect(calculateFee(10_000, 'A', false)).toBe(1200.0);
  });

  test('rounding HALF_UP: 100 × 0.15 × 0.90 × 0.91 = 12.285 → 12.29', () => {
    expect(calculateFee(100, 'B', true)).toBe(12.29);
  });
});

// ══════════════════════════════════════════════════════════════════════════════
// Section 4: Parameterised Tests
// Prompt: #selection convert to test.each()
// ══════════════════════════════════════════════════════════════════════════════
describe('calculateFee() — Parameterised (test.each)', () => {

  test.each([
    // [amount, tier, isPremium, expected]
    [100,    'C', false, 15.0],
    [100,    'B', false, 13.5],
    [100,    'A', false, 12.0],
    [200,    'C', false, 30.0],
    [200,    'B', false, 27.0],
    [200,    'A', false, 24.0],
    [100,    'A', true,  10.92],
    [100,    'B', true,  12.29],
    [100,    'C', true,  13.65],
    [4.99,   'A', false,  0],
    [4.99,   'B', true,   0],
    [500,    'A', false, 60.0],
    [500,    'B', false, 67.5],
    [500,    'C', false, 75.0],
  ])('calculateFee(%p, %p, %p) = %p', (amount, tier, isPremium, expected) => {
    expect(calculateFee(amount as number, tier as string, isPremium as boolean))
      .toBe(expected);
  });
});

// ══════════════════════════════════════════════════════════════════════════════
// Section 5: Hollow Test Examples (Slide 8 — class discussion)
// ══════════════════════════════════════════════════════════════════════════════
describe('calculateFee() — Hollow Test Examples (Slide 8)', () => {

  test('❌ HOLLOW: toBeDefined() passes for any value including wrong ones', () => {
    const result = calculateFee(100, 'A', false);
    expect(result).toBeDefined();  // passes even if result is 999.99
  });

  test('✅ FIXED: toBe(12.0) verifies the exact expected value', () => {
    expect(calculateFee(100, 'A', false)).toBe(12.0);
  });

  test('❌ HOLLOW: wrong expected value — Copilot missed tier B discount', () => {
    // 200 × 0.15 = 30 — but tier B means ×0.90 → correct is 27.0
    // expect(calculateFee(200, 'B', false)).toBe(30.0); // WRONG — commented out
    expect(true).toBe(true); // placeholder for discussion
  });

  test('✅ FIXED: manually verified 200×0.15×0.90 = 27.0', () => {
    expect(calculateFee(200, 'B', false)).toBe(27.0);
  });

  test('❌ HOLLOW: never-failing exception test using try/catch', () => {
    // This passes even if NO exception is thrown!
    try {
      calculateFee(100, 'X', false);
    } catch {
      // swallowed — test gives false confidence
    }
    expect(true).toBe(true);
  });

  test('✅ FIXED: expect().toThrow() fails if exception is NOT raised', () => {
    expect(() => calculateFee(100, 'X', false)).toThrow();
  });
});
