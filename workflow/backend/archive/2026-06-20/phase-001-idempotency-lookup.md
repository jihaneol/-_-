# Phase 001: Idempotency Lookup

## Archive Decision

- Archived at: 2026-06-20
- Disposition: superseded by the commerce order payment idempotency flow.
- Reason: the current presentable backend scope is order payment, coupon issuance, refund, exchange, wallet, and consistency reporting. `OrderPaymentFacade.payOrder` now checks `PaymentRepository.findByIdempotencyKeyValue` before side effects and has behavior coverage for duplicate payment requests. The older standalone `AuthorizePaymentService` slice is no longer the active workflow entrypoint.
- Obsidian record: `07.Build Logs/card-service/days/2026-06-20-3일차.md`

This phase adds the first idempotency read path before creating a new payment.

## Goal

Return an existing authorized payment result when the same idempotency key has already been saved.

## Docs Read

- `docs/how/01-domain-model.md`
- `docs/how/02-api-contract.md`
- `docs/how/00-architecture.md`
- `docs/how/03-test-strategy.md`
- `docs/what/02-roadmap.md`
- `rules/concurrency-rule.md`
- `rules/test-rule.md`

## Scope

- Add an outbound port for finding payment by idempotency key.
- Add repository support for idempotency lookup.
- Update `AuthorizePaymentService` to return an existing payment when found.
- Add application tests proving duplicate authorization does not call save again.

## Out Of Scope

- Ledger model.
- Ledger append.
- Coupon order facade duplicate external-call suppression.
- Database-level concurrency integration test.

## Files To Touch

- `modules/application/src/main/kotlin/com/example/cardservice/application/payment`
- `modules/application/src/main/kotlin/com/example/cardservice/application/payment/provided`
- `modules/application/src/test/kotlin/com/example/cardservice/application/payment`
- `modules/infra/src/main/kotlin/com/example/cardservice/infra/payment/persistence`

## Test First

- Update `AuthorizePaymentServiceBehaviorSpec` before production code.
- First failing test: existing payment for the same idempotency key is returned and save is not called.
- Validation command: `./gradlew :application:test`

## Implementation Steps

- [ ] Add idempotency lookup port.
- [ ] Add repository lookup method.
- [ ] Update authorization service idempotency branch.
- [ ] Update application BehaviorSpec.

## Done Criteria

- [ ] Existing idempotency key returns the existing payment result.
- [ ] Duplicate authorization path does not call save.
- [ ] New idempotency key still saves a new authorized payment.
- [ ] `./gradlew :application:test` passes.

## Validation

- `./gradlew :application:test`

## Review Focus

- Idempotency must return an existing result instead of relying only on duplicate key exceptions.
- Request meaning mismatch policy can remain documented for a later DB/concurrency phase, but must not be hidden.
