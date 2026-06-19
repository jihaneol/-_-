# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Payment idempotency lookup | Prevent duplicate payment creation before ledger work | `workflow/phases/phase-001-idempotency-lookup.md` |
| Payment ledger domain | Add immutable history record model after idempotency lookup | `workflow/phases/phase-002-payment-ledger-domain.md` |
| Ledger append port | Connect authorization flow to append-only ledger records | `workflow/phases/phase-003-ledger-append-port.md` |
| Coupon order duplicate flow | Prevent duplicate external side effects in the first API workflow | `workflow/phases/phase-004-coupon-order-duplicate-flow.md` |
| Commerce order coupon MVP | Add member/product/inventory/order CRUD, stamp coupon issuance, full refund, and soft delete policy | `workflow/phases/phase-005-commerce-order-coupon-mvp.md` |
| Commerce admin frontend | Add React admin UI for the commerce order coupon MVP | `workflow/phases/phase-006-commerce-admin-frontend.md` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Payment cancellation | First corrective workflow after authorization | idempotency and ledger phases |
| Frontend project scaffold | Required before admin UI implementation | backend scaffold merged or API contract stable enough for MSW |

## Later

| Feature | Reason to defer |
|---|---|
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
| Coupon coffee exchange | Add after ten-stamp issuance and history are stable |
| Partial refund | Deferred because refund allocation and coupon reversal rules complicate MVP |
