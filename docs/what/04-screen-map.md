# Screen Map

## Routes

| Route | Purpose |
|---|---|
| `/` | Commerce coupon operation dashboard |

## Dashboard

- Member count.
- Product count.
- Order count.
- Usable coupon stamp count.
- Recent operation result or API error.

## Commerce Operations

- Member creation form.
- Product and inventory creation form.
- Order creation form.
- Order payment form with idempotency key.
- Order table with status badges.
- Full refund action for paid orders.

## Coupon Inspection

- Member selector.
- Issued stamp coupon table.
- Coupon history table.
- Stamp progress toward ten stamps.

## Required UI States

- Loading skeleton or compact spinner.
- Empty state with next action.
- API validation error.
- Server error with retry.
- Success feedback after mutation.
- Disabled submit while request is pending.
