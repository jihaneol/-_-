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

## Portfolio Reviewer

Run before final polish.

Focus:

- whether docs, tests, and README prove the claims,
- whether tradeoffs are clear,
- whether any feature is overclaimed.
