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
shop.wallet(memberId)
shop.coupons(memberId)
shop.histories(memberId)
```

## Mutation Rules

- Money-moving or inventory-moving actions refetch after success.
- Avoid optimistic updates for payment, refund, inventory, and coupon exchange.
- API errors normalize to `{ code, message, fieldErrors? }`.
- Admin client calls `/api/admin/**`.
- Shop client calls `/api/shop/**`.
- Figma page tabs are local UI state only; they do not create additional API routes.

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

## Shop Coupon Wallet Query

```text
shop.wallet(memberId)
GET /api/shop/members/{memberId}/coupon-wallet
```

The shop page uses this customer-safe summary for the coupon card and my-page panel. Purchase success invalidates:

- `shop.wallet(memberId)`
- `shop.coupons(memberId)`
- `shop.histories(memberId)`

## Product Commerce Metadata

```text
shop.products
GET /api/shop/products
```

Each product contains:

- `couponAccrualCount`: display value for "구매 시 쿠폰 N장 적립".
- `exchangeEligible`: display value for exchange reward labeling.

Shop components must read these fields from the API contract. They may not recalculate the coupon accrual policy in each component.

## Shop Page State

```text
ShopView =
  program05
  guide06
  catalog07
  order08
  home
  mypage
  detail
  checkout
```

Only `home`, `guide06`, and purchase flows mutate backend state. Other pages reuse product, member, selected product, and wallet state.

## Ecommerce UX Pass State

- Cart is local UI state for the currently selected product.
- Cart checkout still calls the existing create-order and pay-order mutations.
- Search, filter, category, and sort controls are frontend affordances in this pass and do not call new APIs.
- Customer-visible navigation hides Figma page-number tabs.

## Coffee Kiosk UX State

- Kiosk categories, language chips, accessibility controls, quick-order copy, and drink options are local frontend affordances in this pass.
- Selected product remains the only local cart state.
- Checkout still creates one order line and pays through `/api/shop/orders/{orderId}/pay`.
- Payment success still invalidates the coupon wallet and recent coupon history queries.
