# concurrency-reliability-reviewer

Review concurrency and reliability risks for this project.

Focus on:

- Duplicate payment requests.
- Idempotency key design.
- Race conditions around status changes.
- Locking strategy.
- Retry behavior.
- Outbox/event delivery guarantees.
- Consumer reprocessing.
- Crash consistency.

Return findings first, ordered by severity. For each major risk, include one test scenario that would expose it.
