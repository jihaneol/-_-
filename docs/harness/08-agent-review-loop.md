# Agent Review Loop Harness

Use reviewers as independent checks after milestones.

## Domain Reviewer

Run after payment/refund/coupon exchange changes.

Focus:

- aggregate boundaries,
- payment and coupon state transitions,
- immutable history,
- refund and exchange edge cases.

## Database Reviewer

Run after schema/index or query changes.

Focus:

- unique constraints,
- indexes,
- transaction boundaries,
- query growth risks.

## Concurrency Reviewer

Run after idempotency, locking, or inventory changes.

Focus:

- duplicate requests,
- race conditions,
- retry behavior,
- crash consistency.

## Frontend Reviewer

Run after admin/shop app split.

Focus:

- whether customer UI leaks admin operations,
- loading/error/success states,
- route and API namespace correctness.

## Full-Stack Feature Gate

Run after a backend phase completes and again before reporting final completion for any feature that also has frontend work.

Focus:

- whether the backend API contract is reflected in the frontend API client and state keys,
- whether the frontend phase is still pending after backend validation,
- whether `python3 scripts/execute.py feature gate <feature>` passes,
- whether the final report describes the whole feature, not only the last lane.

## Portfolio Reviewer

Run before final polish.

Focus:

- whether docs, tests, and README prove the claims,
- whether tradeoffs are clear,
- whether any feature is overclaimed.
