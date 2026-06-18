# Prioritized Roadmap

Only shaped feature candidates should appear here.

## Now

| Feature | Why now | Development basis |
|---|---|---|
| Project harness setup | Establish planning and implementation control before coding | `docs/harness`, `docs/frontend-harness`, `work/` |

## Next

| Feature | Why next | Depends on |
|---|---|---|
| Backend project scaffold | Required before API/domain implementation | harness approval |
| Frontend project scaffold | Required before admin UI implementation | API contract |

## Later

| Feature | Reason to defer |
|---|---|
| Kafka/RabbitMQ outbox | Add after core payment flow and tests are stable |
| Load testing | Add after API behavior is stable |
