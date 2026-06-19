# Screen Map

## Routes

| Route | Purpose |
|---|---|
| `/` | Main operation overview |
| `/members` | Member creation, member list, member coupon inspection |
| `/products` | Product creation, inventory creation, inventory increase |
| `/orders-payments` | Order creation, payment, cancellation, full refund |

## Main Page

- Summary counters from `GET /api/dashboard/summary`.
- Recent orders with status badges.
- Paid, refunded, and issued stamp coupon counters.

## Member Page

- Member creation form.
- Member list.
- Member selector for coupon inspection.
- Issued stamp coupon table.
- Coupon history table.
- Stamp progress toward ten stamps.

## Product Page

- Product and inventory creation form.
- Inventory increase form.
- Product table with current inventory.

## Order/Payment Page

- Order creation form.
- Order payment form with idempotency key.
- Order table with status badges.
- Pre-payment order cancellation.
- Full refund action for paid orders.

## Required UI States

- Loading skeleton or compact spinner.
- Empty state with next action.
- API validation error.
- Server error with retry.
- Success feedback after mutation.
- Disabled submit while request is pending.
