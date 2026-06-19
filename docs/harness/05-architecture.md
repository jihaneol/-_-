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
