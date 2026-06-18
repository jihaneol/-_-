# Active Work

This file is the implementation contract for the current task.

Do not start coding a new feature unless it is summarized here.

## Current Active Work

### Title

Backend project scaffold

### Goal

Create the initial Kotlin Spring Boot backend project structure for `card-service` using DDD and hexagonal architecture.

### In Scope

- Gradle Kotlin project scaffold.
- Spring Boot backend module.
- DDD/hexagonal package structure.
- MySQL Docker Compose setup.
- Flyway or equivalent migration baseline.
- Kotest BehaviorSpec setup.
- MockK setup.
- Testcontainers setup for MySQL integration tests.
- Basic application health check.
- Initial `Payment` aggregate skeleton.

### Out of Scope

- Payment authorization API implementation.
- Payment cancellation API implementation.
- Settlement batch.
- Reconciliation report.
- Kafka/RabbitMQ or transactional outbox.
- React frontend scaffold.
- Real card network or VAN integration.

### Development Basis

- `docs/harness/03-domain-model.md`
- `docs/harness/05-architecture.md`
- `docs/harness/06-test-strategy.md`
- `docs/harness/07-week-plan.md`
- `work/02-prioritized-roadmap.md`
- `work/05-dev-checklist.md`
- `work/07-git-workflow.md`
- `work/08-obsidian-archive-policy.md`

### Implementation Tasks

- [ ] Create backend project files.
- [ ] Configure Kotlin, Spring Boot, and Gradle.
- [ ] Add DDD/hexagonal package skeleton.
- [ ] Add MySQL Docker Compose.
- [ ] Add migration baseline.
- [ ] Add Kotest, MockK, and Testcontainers dependencies.
- [ ] Add one smoke test.
- [ ] Add initial `Payment` aggregate skeleton.

### Acceptance Criteria

- [ ] Backend project can compile.
- [ ] Test command runs successfully.
- [ ] MySQL test container can start in integration tests.
- [ ] Package structure matches `docs/harness/05-architecture.md`.
- [ ] No payment business feature is implemented beyond skeleton scope.

### Verification

- Run backend build/test command after scaffolding.
- Record result in Obsidian build log.
