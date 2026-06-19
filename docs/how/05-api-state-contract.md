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
commerce.members
commerce.products
commerce.orders
commerce.inventory(productId)
commerce.coupons(memberId)
commerce.histories(memberId)
```

## Mutations

| Mutation | Invalidates |
|---|---|
| createMember | `commerce.members` |
| createProduct | `commerce.products` |
| createInventory | `commerce.inventory(productId)` |
| createOrder | `commerce.orders` |
| payOrder | `commerce.orders`, `commerce.coupons(memberId)`, `commerce.histories(memberId)` |
| refundOrder | `commerce.orders`, `commerce.coupons(memberId)`, `commerce.histories(memberId)` |

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

## Optimistic Updates

Avoid optimistic updates for money-moving actions. Prefer refetch after success so the UI reflects persisted backend state.
