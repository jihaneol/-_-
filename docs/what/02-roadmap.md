# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Payment idempotency lookup | Prevent duplicate payment creation before ledger work | `workflow/backend/phases/phase-001-idempotency-lookup.md` |
| Payment ledger domain | Add immutable history record model after idempotency lookup | `workflow/backend/phases/phase-002-payment-ledger-domain.md` |
| Ledger append port | Connect authorization flow to append-only ledger records | `workflow/backend/phases/phase-003-ledger-append-port.md` |
| Coupon order duplicate flow | Prevent duplicate external side effects in the first API workflow | `workflow/backend/phases/phase-004-coupon-order-duplicate-flow.md` |
| Commerce order coupon MVP | Add member/product/inventory/order CRUD, stamp coupon issuance, full refund, and soft delete policy | `workflow/backend/phases/phase-005-commerce-order-coupon-mvp.md` |
| Commerce admin frontend | Add React admin UI for the commerce order coupon MVP | `workflow/frontend/archive/2026-06-19/phase-006-commerce-admin-frontend.md` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Admin and shop runtime split | Separate operator and customer surfaces before adding more customer workflows | commerce MVP and admin frontend |
| Customer shop signup and coupon wallet | Add customer-facing purchase and coupon visibility after app/API boundaries are separated | admin/shop runtime split |
| Customer coupon redemption | Let customers use issued coupons without exposing admin APIs | customer shop app and shop API namespace |
| Payment cancellation | First corrective workflow after authorization | idempotency and ledger phases |

## Later

| Feature | Reason to defer |
|---|---|
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
| Partial refund | Deferred because refund allocation and coupon reversal rules complicate MVP |
| Authentication and authorization | Add after admin/shop runtime boundaries are stable |
