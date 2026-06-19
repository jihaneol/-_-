# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Payment idempotency lookup | Prevent duplicate payment creation before ledger work | `harness/phases/phase-001-idempotency-lookup.md` |
| Payment ledger domain | Add immutable history record model after idempotency lookup | `harness/phases/phase-002-payment-ledger-domain.md` |
| Ledger append port | Connect authorization flow to append-only ledger records | `harness/phases/phase-003-ledger-append-port.md` |
| Coupon order duplicate flow | Prevent duplicate external side effects in the first API workflow | `harness/phases/phase-004-coupon-order-duplicate-flow.md` |

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
