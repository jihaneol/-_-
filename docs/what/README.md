# What We Build

## Goal

Build a reviewable card payment backend that proves payment correctness, duplicate-request safety, immutable financial history, settlement, and reconciliation.

The portfolio target is a Kakao Pay-style server developer role, so the project prioritizes backend correctness over UI polish.

## Core Features

- Payment authorization API.
- Idempotency handling for duplicate requests.
- Immutable payment ledger.
- Full cancellation flow.
- Daily merchant settlement.
- Reconciliation mismatch detection.
- Operator-facing admin UI after the backend contract is stable.

## MVP Exclusions

- Real VAN or card network integration.
- Real authentication beyond simple API keys.
- Kubernetes or cloud deployment before core tests pass.
- Partial cancellation until full cancellation and ledger behavior are stable.
- Kafka/RabbitMQ outbox until core payment and ledger flows are proven.

## Primary References

- `docs/what/00-target-job.md`
- `docs/what/01-project-scope.md`
- `docs/what/02-roadmap.md`
- `docs/what/03-frontend-goal.md`
- `docs/what/04-screen-map.md`

