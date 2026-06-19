# One-Week Plan

## Day 1

- Finalize workflow docs.
- Create Spring Boot project.
- Create DDD/hexagonal multi-module structure.
- Add MySQL Docker Compose.
- Add SQL table/index schema files under `sql/`.

## Day 2

- Implement authorization API.
- Persist payment and ledger.
- Split command use cases from query use cases.
- Add Behavior-style domain tests.
- Add application tests with MockK.

## Day 3

- Implement cancellation API.
- Add status transition tests using Behavior style.
- Add idempotency behavior.

## Day 4

- Add concurrency test.
- Fix locking/unique constraint behavior.
- Document transaction decisions.

## Day 5

- Implement settlement batch.
- Implement reconciliation report.
- Add QueryDSL read adapters for reporting/query screens.
- Add batch/reconciliation tests.

## Day 6

- Add async event or outbox stretch if core is stable.
- Add query indexes and measurement notes.
- Run full test suite.

## Day 7

- Polish README.
- Fill dev log and decision log.
- Run reviewer agents.
- Fix the highest-risk findings.
