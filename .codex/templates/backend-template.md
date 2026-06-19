# Backend Harness Template

Replace placeholders before copying into `harness/backend`.

## Placeholders

| Placeholder | Meaning |
|---|---|
| `{target_role}` | Hiring target or portfolio target |
| `{domain}` | Business domain |
| `{core_action}` | Main state-changing workflow |
| `{corrective_action}` | Reversal or correction workflow |
| `{history_record}` | Immutable audit/history record |
| `{batch_report}` | Batch or summary output |
| `{consistency_check}` | Reconciliation or data consistency check |
| `{async_event}` | Domain event |
| `{root_package}` | Kotlin root package |

## Required Backend Harness Files

- `00-target-job.md`: target role and extracted capabilities.
- `01-capability-map.md`: requirement-to-artifact mapping.
- `02-project-scope.md`: MVP, stretch, and out-of-scope.
- `03-domain-model.md`: bounded contexts, aggregates, value objects, invariants.
- `04-api-contract.md`: endpoints, examples, and errors.
- `05-architecture.md`: DDD and hexagonal architecture.
- `06-test-strategy.md`: Behavior-style tests, MockK, integration, concurrency.
- `07-week-plan.md`: day-by-day plan.
- `08-agent-review-loop.md`: reviewer timing and prompts.
- `09-dev-log.md`: daily implementation notes.

## Minimum Evidence

- `{core_action}` API.
- `{corrective_action}` API.
- Immutable `{history_record}`.
- `{batch_report}` workflow.
- `{consistency_check}` report.
- Duplicate-request or concurrency test.
- MockK application test.
- Integration test with MySQL/Testcontainers.
