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
