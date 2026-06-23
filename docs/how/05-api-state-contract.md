# API State Contract

## API Client

Create a small typed API client under `shared/api`.

Responsibilities:

- Base URL configuration.
- JSON request/response handling.
- Error normalization.
- Optional Zod response parsing.

## Query Keys

```text
admin.summary
admin.members(page,size,sort,filters)
admin.products(page,size,sort,filters)
admin.orders(page,size,sort,filters)
admin.couponConsistency
admin.inventory(productId)
admin.coupons(memberId,page,size,sort)
admin.histories(memberId,page,size,sort)

shop.products(page,size,sort,filters)
shop.coupons(memberId,page,size,sort)
shop.histories(memberId,page,size,sort)
```

## Paginated Query State

Collection queries use paginated API responses:

```ts
type PageResponse<T> = {
  items: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  hasNext: boolean
}
```

Frontend behavior:

- Include `page`, `size`, `sort`, and filter values in TanStack Query keys.
- Keep previous page data visible while the next page loads.
- Render empty state only when `items.length === 0` after loading.
- Disable previous/next controls based on `page === 0` and `hasNext`.
- Reset `page` to `0` when filters or selected member change.
- Do not fetch unbounded "all" lists for members, products, orders, coupons, or histories.

## Admin Mutations

| Mutation | Invalidates |
|---|---|
| createMember | `admin.members`, `admin.summary` |
| createProduct | `admin.products`, `admin.summary` |
| createInventory | `admin.inventory(productId)` |
| increaseInventory | `admin.inventory(productId)` |
| cancelOrder | `admin.orders`, `admin.summary` |
| payOrder | `admin.orders`, `admin.summary`, `admin.couponConsistency` |
| refundOrder | `admin.orders`, `admin.summary`, `admin.coupons(memberId)`, `admin.histories(memberId)`, `admin.couponConsistency` |
| approveCouponExchange | `admin.summary`, `admin.coupons(memberId)`, `admin.histories(memberId)`, `admin.couponConsistency` |

## Shop Mutations

| Mutation | Invalidates |
|---|---|
| signupMember | `shop.coupons(memberId)` |
| createOrder | none directly; pay after creation refreshes coupon state |
| payOrder | `shop.orders(memberId)`, `shop.coupons(memberId)`, `shop.histories(memberId)` |
| exchangeCoupons | `shop.coupons(memberId)`, `shop.histories(memberId)`, `shop.product(productId)` |

## Error Model

Normalize backend errors into:

```ts
type ApiError = {
  code: string
  message: string
  fieldErrors?: Record<string, string>
}
```

## Form Validation

Use Zod schemas for:

- Member creation.
- Product and inventory creation.
- Order creation.
- Order payment.
- Coupon exchange approval.

Admin coupon exchange approval calls `POST /api/admin/members/{memberId}/coupon-exchanges` with a selected 5,000 KRW product id. Controls are disabled unless a member is selected, an exchange product is selected, the member has at least ten `ISSUED` coupons, and no approval request is already pending. Do not apply optimistic updates; refetch persisted coupon and history state after success.

Coupon consistency calls `GET /api/admin/coupon-consistency`. The admin UI uses it as a read-only report after issue, refund, and exchange flows, comparing current coupon statuses with immutable coupon history counts by member/order.

## Optimistic Updates

Avoid optimistic updates for money-moving actions. Prefer refetch after success so the UI reflects persisted backend state.

## Runtime Namespaces

- Admin UI calls only `/api/admin/**`.
- Shop UI calls only `/api/shop/**`.
- `adminCommerceApi` and `adminCommerceKeys` are used by operator pages.
- `shopCommerceApi` and `shopCommerceKeys` are used by the customer shop app.
