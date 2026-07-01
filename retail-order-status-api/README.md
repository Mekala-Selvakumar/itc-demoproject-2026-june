# Retail Order Status API — Module 4 Debugging Lab
## Debugging and Error Resolution | Building with GitHub Copilot (SR/RPS NIIT)

---

## 🐛 The Bug (do NOT read the fix section before attempting the lab)

`GET /api/orders/ORD-1042/status` returns `"PENDING"` even though the order
shipped 2 days ago and a shipment record genuinely exists. The bug is
**intermittent by order** — `ORD-2001` (no shipment yet) correctly shows
`PENDING`, but `ORD-1042` and `ORD-3055` (which DO have shipments) also
incorrectly show `PENDING`.

---

## 📁 Project Structure

```
src/main/java/com/retail/order/
├── OrderStatusApiApplication.java
├── controller/
│   └── OrderController.java          ← GET /api/orders/{code}/status (+ /status-strict)
├── service/
│   └── OrderStatusService.java       ← ⚠️ Bug lives here (resolveStatus(), line ~50)
├── repository/
│   ├── OrderRepository.java          ← clean
│   └── ShipmentRepository.java       ← ⚠️ Root cause: findByOrderId(Long)
├── model/
│   ├── Order.java                    ← id: Long, orderCode: String "ORD-1042"
│   └── Shipment.java                 ← orderId: String — but stores order CODES, not Order.id
├── dto/OrderStatusResponse.java
└── exception/
    ├── OrderNotFoundException.java
    └── GlobalExceptionHandler.java

src/main/resources/
├── application.properties            ← DEBUG logging enabled
└── data.sql                          ← Seed data that reproduces the bug

src/test/java/com/retail/order/
├── service/OrderStatusServiceTest.java         ← Mockito unit tests
└── controller/OrderControllerIntegrationTest.java  ← Full H2 integration tests
```

---

## 🚀 Setup

```bash
mvn clean install
mvn spring-boot:run
```

Verify the bug:
```bash
curl http://localhost:8080/api/orders/ORD-1042/status
# → {"orderCode":"ORD-1042","status":"PENDING", ...}   ❌ WRONG — should be SHIPPED

curl http://localhost:8080/api/orders/ORD-2001/status
# → {"orderCode":"ORD-2001","status":"PENDING", ...}   ✅ CORRECT — no shipment exists

curl http://localhost:8080/api/orders/ORD-1042/status-strict
# → 500 Internal Server Error — NullPointerException in the console
```

H2 Console: http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:orderdb`)

---

## 🧪 Step-by-Step Lab (30 minutes)

### Task 1 — Reproduce & Explain the Stack Trace (10 min)

**Step 1.1** — Confirm the bug reproduces:
```bash
curl http://localhost:8080/api/orders/ORD-1042/status
```
Confirm the response shows `"status":"PENDING"` instead of the expected `"SHIPPED"`.

**Step 1.2** — Trigger the real exception using the strict endpoint:
```bash
curl http://localhost:8080/api/orders/ORD-1042/status-strict
```
Watch the application console — you'll see the full stack trace:
```
java.lang.NullPointerException: Cannot invoke "Shipment.getStatus()" because "shipment" is null
    at com.retail.order.service.OrderStatusService.getStatusStrict(OrderStatusService.java:XX)
    at com.retail.order.controller.OrderController.getOrderStatusStrict(OrderController.java:XX)
```

**Step 1.3** — Copy the stack trace into Copilot Chat:
```
#selection /explain this stack trace — what line actually threw the
exception and why?
```

**Step 1.4** — Ask Copilot to trace the variable:
```
@workspace trace how "shipment" is populated in OrderStatusService —
where could it be null?
```

✅ **Checkpoint**: You should now understand that `shipment` is null because
`shipmentRepository.findByOrderId(order.getId())` returned `Optional.empty()`.

---

### Task 2 — Find Root Cause via Logs & Debugger (12 min)

**Step 2.1** — Re-run the original (non-strict) endpoint and watch the console logs:
```bash
curl http://localhost:8080/api/orders/ORD-1042/status
```
You should see DEBUG-level logs like:
```
DEBUG OrderStatusService - Resolving status for orderId=ORD-1042
DEBUG OrderStatusService - findByOrderId(1) → 0 rows
ERROR OrderStatusService - shipment is null for orderId=ORD-1042
WARN  OrderStatusService - Falling back to status=PENDING for orderCode=ORD-1042
```

**Step 2.2** — Ask Copilot to interpret the logs:
```
#selection findByOrderId(1) returned 0 rows but I know a shipment exists
for this order. What could cause this?
```

**Step 2.3** — Open the H2 Console (http://localhost:8080/h2-console) and run:
```sql
SELECT * FROM orders WHERE order_code = 'ORD-1042';
-- Note: id = 1

