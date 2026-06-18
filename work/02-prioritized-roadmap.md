# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Payment idempotency and ledger | Strengthen the first payment workflow with duplicate prevention and immutable records | `docs/harness`, `work/03-active-work.md` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Payment cancellation | First corrective workflow after authorization | payment idempotency and ledger |
| Frontend project scaffold | Required before admin UI implementation | backend scaffold merged or API contract stable enough for MSW |

## Later

| Feature | Reason to defer |
|---|---|
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
