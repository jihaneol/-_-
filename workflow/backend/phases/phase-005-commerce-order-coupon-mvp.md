# Phase 005: Commerce Order Coupon MVP

This phase expands the first coupon order flow into a minimum commerce MVP.

## Goal

Implement the smallest complete flow where a member orders a product, pays the order, inventory is deducted, and stamp coupon issuance records are created. The MVP also includes minimum CRUD, pre-payment order cancellation, full refund, soft delete, and immutable history.

## Docs Read

- `docs/operations/01-feature-candidates.md`
- `docs/what/01-project-scope.md`
- `docs/how/01-domain-model.md`
- `docs/how/02-api-contract.md`
- `docs/how/03-test-strategy.md`
- `rules/concurrency-rule.md`
- `rules/database-schema-rule.md`
- `rules/jpa-entity-rule.md`
- `rules/transaction-rule.md`
- `rules/test-rule.md`

## Scope

- Add member, product, inventory, order, coupon, and coupon history domain models.
- Add minimum CRUD APIs for member, product, inventory, and order.
- Add order payment API that authorizes payment, deducts inventory, and issues stamp coupons.
- Issue one coupon stamp per `5000 KRW` paid amount.
- Add pre-payment order cancellation.
- Add full refund for paid orders. Partial refund is rejected.
- Full refund voids issued coupon stamps and appends reversal history without deleting issuance records.
- Use soft delete for user-facing member, product, and order delete actions.
- Keep payment and coupon history append-only.
- Preserve idempotency so duplicate payment requests do not duplicate payment, inventory deduction, or coupon issuance.

## Out Of Scope

- Coffee exchange after ten coupon stamps.
- Partial refund.
- Manual coupon grant or revoke.
- Cart, promotion, discount, or product option complexity.
- Real external PG/VAN integration.
- Kafka/RabbitMQ and transactional outbox.
- Frontend implementation unless selected as a separate phase.

## Files To Touch

- `modules/domain/src/main/kotlin/com/example/cardservice/domain`
- `modules/application/src/main/kotlin/com/example/cardservice/application`
- `modules/infra/src/main/kotlin/com/example/cardservice/infra`
- `modules/bootstrap/src/main/kotlin/com/example/cardservice/web`
- `modules/domain/src/test/kotlin/com/example/cardservice/domain`
- `modules/application/src/test/kotlin/com/example/cardservice/application`
- `modules/bootstrap/src/test/kotlin/com/example/cardservice`
- `sql/schema`

## Test First

- Add domain tests before production code.
- First failing test: paying a `12000 KRW` order issues exactly two coupon stamps and records coupon history.
- Validation commands:
  - `./gradlew :domain:test`
  - `./gradlew :application:test`
  - `./gradlew :bootstrap:test`

## Implementation Steps

- [x] Add domain model tests for order payment, inventory guard, coupon issuance count, cancellation, full refund coupon voiding, and soft delete.
- [x] Add JPA/domain models and schema for member, product, inventory, order, order line, coupon, and coupon history.
- [x] Add application use cases and ports for minimum CRUD.
- [x] Add order payment orchestration with idempotency, inventory deduction, payment authorization, and coupon issuance.
- [x] Add full refund orchestration and reject partial refund requests.
- [x] Add REST adapters for MVP APIs.
- [x] Add integration tests for successful payment, duplicate payment, insufficient inventory, pre-payment cancellation, and full refund.
- [x] Update API and test documentation after implementation details settle.

## Done Criteria

- [x] Member/product/inventory/order minimum CRUD works.
- [x] Payment succeeds only when order is payable and inventory is available.
- [x] `5000 KRW` paid amount maps to one issued coupon stamp.
- [x] Duplicate order payment request does not duplicate payment, inventory deduction, or coupon issuance.
- [x] Order cancellation works before payment and is rejected after payment.
- [x] Full refund works once and partial refund is rejected.
- [x] Full refund voids all coupons issued from that order and appends reversal history.
- [x] User-facing delete actions are soft deletes.
- [x] Coupon history is append-only.
- [x] Relevant Gradle tests pass.

## Remaining Test Gap

- Full Spring Boot persistence integration tests were added behind `-Dtestcontainers.enabled=true`. They could not be executed in the current local run because Docker daemon is not running.

## Validation

- `./gradlew :domain:test :application:test :bootstrap:test`

## Review Focus

- Aggregate boundaries between order, payment, inventory, and coupon issuance.
- Transaction boundary for payment, inventory deduction, and coupon issuance.
- Idempotency and duplicate side-effect prevention.
- Soft delete query behavior and immutable history records.
