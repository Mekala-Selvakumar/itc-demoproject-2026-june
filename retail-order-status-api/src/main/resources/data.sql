-- src/main/resources/data.sql
-- Seed data for Module 4 Debugging Lab
--
-- Orders table: id is auto-generated Long (1, 2, 3, ...)
-- Shipments table: order_id column stores STRING order codes (the bug!)
--
-- This means:
--   Order id=1 has order_code='ORD-1042'
--   Shipment row has order_id='ORD-1042'  (a STRING, not matching Order.id=1)
--
-- So: shipmentRepository.findByOrderId(1L) searches for a row where
-- order_id = 1 (as a number/string "1"), but the actual stored value
-- is the STRING "ORD-1042" — these never match.
--
-- ORD-2001 is a "control" order with NO shipment record at all,
-- included to show two different scenarios:
--   ORD-1042 → has a shipment, but the bug prevents finding it (the main bug)
--   ORD-2001 → legitimately has no shipment yet (correct PENDING status)
--   ORD-3055 → has a shipment too, same bug applies

-- ── Orders ────────────────────────────────────────────────────────────────────
INSERT INTO orders (id, order_code, customer_id, status, created_at, total_amount)
VALUES (1, 'ORD-1042', 'CUST-501', 'PENDING', DATEADD('DAY', -2, CURRENT_TIMESTAMP), 149.99);

INSERT INTO orders (id, order_code, customer_id, status, created_at, total_amount)
VALUES (2, 'ORD-2001', 'CUST-502', 'PENDING', CURRENT_TIMESTAMP, 49.50);

INSERT INTO orders (id, order_code, customer_id, status, created_at, total_amount)
VALUES (3, 'ORD-3055', 'CUST-503', 'PENDING', DATEADD('DAY', -5, CURRENT_TIMESTAMP), 299.00);

-- ── Shipments ─────────────────────────────────────────────────────────────────
-- ⚠️ BUG: order_id stores the STRING order code, not the numeric Order.id.
-- ORD-1042's actual Order.id is 1, but this row's order_id is the string 'ORD-1042'.
INSERT INTO shipments (id, order_id, carrier, tracking_number, shipped_at, status)
VALUES (1, 'ORD-1042', 'BlueDart', 'BD9988771122', DATEADD('DAY', -1, CURRENT_TIMESTAMP), 'IN_TRANSIT');

-- ORD-2001 has NO shipment row — this is a legitimate PENDING order (control case).

-- ORD-3055's actual Order.id is 3, but again the row stores the string code.
INSERT INTO shipments (id, order_id, carrier, tracking_number, shipped_at, status)
VALUES (2, 'ORD-3055', 'FedEx', 'FX1122334455', DATEADD('DAY', -3, CURRENT_TIMESTAMP), 'DELIVERED');
