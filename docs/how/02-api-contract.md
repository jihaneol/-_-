# API Contract

## Implemented API

### Legacy Create Coupon Order

`POST /api/admin/coupon-orders`

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

## Current Admin Commerce Coupon MVP API

The following endpoints define the current admin MVP for member, product, inventory, order payment, stamp coupon issuance, and full refund. These routes are exposed by `admin-api`.

### Dashboard

- `GET /api/admin/dashboard/summary`: main page counters for active members, products, orders, paid orders, refunded orders, and issued stamp coupons.

Success:

```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "memberCount": 3,
    "productCount": 5,
    "orderCount": 7,
    "paidOrderCount": 4,
    "refundedOrderCount": 1,
    "issuedCouponCount": 9
  }
}
```

### Members

- `POST /api/admin/members`
- `GET /api/admin/members`
- `GET /api/admin/members/{memberId}`
- `PATCH /api/admin/members/{memberId}`
- `DELETE /api/admin/members/{memberId}`: soft delete only.

### Products

- `POST /api/admin/products`
- `GET /api/admin/products`
- `GET /api/admin/products/{productId}`
- `PATCH /api/admin/products/{productId}`
- `DELETE /api/admin/products/{productId}`: soft delete only.

### Inventory

- `POST /api/admin/products/{productId}/inventory`
- `GET /api/admin/products/{productId}/inventory`
- `POST /api/admin/products/{productId}/inventory/increase`
- `POST /api/admin/products/{productId}/inventory/decrease`

### Orders

- `POST /api/admin/orders`
- `GET /api/admin/orders`
- `GET /api/admin/orders/{orderId}`
- `POST /api/admin/orders/{orderId}/cancel`: allowed only before payment.
- `POST /api/admin/orders/{orderId}/pay`: authorizes payment, deducts inventory, and issues stamp coupons.
- `POST /api/admin/orders/{orderId}/refund`: full refund only. Partial refund is rejected. Issued coupons are voided with reversal history.

Pay order request:

```json
{
  "idempotencyKey": "pay-20260619-0001"
}
```

Pay order success:

```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "orderId": "1",
    "paymentId": "1",
    "orderStatus": "PAID",
    "paymentStatus": "AUTHORIZED",
    "paidAmount": 12000,
    "issuedCouponCount": 2
  }
}
```

### Coupons

- `GET /api/admin/members/{memberId}/coupons`: issued stamp coupon records.
- `GET /api/admin/members/{memberId}/coupon-histories`
- `GET /api/admin/orders/{orderId}/coupon-histories`

## Current Shop API

Shop routes expose customer workflows only. They must not expose product creation, inventory adjustment, full member listing, dashboard, or operational refund endpoints.

- `POST /api/shop/members`: demo member signup until authentication exists.
- `GET /api/shop/products`: sale product catalog.
- `GET /api/shop/products/{productId}`: sale product detail.
- `POST /api/shop/orders`: create a customer order.
- `GET /api/shop/orders/{orderId}`: customer order detail by order id.
- `POST /api/shop/orders/{orderId}/pay`: pay a customer order and issue stamp coupons.
- `GET /api/shop/members/{memberId}/coupons`: customer coupon wallet.
- `GET /api/shop/members/{memberId}/coupon-histories`: customer coupon history.

## Planned Shop Coupon Exchange

- `POST /api/shop/members/{memberId}/coupons/exchange`: exchange ten issued stamp coupons for one product.

Shop coupon exchange marks coupons as `EXCHANGED`, appends coupon history, and deducts product inventory. Whether exchange creates an order record is still a planning decision.

Exchange request:

```json
{
  "productId": 1,
  "quantity": 1
}
```

Exchange success:

```json
{
  "code": "SUCCESS",
  "message": "요청이 성공했습니다.",
  "data": {
    "memberId": 1,
    "productId": 1,
    "exchangedCouponCount": 10,
    "remainingIssuedCouponCount": 0
  }
}
```

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
- 이미 환불된 결제입니다.
- 금액은 0보다 커야 합니다.
- 가맹점을 찾을 수 없습니다.
- 쿠폰 수량은 1개 이상이어야 합니다.
- 재고가 부족합니다.
- 결제 완료된 주문은 주문 취소할 수 없습니다.
- 결제 전 주문은 환불할 수 없습니다.
- 부분 환불은 지원하지 않습니다.
- 사용 가능한 쿠폰이 부족합니다.
- 발급 상태 쿠폰만 교환할 수 있습니다.
- 판매 중인 상품만 교환할 수 있습니다.
