# Project Scope Harness

## MVP

- Payment authorization with idempotency.
- Payment cancellation or refund as the corrective workflow.
- Member, product, inventory, order, coupon, and coupon history commerce model.
- Paid order issues one stamp coupon per `5000 KRW`.
- Full refund voids issued coupons and appends history.
- Coupon exchange consumes ten issued coupons for one 5,000 KRW product and appends history.
- Coupon consistency report detects state/history mismatch by member/order.
- Admin UI for running and inspecting core workflows.
- Shop UI for customer purchase, coupon earning, and coupon wallet inspection.

## Current Extension

- Split HTTP runtime into `admin-api` and `shop-api`.
- Split frontend into admin and shop apps.
- Keep shared domain/application/infra modules.
- Add customer shop signup, product purchase, coupon wallet, and Figma-inspired shop pages 05-12.

## Out Of Scope

- Real card network or VAN integration.
- Real authentication/authorization in the first split.
- Separate databases per runtime.
- Partial refund.
- Partial coupon payment.
- Coupon exchange cancellation.
- Dedicated payment ledger table beyond coupon histories.
- Daily settlement batch and full settlement reconciliation tables.
- Kubernetes/cloud deployment before local correctness evidence is complete.

## Scope Rule

Prefer one finished, tested transactional slice over broad shallow screens. Any new customer feature must first enter `docs/operations`, then roadmap, then a backend or frontend phase.
