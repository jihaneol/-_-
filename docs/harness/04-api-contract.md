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
- coupon exchange after policy approval.

Shop API must not expose product creation, inventory adjustment, full member list, dashboard, or operational refund APIs.

## Migration Rule

New routes must be classified as admin or shop before implementation. Ambiguous routes stay out of scope until clarified.

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
