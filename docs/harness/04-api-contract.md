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

## Place Starbucks Coupon Order

`POST /api/starbucks-coupon-orders`

This is the first concrete payment scenario. It simulates a Starbucks coupon order, approves payment through a mock external payment port, and accrues coupons after payment approval.

Request:

```json
{
  "customerId": "customer-001",
  "orderId": "order-0001",
  "idempotencyKey": "order-20260618-0001",
  "quantity": 2
}
```

Success:

```json
{
  "orderId": "order-0001",
  "paymentId": "pay_1",
  "paymentStatus": "AUTHORIZED",
  "amount": 10000,
  "currency": "KRW",
  "couponIds": [
    "starbucks_coupon_1",
    "starbucks_coupon_2"
  ]
}
```

Implementation notes:

- Starbucks coupon unit amount: `5000 KRW`.
- External payment is represented by `ExternalPaymentPort`.
- The mock external payment adapter waits for `300ms`.
- Coupon accrual is represented by `AccrueCouponPort`.
- Current persistence is in-memory; durable persistence and ledger records are next scope.

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
- Starbucks coupon quantity must be positive.
