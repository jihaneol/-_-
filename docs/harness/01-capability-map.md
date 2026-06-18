# Capability Map

| Requirement | Project artifact | Proof |
|---|---|---|
| Kotlin + Spring Boot | Payment REST API | Source code, API tests |
| DDD | Aggregates, value objects, invariants | Domain tests |
| Hexagonal architecture | Inbound/outbound ports and adapters | Package structure, architecture notes |
| RDBMS modeling | MySQL schema for merchant, payment, ledger, settlement | ERD, migration files |
| Query optimization | Indexes for merchant/date/status lookup | Query-plan note |
| Complex business logic | Authorize, cancel, settle, reconcile | Behavior-style domain tests |
| Concurrency | Idempotency key and locking strategy | Race-condition test |
| Transaction handling | Atomic payment state + ledger write | Integration test |
| Async processing | Payment event outbox and broker publishing | Consumer test |
| Batch processing | Daily settlement job | Batch test |
| Data reconciliation | Ledger-settlement mismatch report | Reconciliation test |
| Docker readiness | Local compose stack | One-command setup |
| Communication | Decision log and dev log | README and harness docs |
| Mocking strategy | MockK for outbound ports | Application use case tests |

## Priority

1. Payment authorization/cancellation correctness.
2. Ledger immutability and settlement/reconciliation.
3. Concurrency/idempotency.
4. Tests and measurable proof.
5. Async events.
6. Docker polish.
