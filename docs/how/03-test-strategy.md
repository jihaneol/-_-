# Test Strategy

## Test Stack

- Use Kotest `BehaviorSpec` or equivalent Behavior-style tests for readable scenarios.
- Use MockK for mocked outbound ports in application use case tests.
- Use Spring Boot integration tests for adapters and full API behavior.
- Use Testcontainers with MySQL when persistence behavior matters.

## Domain Tests

Write Behavior-style tests in the `domain` module without Spring.

- Given an authorized payment, when cancellation is requested, then status becomes `CANCELLED` and a cancellation event is recorded.
- Given a cancelled payment, when cancellation is requested again, then the domain rejects it.
- Given ledger rows for a merchant and date, when settlement is calculated, then the expected amount is produced.
- Given settlement and ledger mismatch, when reconciliation runs, then mismatch rows are classified.

## Application Use Case Tests

Use MockK for outbound ports in the `application` module.

- Mock `LoadPaymentPort`, `SavePaymentPort`, `AppendLedgerPort`, and `SaveOutboxEventPort`.
- Verify transaction-facing orchestration decisions at the use case boundary.
- Do not mock the domain aggregate.
- Test command use cases separately from query use cases.
- Query use case tests verify query port calls and projection contracts.

## Integration Tests

- Keep web slice tests in the `bootstrap` module.
- Keep scheduled/batch adapter tests in the `batch` module.
- Keep JPA/QueryDSL adapter tests in the `infra` module.
- Keep external-system/message adapter tests in the `external` module.
- Keep full Spring Boot context tests in the `bootstrap` module.
- Test QueryDSL read adapters in the `infra` module when query shape, joins, filtering, sorting, or pagination matters.
- Authorize payment persists payment and ledger atomically.
- Duplicate idempotency key returns the existing payment.
- Cancel payment appends cancellation ledger row.
- Settlement batch creates merchant summaries.
- Reconciliation detects missing settlement rows.

## Concurrency Tests

- Send concurrent authorization requests with the same idempotency key.
- Assert only one payment and one authorization ledger row exist.
- Keep this test as portfolio evidence because it proves the race condition defense.

## Optional Load Test

- Run a short k6/Gatling test against authorization API.
- Capture p95 latency and error rate.
- Document bottlenecks and indexes added.
