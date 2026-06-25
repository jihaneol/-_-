# Performance Improvement Report

## Summary

The payment spike experiment started from a fully synchronous transaction path. Payment, inventory decrease, coupon issuance, coupon history, and operational projection were all handled before the API response.

The first failure looked like a candidate for Kafka/Outbox, but measurement showed the main bottleneck was hot inventory row contention. The first improvement therefore stayed inside the current architecture and changed inventory decrease from a long pessimistic lock path to a conditional atomic update.

Result:

- `100 VU / 30s / 1 product` moved from FAIL to PASS.
- Throughput improved from `38.60 iter/s` to `59.87 iter/s`.
- Payment p95 improved from `1746.8ms` to `1461ms`.
- `150 VU / 30s / 1 product` still fails, so the next scale-up step should focus on the inventory model before presenting Kafka as the solution.

## Stage 1. Synchronous Before Baseline

Initial payment path:

```text
POST /orders/{orderId}/pay
  -> lock order
  -> find payment by idempotency key
  -> lock inventory row
  -> decrease inventory entity
  -> save payment
  -> mark order paid
  -> issue coupons
  -> append coupon histories
  -> save operational projection
  -> return API response
```

Important property:

- The flow was correct and easy to reason about.
- All side effects were committed in one synchronous transaction.
- The tradeoff was a long request transaction under traffic spikes.

Before measurement:

| Scenario | VU | products | iterations | iter/s | payment p95 | max | threshold |
|---|---:|---:|---:|---:|---:|---:|:---|
| Before hot inventory | 100 | 1 | 1322 | 38.60 | 1746.8ms | 3780ms | FAIL |

## Stage 2. Index Check

Before changing architecture, the representative payment queries were checked with MySQL `EXPLAIN`.

Observed access patterns:

- `payments.idempotency_key`: unique index, `type=const`
- `commerce_orders.id`: primary key, `type=const`
- `order_lines.order_id`: secondary index, `type=ref`
- `inventories.product_id`: unique index, `type=const`
- `coupons.order_id`: secondary index, `type=ref`
- `payment_operation_records(operation_type, order_id)`: unique index, `type=const`

Conclusion:

- The first failure was not explained by an obvious missing index.
- The next suspected bottleneck was row-level lock contention.

## Stage 3. Lock Contention Isolation

The load script originally sent every payment to one product. That meant every payment tried to update the same `inventories` row.

Isolation measurement:

| Scenario | VU | products | iterations | iter/s | payment p95 | threshold |
|---|---:|---:|---:|---:|---:|:---|
| Hot inventory row | 100 | 1 | 1322 | 38.60 | 1746.8ms | FAIL |
| Distributed inventory rows | 100 | 20 | 2466 | 72.70 | 1231ms | PASS |

Conclusion:

- The bottleneck was strongly correlated with a single hot inventory row.
- Kafka would not remove the need to protect stock correctness.
- Kafka could still help later with projection/audit decoupling, but not as the primary fix for hot stock contention.

## Stage 4. Pre-Kafka Improvement

The inventory decrease path was changed while keeping the rest of the payment transaction synchronous.

Before:

```text
select inventory by product_id for update
  -> load Inventory entity
  -> Inventory.decrease(quantity)
  -> save Inventory entity
```

After:

```sql
update inventories
set quantity = quantity - :quantity
where product_id = :productId
  and quantity >= :quantity
```

Implementation:

- Added `InventoryMutationPort` in the application layer.
- Added `JpaInventoryMutationAdapter` in the infra layer.
- Changed payment inventory decrement to conditional atomic update.
- Changed refund inventory recovery to direct increment update.
- Kept order pessimistic locking, idempotency lookup, payment save, coupon issuance, coupon history, and projection save synchronous.

Why this change helps:

- The database still serializes updates to the same stock row.
- The application no longer holds a loaded inventory entity through a wider lock-load-save path.
- The stock sufficiency check and decrement happen in one SQL statement.
- Oversell prevention remains in the database condition.

## Stage 5. Measurement After Tuning

| Scenario | VU | products | iterations | iter/s | payment avg | med | p90 | p95 | max | http fail | checks | threshold |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|:---|
| Before hot inventory | 100 | 1 | 1322 | 38.60 | 950.14ms | 729ms | 1615.9ms | 1746.8ms | 3780ms | 0.00% | 100.00% | FAIL |
| Tuned hot inventory | 100 | 1 | 1881 | 59.87 | 626.46ms | 481ms | 1347ms | 1461ms | 1872ms | 0.00% | 100.00% | PASS |
| Tuned hot inventory | 150 | 1 | 1735 | 51.39 | 1016.02ms | 812ms | 1789ms | 1946.5ms | 3317ms | 0.00% | 100.00% | FAIL |

Improvement at `100 VU / 1 product`:

- Throughput: `+55.1%`
- Payment p95: `-16.4%`
- Payment max latency: `-50.5%`

Result files:

- `build/load-tests/payment-before-100vus-30s.summary.json`
- `build/load-tests/payment-tuned-100vus-30s-1product.summary.json`
- `build/load-tests/payment-tuned-150vus-30s-1product.summary.json`

## Current Decision

If the target is `100 VU / 30s / 1 hot product`, Kafka is not required yet. The current structure now passes that target after the inventory update tuning.

If the target is `150 VU+ / 1 hot product`, Kafka should not be the next explanation. The next design candidates are:

- inventory reservation table
- stock bucket/sharding
- atomic stock command with shorter transaction boundaries
- separate order acceptance and payment confirmation policy

Kafka/Outbox remains a good next feature only for a different problem:

- moving projection/audit work out of the payment response path
- retryable event delivery
- failure isolation for downstream processing
- measuring projection lag and outbox backlog

It should not be described as the fix for hot inventory row contention.

## Verification

Commands passed:

```bash
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test --tests '*CommerceFlowIntegrationTest' -Dtestcontainers.enabled=true
JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test
```

Load commands:

```bash
LOAD_TEST_RUN_ID=payment-tuned-100vus-30s-1product VUS=100 DURATION=30s PRODUCT_COUNT=1 scripts/load-test-payment-before.sh
LOAD_TEST_RUN_ID=payment-tuned-150vus-30s-1product VUS=150 DURATION=30s PRODUCT_COUNT=1 scripts/load-test-payment-before.sh
```
