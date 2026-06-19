# API State Contract Harness

Detailed state contract lives in `docs/how/05-api-state-contract.md`.

## Admin Query Keys

```text
admin.summary
admin.members
admin.products
admin.orders
admin.couponConsistency
admin.inventory(productId)
admin.coupons(memberId)
admin.histories(memberId)
```

## Shop Query Keys

```text
shop.member(memberId)
shop.products
shop.product(productId)
shop.orders(memberId)
shop.coupons(memberId)
shop.histories(memberId)
```

## Mutation Rules

- Money-moving or inventory-moving actions refetch after success.
- Avoid optimistic updates for payment, refund, inventory, and coupon exchange.
- API errors normalize to `{ code, message, fieldErrors? }`.
- Admin client calls `/api/admin/**`.
- Shop client calls `/api/shop/**`.

## Coupon Exchange Mutation

```text
admin.approveCouponExchange(memberId, productId)
POST /api/admin/members/{memberId}/coupon-exchanges
```

Success invalidates:

- `admin.summary`
- `admin.coupons(memberId)`
- `admin.histories(memberId)`
- `admin.couponConsistency`

Disabled state:

- member is not selected
- exchange product is not selected
- issued coupon count is below 10
- mutation is pending

## Coupon Consistency Query

```text
admin.couponConsistency
GET /api/admin/coupon-consistency
```

The Members screen renders this report after issue, refund, and exchange flows. It is not optimistically updated.
