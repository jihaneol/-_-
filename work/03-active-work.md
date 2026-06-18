# Active Work

This file is the implementation contract for the current task.

Do not start coding a new feature unless it is summarized here.

## Current Active Work

### Title

Payment idempotency and ledger

### Goal

Strengthen the Starbucks coupon payment order workflow with duplicate request prevention and immutable payment ledger records.

### In Scope

- Idempotency behavior for Starbucks coupon order/payment requests.
- Immutable payment ledger domain model.
- Append ledger outbound port.
- In-memory ledger adapter.
- Tests proving duplicate requests do not create duplicate payment/ledger records.
- BehaviorSpec tests for ledger append rules.

### Out of Scope

- Payment cancellation.
- Settlement batch.
- Reconciliation report.
- Kafka/RabbitMQ or transactional outbox.
- React frontend.
- Real card network or VAN integration.

### Development Basis

- `docs/harness/03-domain-model.md`
- `docs/harness/04-api-contract.md`
- `docs/harness/05-architecture.md`
- `docs/harness/06-test-strategy.md`
- `work/02-prioritized-roadmap.md`
- `work/05-dev-checklist.md`
- `rules/git-workflow.md`
- `rules/obsidian-archive-policy.md`

### Implementation Tasks

- [ ] Define immutable payment ledger model.
- [ ] Add ledger append outbound port.
- [ ] Add in-memory ledger adapter.
- [ ] Add idempotency lookup/store behavior.
- [ ] Add duplicate request tests.
- [ ] Add ledger append tests.

### Acceptance Criteria

- [ ] Same idempotency key does not create duplicate payment records.
- [ ] Authorized payment writes an immutable ledger record.
- [ ] Duplicate request behavior is covered by tests.
- [ ] Tests pass with `./gradlew test`.

### Verification

- Run `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`.
- Record result in Obsidian build log.
