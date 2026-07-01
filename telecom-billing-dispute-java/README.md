# Telecom Billing Dispute — Spring Boot
## Module 2 Demo Project | Building with GitHub Copilot (SR/RPS NIIT)

---

## 📁 Project Structure

```
src/
├── main/java/com/telecom/billing/
│   ├── BillingDisputeApplication.java     ← Spring Boot entry point
│   ├── audit/
│   │   └── AuditService.java              ← Clean audit trail (no smells)
│   ├── config/
│   │   └── BillingConfig.java             ← Named constants (refactor destination)
│   ├── controller/
│   │   └── DisputeController.java         ← REST API: POST /api/disputes/demo
│   ├── model/
│   │   ├── Customer.java                  ← JPA entity
│   │   ├── Dispute.java                   ← JPA entity (main aggregate)
│   │   ├── DisputeItem.java               ← JPA entity
│   │   ├── DisputeReason.java             ← Enum
│   │   ├── DisputeStatus.java             ← Enum
│   │   ├── CustomerTier.java              ← Enum (A/B/C)
│   │   ├── Invoice.java                   ← JPA entity
│   │   ├── InvoiceLineItem.java           ← JPA entity
│   │   ├── InvoiceStatus.java             ← Enum
│   │   ├── ProcessedDispute.java          ← Result DTO
│   │   └── ValidationResult.java          ← 🎯 Used after Extract Method refactor
│   ├── notification/
│   │   └── NotificationService.java       ← ⚠️ Circular dep anchor (Slide 5)
│   ├── service/
│   │   ├── BillingDisputeService.java     ← ⚠️ God class — main lab target (Slides 4,6,7)
│   │   ├── BillingService.java            ← ⚠️ Duplicate discount logic copy 1/3
│   │   └── InvoiceService.java            ← ⚠️ Duplicate discount logic copy 2/3
│   └── util/
│       ├── DisputeCalculator.java         ← ⚠️ All code smells (Slides 4,5,6,7)
│       └── DiscountCalculator.java        ← 🎯 DRY refactor target scaffold (Slide 7)
├── main/resources/
│   └── application.properties
└── test/java/com/telecom/billing/
    ├── service/BillingDisputeServiceTest.java   ← 15 tests with Mockito
    └── util/DisputeCalculatorTest.java          ← 14 tests (run after every refactor)
```

---

## 🚀 Quick Start

```bash
# Build and run
mvn spring-boot:run

# Run all tests (should ALL PASS before you start any refactoring)
mvn test

# Run just the calculator tests
mvn test -Dtest=DisputeCalculatorTest

# Run just the service tests
mvn test -Dtest=BillingDisputeServiceTest

# Hit the demo endpoint
curl -X POST http://localhost:8080/api/disputes/demo | jq

# H2 console (view the in-memory DB)
open http://localhost:8080/h2-console
# JDBC URL: jdbc:h2:mem:billingdb
```

---

## 🧪 Lab Tasks (35 minutes)

> **Rule**: Run `mvn test` after **every** step before moving on.

---

### Task 1 — Understand the Module (10 min)

Open **Copilot Chat** in VS Code and run:

```
@workspace explain what BillingDisputeService.java does and list its public methods
```
Expected: `raiseDispute`, `processDispute`, `getDiscount`, `updateDisputeStatus`,
`isHighValueDispute`, `oldResolve`, `getServiceInfo`

```
@workspace how does a billing dispute get from raiseDispute() to the resolution email?
```
Expected call chain:
`raiseDispute()` → `processDispute()` → `NotificationService.sendResolutionEmail()`
                                       → `AuditService.logDisputeResolution()`

```
@workspace identify the top 3 code smells in BillingDisputeService.java
```
Expected Copilot output:
1. God class — too many responsibilities
2. Duplicate discount logic (also in BillingService & DisputeCalculator)
3. Magic numbers — 5.0, 0.15, 0.09, 0.80, 500.0

---

### Task 2 — Refactor: Extract & DRY (15 min)

#### Step 2a — Extract validateDispute()

Select the **entire validation block** inside `processDispute()` and run:

```
#selection extract the validation logic into a private validateDispute(Dispute dispute)
          method that returns a ValidationResult with isValid boolean and List<String> errors
```

✅ `mvn test` — all 29 tests must still pass.

#### Step 2b — Extract calculatePenalty()

Select the **calculation block** (penalty + refund lines) and run:

```
#selection extract the penalty calculation into a private calculatePenalty(
          double overchargeAmount, CustomerTier tier, boolean isPremium) method
          that returns a double rounded to 2 decimal places
```

✅ `mvn test` again.

#### Step 2c — DRY: Consolidate Discount Logic

```
@workspace consolidate the duplicate discount logic from BillingService.java,
           InvoiceService.java, and DisputeCalculator.java into
           DiscountCalculator.java — the scaffold is already there
```

Replace each private `getDiscount()` / `calcDiscount()` / `getDiscountForTier()` method
with an injected `DiscountCalculator` bean.

✅ `mvn test` — zero regressions.

