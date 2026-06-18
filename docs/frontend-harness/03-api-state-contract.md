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
payments.list(filters)
payments.detail(paymentId)
settlements.daily(date)
reconciliation.daily(date)
dashboard.summary(date)
```

## Mutations

| Mutation | Invalidates |
|---|---|
| authorizePayment | `payments.list`, `dashboard.summary` |
| cancelPayment | `payments.list`, `payments.detail`, `dashboard.summary` |
| runDailySettlement | `settlements.daily`, `dashboard.summary` |
| runDailyReconciliation | `reconciliation.daily`, `dashboard.summary` |

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

- Authorize payment request.
- Cancel payment request.
- Settlement run date.
- Reconciliation run date.

## Optimistic Updates

Avoid optimistic updates for money-moving actions. Prefer refetch after success so the UI reflects persisted backend state.
