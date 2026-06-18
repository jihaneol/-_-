# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Backend project scaffold | Required before API/domain implementation | `docs/harness`, `work/03-active-work.md` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Payment authorization | First core transactional workflow | backend scaffold |
| Frontend project scaffold | Required before admin UI implementation | API contract |

## Later

| Feature | Reason to defer |
|---|---|
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
