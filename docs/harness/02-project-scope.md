# Project Scope Harness

## MVP

- Payment authorization with idempotency.
- Payment cancellation or refund as the corrective workflow.
- Immutable payment ledger.
- Member, product, inventory, order, coupon, and coupon history commerce model.
- Paid order issues one stamp coupon per `5000 KRW`.
- Full refund voids issued coupons and appends history.
- Daily settlement batch.
- Reconciliation report for mismatch detection.
- Admin UI for running and inspecting core workflows.

## Current Extension

- Split HTTP runtime into `admin-api` and `shop-api`.
- Split frontend into admin and shop apps.
- Keep shared domain/application/infra modules.
- Add customer shop signup, product purchase, coupon wallet, and coupon exchange only after boundaries are approved.

## Out Of Scope

- Real card network or VAN integration.
- Real authentication/authorization in the first split.
- Separate databases per runtime.
- Partial refund.
- Partial coupon payment.
- Coupon exchange cancellation.
- Kubernetes/cloud deployment before local correctness evidence is complete.

## Scope Rule

Prefer one finished, tested transactional slice over broad shallow screens. Any new customer feature must first enter `docs/operations`, then roadmap, then a backend or frontend phase.
