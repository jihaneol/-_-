# Project Scope

## MVP

- Merchant registration seed data.
- Payment authorization API.
- Full cancellation API.
- Immutable payment ledger.
- Daily settlement batch per merchant.
- Reconciliation report that detects ledger/settlement mismatch.
- Idempotency key handling for duplicate requests.
- MySQL persistence.
- Docker Compose local environment.
- DDD aggregate and value object model.
- Hexagonal package structure with ports and adapters.
- CQRS split between command use cases and QueryDSL-based query use cases.
- Behavior-style unit/application tests.
- MockK for mocked outbound ports.
- Integration tests.
- Minimum commerce CRUD for member, product, inventory, and order.
- Stamp coupon issuance after successful order payment.
- One issued stamp coupon per `5000 KRW` paid amount.
- Full refund for paid orders. Partial refund is excluded.
- Soft delete for user-facing member/product/order delete actions.
- Immutable coupon issuance and refund history.

## Stretch

- Kafka or RabbitMQ payment event publishing.
- Transactional outbox.
- Failed event retry and dead-letter handling.
- Partial cancellation.
- Partial refund.
- Coupon coffee exchange/redemption.
- k6 or Gatling load test.

## Out of Scope

- Real card network or VAN integration.
- User-facing frontend.
- Real authentication/authorization beyond simple API keys.
- Kubernetes and cloud deployment before core tests are complete.
