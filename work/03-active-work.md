# Active Work

This file is the implementation contract for the current task.

Do not start coding a new feature unless it is summarized here.

## Current Active Work

### Title

Payment authorization use case

### Goal

Implement the first core transactional workflow: payment authorization.

### In Scope

- Payment authorization command model.
- Payment aggregate authorization behavior.
- Application inbound port for authorization.
- Outbound ports required to save payment and ledger records.
- MockK application use case test.
- BehaviorSpec domain test for authorization rules.

### Out of Scope

- HTTP authorization API.
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
- `work/07-git-workflow.md`
- `work/08-obsidian-archive-policy.md`

### Implementation Tasks

- [ ] Define authorization command and result models.
- [ ] Add payment authorization behavior to the aggregate.
- [ ] Add inbound authorization use case contract.
- [ ] Add required outbound ports.
- [ ] Add BehaviorSpec domain tests.
- [ ] Add MockK application use case tests.

### Acceptance Criteria

- [ ] Authorization creates an authorized payment domain object.
- [ ] Invalid amount or blank required identifiers are rejected.
- [ ] Application use case coordinates domain and outbound ports without web or persistence leakage.
- [ ] Tests pass with `./gradlew test`.

### Verification

- Run `./gradlew test`.
- Record result in Obsidian build log.
