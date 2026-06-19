# Capability Map

| Requirement | Project artifact | Proof |
|---|---|---|
| Kotlin + Spring Boot | Payment REST API | Source code, API tests |
| DDD | Aggregates, value objects, invariants | Domain tests |
| Hexagonal architecture | `domain`, `application`, `admin-api`, `shop-api`, `batch`, `infra`, `external` Gradle modules | Module dependencies, architecture notes |
| CQRS | Separate command/query use cases, ports, and adapters | Command domain tests, infra QueryDSL adapter tests |
| RDBMS modeling | MySQL schema for merchant, payment, ledger, settlement | ERD, `sql/` schema files |
| Query optimization | QueryDSL read adapters and indexes for merchant/date/status lookup | Query-plan note |
| Complex business logic | Authorize, cancel, settle, reconcile | Behavior-style domain tests |
| Concurrency | Idempotency key and locking strategy | Race-condition test |
| Transaction handling | Atomic payment state + ledger write | Integration test |
| Async processing | Payment event outbox and broker publishing | Consumer test |
| Batch processing | Daily settlement job in the `batch` module | Batch adapter test |
| Data reconciliation | Ledger-settlement mismatch report | Reconciliation test |
| Docker readiness | Local compose stack | One-command setup |
| Communication | Decision log and dev log | README and workflow docs |
| Mocking strategy | MockK for outbound ports | Application use case tests |

## Priority

1. Payment authorization/cancellation correctness.
2. Ledger immutability and settlement/reconciliation.
3. Concurrency/idempotency.
4. Tests and measurable proof.
5. Async events.
6. Docker polish.
