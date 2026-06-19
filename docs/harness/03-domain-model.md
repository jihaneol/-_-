# Domain Model Harness

## Core Aggregates And Records

| Area | Model | Responsibility |
|---|---|---|
| Payment | `Payment` | authorization, cancellation/refund status, idempotency identity |
| Commerce | `Member` | customer identity for MVP commerce flows |
| Commerce | `Product` | sale product and sale status |
| Commerce | `Inventory` | stock increase/decrease guard |
| Commerce | `CommerceOrder` | order lines, total amount, payment status |
| Coupon | `Coupon` | issued, voided, exchanged stamp state |
| Coupon | `CouponHistory` | append-only coupon issuance/reversal/exchange history |
| Reporting | dashboard summary | operational count summary |
| Reconciliation | coupon consistency report | mismatch classification between coupon state and histories |

## Invariants

- Payment idempotency key must not create duplicate external side effects.
- Inventory cannot decrease below zero.
- Paid order coupon count is `paidAmount / 5000`.
- Full refund voids all coupons issued by the order.
- Coupon history is append-only.
- Coupon exchange uses exactly ten `ISSUED` coupons for one 5,000 KRW exchange product.
- Coupon exchange approval deducts one product inventory item in the same transaction.
- Admin and shop runtimes must not fork domain rules.

## Deferred Domain Targets

- Dedicated payment ledger table.
- Daily settlement aggregate.
- Full settlement reconciliation report.
- Exchange order aggregate for separate exchange numbers.

## Boundary Decision

`admin-api` and `shop-api` split only the inbound runtime boundary. Domain and application rules remain shared until there is a proven reason to split them.
