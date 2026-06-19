# Phase 002: Payment Ledger Domain

This phase introduces the immutable payment ledger domain model and its domain tests.

## Goal

Represent payment history as immutable append-only ledger records.

## Docs Read

- `docs/how/01-domain-model.md`
- `docs/how/00-architecture.md`
- `docs/how/03-test-strategy.md`
- `rules/test-rule.md`
- `rules/database-schema-rule.md`

## Scope

- Add payment ledger domain model.
- Add ledger type or reason values needed for authorization records.
- Add domain BehaviorSpec tests for ledger creation and immutability rules.

## Out Of Scope

- Persistence-backed ledger table.
- Application service ledger append.
- Settlement or reconciliation.

## Files To Touch

- `modules/domain/src/main/kotlin/com/example/cardservice/domain/payment/model`
- `modules/domain/src/test/kotlin/com/example/cardservice/domain/payment/model`

## Test First

- Add ledger BehaviorSpec before production code.
- First failing test: authorization ledger can be created with payment id, amount, currency, and append type.
- Validation command: `./gradlew :domain:test`

## Implementation Steps

- [ ] Add payment ledger model.
- [ ] Add ledger type/reason model if needed.
- [ ] Add BehaviorSpec for valid ledger creation.
- [ ] Add BehaviorSpec for invalid ledger values.

## Done Criteria

- [ ] Authorized payment ledger record can be created.
- [ ] Invalid ledger values are rejected.
- [ ] Ledger model exposes no mutation behavior.
- [ ] `./gradlew :domain:test` passes.

## Validation

- `./gradlew :domain:test`

## Review Focus

- Ledger records must be append-only history records, not mutable payment state.
- Domain model should stay free of adapter/query technology.
