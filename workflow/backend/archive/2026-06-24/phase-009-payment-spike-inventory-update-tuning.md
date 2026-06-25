# Phase 009: Payment Spike Inventory Update Tuning

## Goal

Improve the current synchronous payment path before introducing Kafka. The first target is hot inventory row contention observed in the Before load test.

## Problem

The baseline payment spike test sends many payment requests to one product. The current payment path loads the inventory row with `PESSIMISTIC_WRITE`, mutates the entity, then saves it. Under hot-product traffic this keeps the same inventory row locked across the rest of the payment transaction.

## Scope

- Keep payment, order state, coupon issuance, and operational projection synchronous.
- Replace payment/refund inventory mutation with conditional SQL updates:
  - pay: `quantity = quantity - requested` only when enough stock exists.
  - refund: `quantity = quantity + refunded`.
- Keep existing pessimistic lock paths for order locking and coupon exchange.
- Add/adjust behavior and integration tests for insufficient stock and projection behavior.
- Re-run the same k6 baseline matrix enough to compare the hot-product case.

## Out Of Scope

- Kafka/Outbox.
- Inventory reservation tables.
- Stock bucket/sharding design.
- Moving coupon issuance async.
- Changing API contracts.

## Measurement Plan

Before reference:

| Scenario | VU | products | iterations | iter/s | payment p95 | threshold |
|---|---:|---:|---:|---:|---:|:---|
| Hot inventory row | 100 | 1 | 1322 | 38.6 | 1746.8ms | FAIL |
| Distributed inventory rows | 100 | 20 | 2466 | 72.7 | 1231ms | PASS |

After tuning:

- Run `100 VU / 30s / 1 product`.
- Optionally run `50 VU / 30s / 1 product` if the 100 VU result still fails.
- Compare p95, iter/s, HTTP failure rate, and check pass rate.

## Done Criteria

- [x] Application tests pass.
- [x] Testcontainers commerce integration test passes.
- [x] k6 hot-product measurement is recorded under `build/load-tests`.
- [x] Result is documented before deciding whether Kafka is still the next step.

## Implementation

- Added `InventoryMutationPort` in the application layer.
- Added `JpaInventoryMutationAdapter` in the infra layer.
- Changed payment inventory decrement from lock-load-save to conditional update:
  - `quantity = quantity - requested`
  - only when `quantity >= requested`
- Changed refund inventory recovery to a direct increment update.
- Kept order pessimistic locking and coupon exchange locking unchanged.

## Validation

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'`
  - passed
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test --tests '*CommerceFlowIntegrationTest' -Dtestcontainers.enabled=true`
  - passed

## Measurement Result

| Scenario | VU | products | iterations | iter/s | payment avg | med | p90 | p95 | max | http fail | checks | threshold |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|:---|
| Before hot inventory | 100 | 1 | 1322 | 38.60 | 950.14ms | 729ms | 1615.9ms | 1746.8ms | 3780ms | 0.00% | 100.00% | FAIL |
| Tuned hot inventory | 100 | 1 | 1881 | 59.87 | 626.46ms | 481ms | 1347ms | 1461ms | 1872ms | 0.00% | 100.00% | PASS |
| Tuned hot inventory | 150 | 1 | 1735 | 51.39 | 1016.02ms | 812ms | 1789ms | 1946.5ms | 3317ms | 0.00% | 100.00% | FAIL |

Compared with the original `100 VU / 1 product` baseline:

- Throughput improved by about `55.1%`.
- Payment p95 improved by about `16.4%`.
- Payment max latency improved by about `50.5%`.

## Decision

The current structure can be improved without Kafka by shortening the hot inventory update. This moves the single-product target from `100 VU FAIL` to `100 VU PASS`, but `150 VU` still fails. Kafka/Outbox remains useful only for decoupling projection/audit work; it should not be presented as the fix for hot inventory contention.
