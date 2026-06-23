# API State Contract Harness

Detailed state contract lives in `docs/how/05-api-state-contract.md`.

## Admin Query Keys

```text
admin.summary
admin.members(page,size,sort,filters)
admin.products(page,size,sort,filters)
admin.orders(page,size,sort,filters)
admin.couponConsistency
admin.inventory(productId)
admin.coupons(memberId,page,size,sort)
admin.histories(memberId,page,size,sort)
```

## Shop Query Keys

```text
shop.member(memberId)
shop.products(page,size,sort,filters)
shop.product(productId)
shop.orders(memberId)
shop.wallet(memberId)
shop.coupons(memberId,page,size,sort)
shop.histories(memberId,page,size,sort)
```

## Paginated List UI Plan

- Replace list queries with `PageResponse<T>` contracts.
- Keep local page state per table/list surface: members, products, orders, member coupons, coupon histories, and shop products.
- Preserve current operational flow by rendering `items` while using metadata for controls.
- Add compact pager controls to admin tables: previous, next, current page, total pages.
- For shop product catalog, use load-more or page controls, but never request an unbounded product list.
- Reset page to `0` when the selected member, search text, status filter, or sort changes.

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
- `admin.coupons(memberId,page,size,sort)` base group
- `admin.histories(memberId,page,size,sort)` base group
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
- `shop.coupons(memberId,page,size,sort)` base group
- `shop.histories(memberId,page,size,sort)` base group

## Product Commerce Metadata

```text
shop.products(page,size,sort)
GET /api/shop/products?page=0&size=20&sort=id,desc
```

Response uses `PageResponse<Product>` with `items`, `page`, `size`, `totalElements`, `totalPages`, and `hasNext`.

Each product item contains:

- `couponAccrualCount`: display value for "구매 시 쿠폰 N장 적립".
- `exchangeEligible`: display value for exchange reward labeling.

Shop components must read these fields from the API contract. They may not recalculate the coupon accrual policy in each component.

## Admin Simple List Queries

```text
admin.members(page,size,sort)
GET /api/admin/members?page=0&size=20&sort=id,desc

admin.products(page,size,sort)
GET /api/admin/products?page=0&size=20&sort=id,desc

admin.orders(page,size,sort)
GET /api/admin/orders?page=0&size=20&sort=id,desc
```

These simple aggregate lists use `PageResponse<T>` and render `items` in tables. UI must not rely on `{ members }`, `{ products }`, `{ orders }`, or top-level arrays.

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
