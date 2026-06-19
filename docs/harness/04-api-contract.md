# API Contract Harness

Detailed route contracts live in `docs/how/02-api-contract.md`.

## Current Runtime

The HTTP runtime is split into `admin-api` and `shop-api`.

### Admin API

Namespace: `/api/admin/**`

Responsibilities:

- dashboard summary,
- member/product/inventory administration,
- order inspection and cancellation,
- payment/refund operation,
- coupon and history inspection.
- coupon exchange corrective operation.
- coupon consistency reporting.

Admin API must not contain customer-specific page state or cart-only behavior.

### Shop API

Namespace: `/api/shop/**`

Responsibilities:

- demo member signup until authentication exists,
- sale product catalog,
- customer order creation,
- order payment,
- customer coupon wallet,
- customer-safe coupon history summary.

Shop API must not expose product creation, inventory adjustment, full member list, dashboard, or operational refund APIs.

## Product Catalog Route

```http
GET /api/shop/products
GET /api/shop/products/{productId}
GET /api/admin/products
GET /api/admin/products/{productId}
```

Product responses include customer-safe commerce metadata so the frontend does not duplicate coupon policy math.

Response item:

```json
{
  "id": 1,
  "name": "Americano",
  "price": 12000,
  "saleStatus": "ON_SALE",
  "couponAccrualCount": 2,
  "exchangeEligible": false
}
```

- `couponAccrualCount`: coupons issued by a successful payment for one unit at the current 5,000 KRW accrual unit.
- `exchangeEligible`: true only for on-sale 5,000 KRW products that can be used as coupon exchange rewards.

## Shop Coupon Wallet Route

```http
GET /api/shop/members/{memberId}/coupon-wallet
```

Use this route for the customer-facing coupon card and my-page summary.

Response:

```json
{
  "code": "SUCCESS",
  "data": {
    "memberId": 3,
    "issuedCouponCount": 7,
    "exchangedCouponCount": 10,
    "voidedCouponCount": 0,
    "totalCouponCount": 17,
    "exchangeableSetCount": 0,
    "remainingToNextExchange": 3,
    "recentHistories": []
  }
}
```

The response is a customer-safe summary. It does not expose consistency report rows or admin-only mismatch diagnostics.

## Migration Rule

New routes must be classified as admin or shop before implementation. Ambiguous routes stay out of scope until clarified.

## Core Payment Route

```http
POST /api/admin/orders/{orderId}/pay
POST /api/shop/orders/{orderId}/pay
```

Request:

```json
{
  "idempotencyKey": "pay-20260620-0001"
}
```

Response:

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 7,
    "paymentId": 12,
    "orderStatus": "PAID",
    "paymentStatus": "AUTHORIZED",
    "paidAmount": 12000,
    "issuedCouponCount": 2
  }
}
```

The same idempotency key for the same order returns the existing payment result without re-deducting inventory or re-issuing coupons.

## Corrective Refund Route

```http
POST /api/admin/orders/{orderId}/refund
```

Response:

```json
{
  "code": "SUCCESS",
  "data": {
    "orderId": 7,
    "paymentId": 12,
    "orderStatus": "REFUNDED",
    "paymentStatus": "REFUNDED",
    "voidedCouponCount": 2
  }
}
```

## Coupon Exchange Admin Route

```http
POST /api/admin/members/{memberId}/coupon-exchanges
```

Use this route for operator-driven approval of a 10-stamp coupon exchange.

Request:

```json
{
  "productId": 8
}
```

Response:

```json
{
  "code": "SUCCESS",
  "data": {
    "memberId": 3,
    "productId": 8,
    "productName": "Americano",
    "exchangedCouponCount": 10,
    "remainingIssuedCouponCount": 0,
    "exchangedCouponIds": [1,2,3,4,5,6,7,8,9,10]
  }
}
```

Error cases:

- `400`: member does not exist.
- `400`: product does not exist.
- `400`: product is not on sale.
- `400`: product price is not 5,000 KRW.
- `400`: inventory does not exist or is insufficient.
- `400`: member has fewer than 10 issued coupons.

## Coupon Consistency Report Route

```http
GET /api/admin/coupon-consistency
```

Use this route for operator inspection after issue, refund, and exchange flows.

Response:

```json
{
  "code": "SUCCESS",
  "data": {
    "consistent": true,
    "totalCouponCount": 12,
    "totalIssueHistoryCount": 12,
    "totalVoidHistoryCount": 1,
    "totalExchangeHistoryCount": 10,
    "memberRows": [
      {
        "memberId": 3,
        "issuedCouponCount": 1,
        "voidedCouponCount": 1,
        "exchangedCouponCount": 10,
        "issueHistoryCount": 12,
        "voidHistoryCount": 1,
        "exchangeHistoryCount": 10,
        "exchangeableSetCount": 0,
        "remainingToNextExchange": 9,
        "consistent": true
      }
    ],
    "orderRows": [
      {
        "orderId": 7,
        "memberId": 3,
        "issuedCouponCount": 1,
        "voidedCouponCount": 1,
        "exchangedCouponCount": 10,
        "issueHistoryCount": 12,
        "voidHistoryCount": 1,
        "exchangeHistoryCount": 10,
        "consistent": true
      }
    ]
  }
}
```
