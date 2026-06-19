# Review Skill

Use this skill after a phase is implemented or when the user asks for structural review.

## Review Order

1. Compare the completed changes against the active phase file.
2. Check the relevant `docs/` files for contract drift.
3. Check architecture boundaries and rule files.
4. Check tests and validation output.
5. If review passes, record the acceptance with `python3 scripts/execute.py --lane <backend|frontend> review "note"`.
6. Report findings before summaries.

## Review Focus Areas

- Domain correctness: aggregate boundaries, invariants, idempotency, cancellation, ledger, settlement, reconciliation.
- Database correctness: schema, constraints, indexes, transaction boundaries, persistence adapter ownership.
- Concurrency: duplicate requests, locking, retry behavior, outbox/event guarantees.
- API and UI: operator workflows, error states, stable contracts, no accidental DTO leakage.
- Portfolio readiness: docs explain what was hard, why decisions were made, and how behavior was verified.

## Output Shape

Lead with findings ordered by severity. Use file and line references. If there are no findings, say so and list residual risk or missing verification.
