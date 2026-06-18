# API Contract

## Implemented API

### Create Coupon Order

`POST /api/coupon-orders`

This is the first concrete payment scenario. It creates a coupon order, approves payment through a mock external payment port, and accrues coupons after payment approval.

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
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "orderId": "order-0001",
    "paymentId": "1",
    "paymentStatus": "AUTHORIZED",
    "paymentStatusLabel": "승인 완료",
    "amount": 10000,
    "currency": "KRW",
    "couponIds": [
      "coupon_1",
      "coupon_2"
    ]
  }
}
```

Implementation notes:

- coupon unit amount: `5000 KRW`.
- External payment is represented by `ExternalPaymentPort`.
- The mock external payment adapter waits for `300ms`.
- Coupon accrual is represented by `AccrueCouponPort`.
- Payment persistence uses a JPA adapter backed by the `payments` table.
- Coupon accrual is still an in-memory adapter.
- Durable ledger records are next scope.

## Planned API

The following endpoints are target contracts for upcoming work and are not implemented in the current runtime.

### Authorize Payment

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

### Cancel Payment

`POST /api/payments/{paymentId}/cancel`

```json
{
  "reason": "customer_requested"
}
```

### Merchant Payments

`GET /api/merchants/{merchantId}/payments?from=2026-06-18&to=2026-06-18`

### Run Settlement

`POST /api/settlements/daily?date=2026-06-18`

### Run Reconciliation

`POST /api/reconciliation/daily?date=2026-06-18`

## Error Cases

- 같은 중복 요청 방지 키로 다른 요청 본문이 들어오면 거절한다.
- 결제를 찾을 수 없습니다.
- 이미 취소된 결제입니다.
- 금액은 0보다 커야 합니다.
- 가맹점을 찾을 수 없습니다.
- 쿠폰 수량은 1개 이상이어야 합니다.
