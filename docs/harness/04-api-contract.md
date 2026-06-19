# API Contract Harness

Detailed route contracts live in `docs/how/02-api-contract.md`.

## Current Runtime

The current implementation still has a single Spring Boot HTTP runtime. Existing `/api/*` routes are the migration source.

## Target Runtime

### Admin API

Namespace: `/api/admin/**`

Responsibilities:

- dashboard summary,
- member/product/inventory administration,
- order inspection and cancellation,
- payment/refund operation,
- coupon and history inspection.

Admin API must not contain customer-specific page state or cart-only behavior.

### Shop API

Namespace: `/api/shop/**`

Responsibilities:

- demo member signup until authentication exists,
- sale product catalog,
- customer order creation,
- order payment,
- customer coupon wallet,
- coupon exchange after policy approval.

Shop API must not expose product creation, inventory adjustment, full member list, dashboard, or operational refund APIs.

## Migration Rule

Every route moved from `/api/*` must be classified as admin or shop before implementation. Ambiguous routes stay out of scope until clarified.
