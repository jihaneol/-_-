# Phase 003: Ledger Append Port

This phase connects payment authorization to ledger append through an application outbound port.

## Goal

Append an authorization ledger record when a new payment authorization is saved.

## Docs Read

- `docs/backend/03-domain-model.md`
- `docs/backend/05-architecture.md`
- `docs/backend/06-test-strategy.md`
- `rules/transaction-rule.md`
- `rules/test-rule.md`

## Scope

- Add append ledger outbound port.
- Add in-memory ledger adapter.
- Update authorization service orchestration for new payment authorizations.
- Add application tests proving ledger append occurs for new authorization.

## Out Of Scope

- Persistence-backed ledger table.
- Settlement and reconciliation.
- Duplicate coupon order external-call suppression.
- Concurrency integration test.

## Files To Touch

- `modules/application/src/main/kotlin/com/example/cardservice/application/payment`
- `modules/application/src/main/kotlin/com/example/cardservice/application/payment/provided`
- `modules/application/src/test/kotlin/com/example/cardservice/application/payment`
- `modules/infra/src/main/kotlin/com/example/cardservice/infra/payment/persistence`

## Test First

- Update application BehaviorSpec before production code.
- First failing test: new authorization appends exactly one authorization ledger record.
- Validation command: `./gradlew :application:test`

## Implementation Steps

- [ ] Add append ledger port.
- [ ] Add in-memory ledger adapter.
- [ ] Update authorization service to append ledger on new payment.
- [ ] Verify duplicate idempotency path does not append a new ledger record.

## Done Criteria

- [ ] New authorization appends one ledger record.
- [ ] Duplicate idempotency path does not append another ledger record.
- [ ] Application tests cover the orchestration.
- [ ] `./gradlew :application:test` passes.

## Validation

- `./gradlew :application:test`

## Review Focus

- Payment save and ledger append transaction boundary must be explainable.
- Duplicate request behavior must not create duplicate ledger records.
