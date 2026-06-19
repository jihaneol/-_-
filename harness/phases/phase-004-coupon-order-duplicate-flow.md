# Phase 004: Coupon Order Duplicate Flow

This phase tightens the coupon order facade so duplicate requests do not repeat external side effects.

## Goal

Avoid repeated external payment approval and coupon accrual when a coupon order request is a duplicate by idempotency key.

## Docs Read

- `docs/backend/04-api-contract.md`
- `docs/backend/05-architecture.md`
- `docs/backend/06-test-strategy.md`
- `rules/concurrency-rule.md`
- `rules/transaction-rule.md`
- `rules/test-rule.md`

## Scope

- Add or refine application tests for duplicate coupon order requests.
- Adjust coupon order orchestration so duplicate requests return existing payment result without repeating external approval.
- Keep behavior inside application/service ports.

## Out Of Scope

- Real card network integration.
- Persistent coupon issuance store.
- Full DB-backed concurrency test.
- Cancellation, settlement, and reconciliation.

## Files To Touch

- `modules/application/src/main/kotlin/com/example/cardservice/application/payment`
- `modules/application/src/test/kotlin/com/example/cardservice/application/payment`

## Test First

- Update `CouponOrderFacadeBehaviorSpec` before production code.
- First failing test: duplicate request does not call external payment approval twice.
- Validation command: `./gradlew :application:test`

## Implementation Steps

- [ ] Add duplicate coupon order behavior test.
- [ ] Adjust facade or use case contract to expose duplicate detection.
- [ ] Avoid repeated external approval for duplicate requests.
- [ ] Preserve normal new coupon order behavior.

## Done Criteria

- [ ] Duplicate request does not call external payment approval twice.
- [ ] Duplicate request does not create duplicate coupon accrual side effects.
- [ ] New request behavior remains unchanged.
- [ ] `./gradlew :application:test` passes.

## Validation

- `./gradlew :application:test`

## Review Focus

- External side effects must not repeat for duplicate requests.
- Application orchestration should not hide partial failure risks.
