# Phase 8: Payment Spike Before Projection

## Goal

Create the "Before" baseline for the Kafka/outbox performance story: payment traffic performs operational projection/audit work synchronously inside the payment transaction.

## Docs Read

- `work/03-active-work.md`
- `workflow/features/kafka-transactional-outbox-eventing.md`
- `docs/harness/05-architecture.md`
- `docs/harness/06-test-strategy.md`
- `rules/database-schema-rule.md`
- `rules/backend-architecture.md`

## Scope

- Add a payment operational projection/audit table.
- Add a domain/JPA projection entity and application repository port.
- Save a payment projection row synchronously when a new payment succeeds.
- Save a refund projection row synchronously when a refund succeeds.
- Keep idempotent duplicate payment requests from writing duplicate projection rows.
- Add a k6 load-test skeleton for the synchronous projection baseline.

## Out Of Scope

- Kafka broker setup.
- Outbox table.
- Kafka producer/consumer.
- Moving coupon issuance, inventory locking, or payment idempotency to async processing.
- Artificial latency such as `Thread.sleep`.

## Implementation Steps

- [x] Add schema for synchronous payment operational projection.
- [x] Add domain entity and repository port.
- [x] Wire `OrderPaymentFacade` to save projection rows on pay/refund.
- [x] Update application and integration tests.
- [x] Add baseline k6 payment spike script.
- [x] Record validation commands.

## Done Criteria

- Payment success writes one synchronous projection row.
- Duplicate idempotent payment request does not write another projection row.
- Refund writes one synchronous projection row.
- Existing payment/coupon correctness remains unchanged.
- Load-test script exists for the Before baseline.
- Backend tests pass.

## Validation

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :application:test --tests '*OrderPaymentFacadeBehaviorSpec'`
  - Result: passed on 2026-06-24.
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`
  - Result: passed on 2026-06-24.
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test --tests '*CommerceFlowIntegrationTest' -Dtestcontainers.enabled=true`
  - Result: passed on 2026-06-24.
- Local HTTP smoke:
  - `admin_front=200 shop_front=200 admin_api=200 shop_api=200`
  - Shop payment wrote one `PAYMENT_AUTHORIZED` projection row.
  - Duplicate payment retry with the same idempotency key kept projection count at one.
- k6 script execution:
  - `VUS=2 DURATION=5s scripts/load-test-payment-before.sh`
  - Result: passed with `payment_latency p95=568.1ms`, `http_req_failed=0.00%`, and `43` completed iterations.
  - Full baseline run is still needed with production-like VUS/duration.

## Review Focus

Concurrency reviewer: verify duplicate payment calls do not duplicate projection rows and that the projection is clearly marked as the synchronous baseline to be removed/replaced by outbox in the After phase.
