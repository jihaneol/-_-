# API Contract

## Authorize Payment

`POST /api/payments/authorize`

```json
{
  "merchantId": "merchant-001",
  "idempotencyKey": "order-20260618-0001",
  "orderId": "order-0001",
  "amount": 15000,
  "currency": "KRW"
}
```

Success:

```json
{
  "paymentId": "pay_...",
  "status": "AUTHORIZED",
  "amount": 15000
}
```

## Cancel Payment

`POST /api/payments/{paymentId}/cancel`

```json
{
  "reason": "customer_requested"
}
```

## Merchant Payments

`GET /api/merchants/{merchantId}/payments?from=2026-06-18&to=2026-06-18`

## Run Settlement

`POST /api/settlements/daily?date=2026-06-18`

## Run Reconciliation

`POST /api/reconciliation/daily?date=2026-06-18`

## Error Cases

- Duplicate idempotency key with different request body.
- Payment not found.
- Payment already cancelled.
- Amount must be positive.
- Merchant not found.
