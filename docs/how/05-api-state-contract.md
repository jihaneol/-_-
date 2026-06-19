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
admin.members
admin.products
admin.orders
admin.inventory(productId)
admin.coupons(memberId)
admin.histories(memberId)

shop.products
shop.coupons(memberId)
shop.histories(memberId)
```

## Admin Mutations

| Mutation | Invalidates |
|---|---|
| createMember | `admin.members`, `admin.summary` |
| createProduct | `admin.products`, `admin.summary` |
| createInventory | `admin.inventory(productId)` |
| increaseInventory | `admin.inventory(productId)` |
| cancelOrder | `admin.orders`, `admin.summary` |
| refundOrder | `admin.orders`, `admin.summary`, `admin.coupons(memberId)`, `admin.histories(memberId)` |

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
- Coupon exchange.

## Optimistic Updates

Avoid optimistic updates for money-moving actions. Prefer refetch after success so the UI reflects persisted backend state.

## Runtime Namespaces

- Admin UI calls only `/api/admin/**`.
- Shop UI calls only `/api/shop/**`.
- `adminCommerceApi` and `adminCommerceKeys` are used by operator pages.
- `shopCommerceApi` and `shopCommerceKeys` are used by the customer shop app.
