# Feature: Kafka Transactional Outbox Eventing

## Goal

Add a reliable async eventing slice for payment traffic spikes. The feature should prove post-payment projection/audit work can be decoupled from payment request latency without losing events or duplicating side effects.

## Why This Fits

- The current payment flow already proves idempotency, inventory locking, coupon issuance, refund reversal, and immutable history.
- Kafka becomes valuable after that core correctness proof because downstream projection, audit, notification, and settlement-prep work can be processed asynchronously.
- Transactional outbox is the important backend design point: DB state and broker delivery are coordinated without a distributed transaction.
- The traffic story is concrete: when a promotion drives 100-500 concurrent payment attempts, the API should keep the critical payment transaction short and push non-critical post-payment work behind Kafka.

## Problem Scenario

```text
Promotion traffic spike
  -> many concurrent shop payment requests
  -> DB locks protect order/inventory/payment/coupon state
  -> synchronous projection/audit work makes payment p95/p99 worse
  -> downstream projection/audit failure can affect the payment request
```

Kafka is introduced for the projection/audit/settlement-prep lane. It is not introduced to solve oversell prevention, duplicate payment, or coupon issuance.

## First Slice

- Add `OutboxEvent` persistence.
- Append `OrderPaid` and `OrderRefunded` outbox rows inside existing payment/refund transactions.
- Publish pending outbox rows to Kafka topic `commerce.order-events.v1`.
- Add one idempotent consumer that writes an operational projection or audit record.
- Keep coupon issuance synchronous in the first slice.

## Performance Experiment

Baseline:

```text
payment transaction
  -> save order/payment/inventory/coupon/history
  -> synchronously update projection/audit
  -> API response
```

Kafka/outbox:

```text
payment transaction
  -> save order/payment/inventory/coupon/history
  -> append outbox event
  -> API response
  -> publisher sends Kafka event
  -> idempotent consumer updates projection/audit
```

Measure both with the same load profile:

- p50/p95/p99 payment latency.
- successful payments per second.
- failed payments and duplicate side effects.
- DB lock wait symptoms.
- outbox pending count.
- projection lag.
- publish retry count.
- duplicate consumer replay count.

## Before Baseline Result

Environment:

- Local Mac development environment.
- Admin API: `http://127.0.0.1:8082`
- Shop API: `http://127.0.0.1:8081`
- MySQL: local Docker container `card-service-mysql`
- Load runner: Docker `grafana/k6`
- Script: `load-tests/payment-spike-sync-projection.js`
- Wrapper: `scripts/load-test-payment-before.sh`
- Duration: `30s`
- Threshold: `payment_latency p95 < 1500ms`, `http_req_failed < 5%`

Result files:

- `build/load-tests/payment-before-10vus-30s.summary.json`
- `build/load-tests/payment-before-30vus-30s.summary.json`
- `build/load-tests/payment-before-50vus-30s.summary.json`
- `build/load-tests/payment-before-100vus-30s.summary.json`

| VU | iterations | iter/s | payment avg | med | p90 | p95 | max | http fail | checks | threshold |
|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|:---|
| 10 | 1433 | 47.45 | 169.11ms | 118ms | 328.2ms | 454.4ms | 1296ms | 0% | 100% | PASS |
| 30 | 1037 | 33.22 | 425.7ms | 227ms | 690.4ms | 1176ms | 9337ms | 0% | 100% | PASS |
| 50 | 1877 | 61.61 | 339.48ms | 236ms | 803.7ms | 1114.45ms | 1736ms | 0.09% | 99.93% | PASS |
| 100 | 1322 | 38.6 | 950.14ms | 729ms | 1615.9ms | 1746.8ms | 3780ms | 0% | 100% | FAIL |

Interpretation:

- `10 VU` is comfortably inside the target.
- `30 VU` still passes but shows a high max latency spike.
- `50 VU` still passes the threshold but already has member creation timeout failures, so it is the first warning point.
- `100 VU` fails the payment p95 threshold and is the clear Before saturation point.
- After implementation should reuse the same VU/duration matrix and compare request-path p95 plus projection lag.

## Index And Lock Check

Before moving to Kafka, the payment path was checked for missing-index symptoms.

Observed index usage:

- `payments.idempotency_key`: unique index, `EXPLAIN type=const`.
- `commerce_orders.id`: primary key, `EXPLAIN type=const`.
- `order_lines.order_id`: secondary index, `EXPLAIN type=ref`.
- `inventories.product_id`: unique index, `EXPLAIN type=const`.
- `coupons.order_id`: secondary index, `EXPLAIN type=ref`.
- `payment_operational_projections(operation_type, order_id)`: unique index, `EXPLAIN type=const`.

The first clear bottleneck is not an obvious missing index. The baseline load test sends every payment to one product, so every payment locks the same inventory row with `PESSIMISTIC_WRITE`.

Isolation run:

| Scenario | VU | products | iterations | iter/s | payment p95 | http fail | threshold |
|---|---:|---:|---:|---:|---:|---:|:---|
| Hot inventory row | 100 | 1 | 1322 | 38.6 | 1746.8ms | 0% | FAIL |
| Distributed inventory rows | 100 | 20 | 2466 | 72.7 | 1231ms | 0% | PASS |

Conclusion:

