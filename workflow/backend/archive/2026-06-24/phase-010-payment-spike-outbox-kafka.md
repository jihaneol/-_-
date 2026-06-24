# Phase 010: Payment Spike Outbox Kafka

## Goal

Move payment operational projection writes out of the synchronous payment request path using transactional outbox and Kafka.

## Review Decision

Proceed with Kafka only for projection/audit decoupling and downstream failure isolation.

Do not present Kafka as the fix for hot inventory row contention. Phase 009 already improved the hot-product `100 VU` path with conditional inventory updates, while `150 VU` still points to inventory-model scale-up work.

## Scope

- Add `outbox_events` table and persistence model.
- Add `processed_outbox_events` table for consumer idempotency.
- Append `PAYMENT_AUTHORIZED` and `PAYMENT_REFUNDED` outbox rows inside existing payment/refund transactions.
- Remove synchronous `payment_operational_projections` writes from the payment/refund request path.
- Add a scheduled Kafka publisher for pending outbox events.
- Add a Kafka consumer that writes `payment_operational_projections` idempotently.
- Add local Docker Compose Kafka service.
- Add an After load-test wrapper and document the measurement plan.

## Out Of Scope

- Inventory lock/stock bucket redesign.
- Exactly-once Kafka semantics.
- Schema Registry.
- Moving coupon issuance async.
- Admin UI for outbox monitoring.

## Measurement Plan

Use the same payment spike script and compare:

- `100 VU / 30s / 1 product`
- `150 VU / 30s / 1 product`

Metrics:

- payment p95
- iter/s
- HTTP/check failure rate
- outbox pending count
- projection count lag
- publisher failure count

## Done Criteria

- [x] Application behavior tests prove payment appends outbox and does not synchronously save projection.
- [x] Integration test proves outbox row is committed with payment state.
- [x] Publisher/consumer components compile and are gated behind config.
- [x] Local Docker Compose includes Kafka.
- [x] Backend full test suite passes.
- [x] After load wrapper exists.
- [x] Measurement result is recorded, including the negative performance result.

## Implementation

- Added `OutboxEvent` and `ProcessedOutboxEvent` JPA entities.
- Added `OutboxEventRepository` and `ProcessedOutboxEventRepository`.
- Changed `OrderPaymentFacade`:
  - payment success now appends `PAYMENT_AUTHORIZED` outbox event.
  - refund success now appends `PAYMENT_REFUNDED` outbox event.
  - synchronous `payment_operational_projections` save was removed from the request path.
- Added Kafka publisher:
  - polls `PENDING` and `FAILED` outbox rows.
  - sends payload to `commerce.order-events.v1`.
  - marks rows `PUBLISHED` or `FAILED`.
- Added Kafka consumer:
  - consumes payment operational events.
  - writes `payment_operational_projections`.
  - records `processed_outbox_events` for idempotency.
- Added local Kafka Compose service using `apache/kafka:3.8.0`.
- Added `scripts/local-stack-kafka.sh`.
- Added `scripts/load-test-payment-after-kafka.sh`.

## Validation

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'`
  - passed
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test --tests '*CommerceFlowIntegrationTest' -Dtestcontainers.enabled=true`
  - passed
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`
  - passed
- Kafka smoke:
  - `VUS=2 DURATION=5s PRODUCT_COUNT=1 scripts/load-test-payment-after-kafka.sh`
  - retry after local schema fix passed
  - outbox `PUBLISHED=61`, processed `61`, unpublished `0`

## Measurement Result

Reference:

| Scenario | VU | products | iterations | iter/s | payment p95 | threshold |
|---|---:|---:|---:|---:|---:|:---|
| Tuned sync projection | 100 | 1 | 1881 | 59.87 | 1461ms | PASS |

Kafka/outbox measurements:

| Scenario | VU | products | iterations | iter/s | payment p95 | outbox state | threshold |
|---|---:|---:|---:|---:|---:|---|:---|
| Kafka worker in API process | 100 | 1 | 1253 | 36.08 | 1932.6ms | unpublished `0`, max lag `4s` | FAIL |
| Kafka worker split process | 100 | 1 | 403 | 11.03 | 13799.6ms | unpublished `0`, max lag `6s` | FAIL |
| Kafka worker delayed `60s` | 100 | 1 | 790 | 20.03 | 6083.2ms | pending `690`, max lag `39s` | FAIL |

## Decision

Kafka/outbox is functionally implemented, but the first local performance result is negative.

This confirms the earlier review: Kafka should not be treated as a guaranteed performance improvement. In this local setup it adds outbox insert, Kafka runtime, publisher polling, consumer writes, and extra DB pressure. The request path did not improve compared with the tuned synchronous baseline.

The feature is still useful as a reliability and failure-isolation pattern, but it is not yet a successful performance optimization. Next work should either:

- optimize the outbox implementation separately, or
- pause Kafka and continue with inventory model scale-up if the target is hot-product throughput.