---

### Task 3 — Improve Readability + Review (10 min)

#### Step 3a — Generate Javadoc

Select `calculatePenalty()` (after your refactor) and run:

```
/doc
```

**Verify**:
- Does `@param isPremium` match the actual type (`boolean`)?
- Does `@return` describe what's actually returned?
- Is the behaviour description accurate?

#### Step 3b — Rename single-letter variables

Select `processDispute()` and run:

```
#selection rename all single-letter and abbreviated variables to descriptive names
```

Expected renames:

| Before | After |
|--------|-------|
| `c`    | `customer` |
| `d`    | `dispute` |
| `oa`   | `overchargeAmount` |
| `pen`  | `penaltyAmount` |
| `ref`  | `refundAmount` |
| `stat` | `disputeStatus` |
| `res`  | `processedDispute` |

#### Step 3c — Apply the 6-point Review Checklist

| # | Check | ✅ / ❌ |
|---|-------|--------|
| 1 | All 29 tests pass after each change | |
| 2 | Logic unchanged — read the diff line by line | |
| 3 | Edge cases still handled (null, 0.0, negative, empty list) | |
| 4 | No new Maven dependencies introduced | |
| 5 | New names match domain vocabulary (dispute, penalty, tier) | |
| 6 | Extracted methods are actually called from processDispute() | |

---

## 🔍 Copilot Chat Reference (Slides 4 & 5)

### Codebase Understanding
```
@workspace explain what BillingDisputeService.java does and list its public methods
@workspace how does a billing dispute get from raiseDispute() to the resolution email?
@workspace which classes import or call DisputeCalculator.applyPenalty()?
@workspace which methods in the billing package are never called anywhere?
@workspace does BillingDisputeService violate SRP? List responsibilities and suggest a split.
```

### Dependency Analysis
```
@workspace list every external dependency used by BillingDisputeService and why each is needed
@workspace are there any circular dependencies in the billing package? show the cycle path
@workspace which internal classes does DisputeCalculator depend on?
@workspace find duplicated discount-calculation logic and suggest a shared utility class
@workspace if we upgrade spring-boot from 3.2 to 3.3, what might break in the billing module?
```

---

## ⚠️ Intentional Code Smells Map

| File | Smell | Slide |
|------|-------|-------|
| `DisputeCalculator.java` | Magic numbers: `0.15`, `0.09`, `0.80`, `5.0` | 6 |
| `DisputeCalculator.java` | Poor naming: `calc()`, `x`, `d`, `f`, `tmp`, `t` | 6, 7 |
| `DisputeCalculator.java` | Dead code: `legacyCalcPenalty()` | 6 |
| `DisputeCalculator.java` | Duplicate discount map | 7 |
| `BillingDisputeService.java` | God class — 7 responsibilities | 6 |
| `BillingDisputeService.java` | Long method: `processDispute()` ~90 lines | 6, 7 |
| `BillingDisputeService.java` | Dead code: `oldResolve()` | 6 |
| `BillingDisputeService.java` | Magic numbers: `5.0`, `0.15`, `0.09`, `0.80`, `500.0` | 6 |
| `BillingDisputeService.java` | Poor naming: `d`, `c`, `pen`, `ref`, `oa`, `stat`, `res` | 7 |
| `BillingDisputeService.java` | Deep nesting: 4-level if-else | 8 |
| `BillingService.java` | Duplicate discount logic (1/3) | 7 |
| `InvoiceService.java` | Duplicate discount logic (2/3) | 7 |
| `NotificationService.java` | Circular dep: `→ BillingService` | 5 |

---

## 🎯 Expected Refactored State

```java
// BillingDisputeService.java — after lab
public ProcessedDispute processDispute(Customer customer, Dispute dispute) {
    ValidationResult validation = validateDispute(dispute);    // ← extracted
    if (!validation.isValid()) { ... }

    double penaltyAmount = calculatePenalty(                   // ← extracted
        overchargeAmount, customer.getTier(), customer.isPremium());

    DisputeStatus status = determineStatus(penaltyAmount, refundAmount, dispute);

    ProcessedDispute result = buildResult(dispute, status, penaltyAmount, refundAmount);
    sendResolutionEmail(customer, result);                      // ← extracted
    logAudit(dispute.getDisputeId(), customer.getCustomerId(), result);
    return result;
}

// DiscountCalculator.java — shared (replaces 3 duplicates)
public double getDiscountRateForTier(CustomerTier tier) { ... }
public double calculateDiscountedAmount(double base, CustomerTier tier, boolean isPremium) { ... }
```

---

## 🛠️ Tech Stack

| Component | Version |
|-----------|---------|
| Java | 21 |
| Spring Boot | 3.2.2 |
| Spring Data JPA | (via Boot) |
| H2 Database | (in-memory, runtime) |
| Spring Mail | (wired, simulated) |
| Lombok | latest |
| JUnit 5 | (via Boot test) |
| Mockito | (via Boot test) |
| AssertJ | (via Boot test) |
| Maven | 3.9+ |