SELECT * FROM shipments;
-- Note: order_id column contains 'ORD-1042' (a STRING), not 1
```

**Step 2.4** — Set a breakpoint in your IDE at
`OrderStatusService.java` on the line:
```java
Shipment shipment = shipmentRepository.findByOrderId(order.getId())
```
Run in Debug mode, hit the endpoint again, and inspect:
- `order.getId()` → `1` (Long)
- The actual `order_id` value in the `shipments` table → `"ORD-1042"` (String)

**Step 2.5** — Ask Copilot to confirm the root cause:
```
@workspace Shipment.orderId is typed as String but ShipmentRepository
.findByOrderId() takes a Long parameter. Is there a type mismatch between
what's stored and what's queried?
```

✅ **Checkpoint**: Root cause confirmed — `Shipment.orderId` stores the
**order code** (`"ORD-1042"`) as a String, but the repository method
queries using the **numeric Order.id** (`1L`). These never match.

---

### Task 3 — Fix, Validate & Review (8 min)

**Step 3.1** — Ask Copilot for a fix:
```
#selection suggest a fix for this orderId type mismatch — should I change
the repository, the entity, or the query?
```

**Step 3.2** — Apply one of these two valid fixes:

**Option A (recommended) — Query by the matching field:**
```java
// ShipmentRepository.java
Optional<Shipment> findByOrderId(String orderCode);   // change Long → String

// OrderStatusService.java
Shipment shipment = shipmentRepository.findByOrderId(order.getOrderCode())
        .orElse(null);
```

**Option B — Align the data model (bigger change, more correct long-term):**
Add a proper foreign key column `order_pk_id BIGINT` to `shipments`,
backfill it, and query by that instead. (Discuss with participants — Option A
is the pragmatic same-day fix; Option B is the "do it right" follow-up ticket.)

**Step 3.3** — Re-run and confirm the fix:
```bash
mvn spring-boot:run
curl http://localhost:8080/api/orders/ORD-1042/status
# → {"orderCode":"ORD-1042","status":"SHIPPED", ...}   ✅ FIXED
```

**Step 3.4** — Run the full test suite:
```bash
mvn test
```
Update the two tests that documented the buggy behaviour
(`demonstrates_the_orderId_type_mismatch_bug` and
`testOrd1042ReturnsCorrectStatus`) to assert the CORRECT behaviour now.

**Step 3.5** — Ask Copilot to review your diff:
```
#selection review this fix — are there other places in the codebase that
assume orderId is a Long when it should be the order code?
```

---

## ✅ Fix Validation Checklist

| # | Check | Command |
|---|-------|---------|
| 1 | Original failing request now returns correct status | `curl .../ORD-1042/status` → `SHIPPED` |
| 2 | Control case still works | `curl .../ORD-2001/status` → `PENDING` |
| 3 | Strict endpoint no longer throws | `curl .../ORD-1042/status-strict` → `200 OK` |
| 4 | Full regression suite passes | `mvn test` → all green |
| 5 | No other orderId type mismatches | `@workspace` search prompt above |

---

## 🎯 Root Cause Summary

| | |
|---|---|
| **Symptom** | API returns `PENDING` for orders that have already shipped |
| **Where it manifests** | `OrderStatusService.resolveStatus()` |
| **Root cause** | `Shipment.orderId` (String, holds order codes) vs. `ShipmentRepository.findByOrderId(Long)` (queries by numeric Order.id) — type/semantic mismatch |
| **Why it's intermittent** | Orders with no shipment yet (legitimately `PENDING`) look identical to orders whose shipment lookup silently fails |
| **The fix** | Query `ShipmentRepository` by `orderCode` (String) instead of `Order.id` (Long) |
| **Anti-pattern to avoid** | Adding `if (shipment == null) return cachedStatus;` — a band-aid that hides the bug instead of fixing the type mismatch |
