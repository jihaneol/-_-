# Architecture Harness

Detailed architecture lives in `docs/how/00-architecture.md`.

## Module Target

```text
modules/domain
modules/application
modules/infra
modules/external
modules/batch
modules/admin-api
modules/shop-api
```

## Dependency Rule

```text
admin-api/shop-api/batch
  -> application
  -> domain

infra/external
  -> application
  -> domain
```

`domain` has no project module dependency. `application` owns use cases and ports. `infra` owns persistence and QueryDSL. `external` owns external payment/message adapters.

## Runtime Split

- `admin-api`: operator HTTP runtime and admin OpenAPI docs.
- `shop-api`: customer HTTP runtime and shop OpenAPI docs.
- Both use shared application use cases.
- Shared web error response can be duplicated initially and extracted later only if duplication becomes costly.

## Security Shape

- `Member` owns required username, encoded password, and `ADMIN`/`USER` role.
- `application` defines `PasswordHashPort`; API runtimes provide BCrypt adapters.
- Admin and shop runtimes each register a stateless Spring Security filter chain.
- JWT filters parse bearer tokens, reload the active member by username, and require the token role to match the current member role.
- Admin routes require `ROLE_ADMIN`; shop routes require `ROLE_USER` after signup/login.

## Reliability Shape

- Use unique constraints and transaction boundaries for idempotency.
- Use append-only records for ledgers and coupon histories.
- Prefer transactional outbox before broker publishing.
- Keep failed event delivery visible and retryable.

## Current Transaction Boundaries

- `OrderPaymentFacade.payOrder` locks the order, checks idempotency, locks inventory, saves payment, marks the order paid, issues coupons, and appends issue histories in one transaction.
- `OrderPaymentFacade.refundOrder` locks the order/inventory, refunds the payment, restores inventory, voids coupons, and appends void histories in one transaction.
- `CouponExchangeService.approveCouponExchange` locks the exchange product inventory and ten issued coupons, deducts one inventory item, exchanges the coupons, and appends exchange histories in one transaction.

## Current Persistence And Locking

- `Payment` has a unique idempotency key constraint.
- `JpaCommerceLockAdapter` uses `PESSIMISTIC_WRITE` for orders, inventory, individual coupons, and exchange coupon selection.
- Admin and shop runtimes share the same application services so customer and operator paths do not fork business rules.

## Deferred Reliability Work

- Transactional outbox and broker retry UI.
- Dedicated payment ledger table.
- Daily settlement and full reconciliation batch.

## Kafka Outbox Target

Traffic problem:

- During a cafe rush or promotion, many shop payment requests arrive at once.
- The payment transaction still owns oversell prevention, idempotency, coupon issuance, and coupon history.
- Non-critical post-payment work such as operational projection, audit/event log, notification stub, and settlement-prep events can be processed asynchronously.
- Kafka is introduced to reduce request-path coupling and to prove retry/replay behavior, not to solve inventory locking or payment duplication.

First async slice:

```text
OrderPaymentFacade.payOrder/refundOrder
  -> save order/payment/inventory/coupon state
  -> append OutboxEvent in the same DB transaction
  -> scheduled publisher reads pending events
  -> Kafka topic commerce.order-events.v1
  -> idempotent consumer updates projection or audit table
```

Ownership:

- `application` owns outbox ports and event models.
- `domain` stays broker-free.
- `infra` owns outbox JPA persistence.
- `external` owns Kafka producer/consumer adapters.
- `batch` can own the scheduled outbox publisher inbound adapter if it runs outside the HTTP runtimes.

Initial event types:

- `OrderPaid`: order id, payment id, member id, paid amount, issued coupon count, occurred-at.
- `OrderRefunded`: order id, payment id, member id, voided coupon count, occurred-at.

Failure handling:

- Outbox rows start as `PENDING`.
- Successful publish marks `PUBLISHED`.
- Failed publish increments attempt count and stores the last error.
- Consumers persist processed event ids to make replay safe.
- Kafka delivery is treated as at-least-once; application side effects must be idempotent.

Explicit non-goal for the first slice:

- Do not move coupon issuance to async yet. Coupon issuance remains part of the payment transaction until the eventing path has its own retry, replay, and observability proof.

Performance proof:

```text
Baseline
  payment transaction
  -> synchronous projection/audit update
  -> API response

Outbox
  payment transaction
  -> append outbox event
  -> API response
  -> publisher/consumer update projection/audit later
```

Compare both shapes under the same concurrent payment load. The expected tradeoff is lower payment p95/p99 latency and better downstream failure isolation in exchange for projection lag and outbox operational complexity.