- The `100 VU` Before failure is strongly correlated with single-row inventory lock contention.
- Kafka/Outbox will not solve inventory correctness or hot-stock locking by itself.
- Kafka/Outbox can still reduce the transaction tail by moving projection/audit work out of the request path, but the After result should be interpreted separately from inventory lock contention.
- If the goal becomes higher throughput for one hot product, the next design topic is inventory reservation/decrement strategy, not Kafka.

## Pre-Kafka Tuning Result

Before Kafka/Outbox, the payment path was tuned within the current architecture:

- Detailed stage report: `docs/harness/10-performance-improvement-report.md`
- Payment inventory decrement changed from `PESSIMISTIC_WRITE` lock-load-save to conditional SQL update.
- Refund inventory recovery changed to direct SQL increment.
- Order lock, payment idempotency, coupon issuance, and synchronous projection remained unchanged.

| Scenario | VU | products | iterations | iter/s | payment p95 | max | threshold |
|---|---:|---:|---:|---:|---:|---:|:---|
| Before hot inventory | 100 | 1 | 1322 | 38.60 | 1746.8ms | 3780ms | FAIL |
| Tuned hot inventory | 100 | 1 | 1881 | 59.87 | 1461ms | 1872ms | PASS |
| Tuned hot inventory | 150 | 1 | 1735 | 51.39 | 1946.5ms | 3317ms | FAIL |

Result:

- `100 VU / 1 product` moved from FAIL to PASS.
- Throughput improved by about `55.1%`.
- Payment p95 improved by about `16.4%`.
- Payment max latency improved by about `50.5%`.
- `150 VU / 1 product` still fails, so the next bottleneck is not fully removed.

Next decision:

- If the target is `100 VU` hot-product traffic, Kafka is not required yet.
- If the target is higher hot-product traffic, evaluate inventory reservation/bucket strategy before claiming Kafka solves the bottleneck.
- Kafka/Outbox is still a valid next feature for projection/audit decoupling and failure isolation, but its performance result must be measured separately.

## Kafka/Outbox Slice Result

Kafka/outbox was implemented as a second slice after the pre-Kafka inventory update tuning.

Implemented:

- `outbox_events`
- `processed_outbox_events`
- transactional outbox append in payment/refund transaction
- Kafka publisher for pending outbox rows
- Kafka consumer for operational projection writes
- local Kafka Docker Compose service
- Kafka local stack and load-test wrappers

Functional smoke:

- `VUS=2 DURATION=5s PRODUCT_COUNT=1 scripts/load-test-payment-after-kafka.sh`
- result: passed
- outbox: `PUBLISHED=61`, processed `61`, unpublished `0`

Performance comparison:

| Scenario | VU | products | iterations | iter/s | payment p95 | outbox state | threshold |
|---|---:|---:|---:|---:|---:|---|:---|
| Tuned sync projection | 100 | 1 | 1881 | 59.87 | 1461ms | synchronous | PASS |
| Kafka worker in API process | 100 | 1 | 1253 | 36.08 | 1932.6ms | unpublished `0`, max lag `4s` | FAIL |
| Kafka worker split process | 100 | 1 | 403 | 11.03 | 13799.6ms | unpublished `0`, max lag `6s` | FAIL |
| Kafka worker delayed `60s` | 100 | 1 | 790 | 20.03 | 6083.2ms | pending `690`, max lag `39s` | FAIL |

Decision:

- Kafka/outbox is functionally valid but is not a successful performance optimization in the current local setup.
- The tuned synchronous baseline remains faster for `100 VU / 1 product`.
- The result is still useful portfolio evidence because it shows a negative measurement and a corrected design conclusion.
- Kafka should be kept as a reliability/failure-isolation feature, not claimed as the hot-product throughput fix.
- If performance remains the main target, next work should focus on inventory model scale-up or outbox worker optimization, not just "adding Kafka".

## Pipeline

- backend: create a new phase only after `phase-007-paginated-query-cqrs.md` is archived or explicitly superseded.
- frontend: no first-slice UI dependency.
- optional admin UI later: outbox pending/failed count and latest event timestamp.

## Architecture

```text
shop-api/admin-api
  -> OrderPaymentUseCase
  -> OrderPaymentFacade transaction
  -> OutboxEventRepository.save(...)
  -> OutboxPublisher scheduled job
  -> Kafka commerce.order-events.v1
  -> Idempotent consumer
  -> projection/audit table
```

## Out Of Scope

- Exactly-once Kafka semantics.
- Distributed transaction manager.
- Schema Registry.
- Moving coupon issuance async.
- Using Kafka for inventory locking or payment idempotency.
- Real notification providers.
- Kubernetes or cloud deployment.

## Done Criteria

- Payment/refund integration tests prove outbox rows are committed with business state.
- Publisher test proves pending rows become published after Kafka send.
- Failure test proves publish errors leave rows retryable and visible.
- Consumer test proves replay/duplicate delivery is idempotent.
- Local Docker Compose includes Kafka for manual validation.
- k6 or Gatling load tests compare synchronous projection/audit with outbox Kafka under the same payment spike profile.
- The measured result documents the tradeoff: lower request-path latency or better failure isolation, with explicit projection lag and outbox complexity.
- README/harness docs explain the outbox decision and verification commands.

## Review Focus

- Concurrency reviewer: duplicate event handling, retry behavior, and outbox state transitions.
- Database reviewer: outbox schema, indexes for pending polling, attempt count, and cleanup strategy.
- Portfolio reviewer: whether the feature demonstrates latency, reliability, and operational recovery rather than just "using Kafka".
