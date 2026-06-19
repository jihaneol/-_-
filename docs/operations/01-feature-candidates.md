# Feature Candidates

Use this file to shape raw ideas into implementable feature candidates.

## Candidate Template

```md
## Feature: Title

Status: draft | review | approved | rejected | parked
Source:
Target user:

### Problem


### Proposed Behavior


### Backend Impact

- API:
- Domain:
- Persistence:
- Async/batch:

### Frontend Impact

- Page:
- Widget:
- Feature:
- State handling:

### Tests

- Domain:
- Application:
- Integration:
- UI:

### Excluded Scope

- 

### Risks

- 

### Decision


```

## Candidates

## Feature: Paid Order Coupon Accrual

Status: approved
Source: 2026-06-19 user planning
Target user: developer, future portfolio reviewer

### Problem

The project needs a small but complete commerce transaction flow where a member orders a product, payment succeeds, inventory decreases, and a coupon/reward history is recorded.

### Proposed Behavior

1. A member places an order for a product quantity.
2. The system validates product availability and inventory.
3. Payment is authorized for the order total.
4. On successful payment, inventory is deducted and coupon issuance records are created.
5. The coupon policy issues one stamp coupon per `5000 KRW` paid amount.
6. Ten issued stamp coupons can later be exchanged for one coffee, but exchange is not part of this MVP.
7. Order, payment, coupon, and history records remain inspectable.
8. A paid order can be fully refunded. Partial refund is not allowed.
9. Full refund voids issued coupon stamps and appends refund reversal history instead of deleting coupons.
10. User-facing delete actions use soft delete. Payment and coupon history remain immutable.

### Minimum CRUD

- Member: create, update profile basics, list/detail, deactivate.
- Product: create, update name/price/sale status, list/detail.
- Inventory: create initial stock, increase/decrease stock, view current stock.
- Order: create order, list/detail orders, cancel before payment, fully refund after payment.
- Payment: pay order, fully refund payment, list/detail payments; no direct update except state transition.
- Coupon: list/detail issued coupon stamps by member; no manual mutation in MVP.
- CouponHistory: list histories by member/order/payment; append-only, no update/delete.

### Backend Impact

- API: minimum CRUD for member/product/inventory/order plus payment execution, full refund, and coupon/history reads.
- Domain: Member, Product, Inventory, Order, Payment, Coupon, CouponHistory.
- Persistence: tables for members, products, inventory, orders, order lines, payments, coupons, coupon histories.
- Async/batch: none for MVP; event/outbox can be added later.

### Frontend Impact

- Page: order/payment execution screen and history inspection screen if frontend is included.
- Widget: member selector, product/inventory summary, payment action, coupon history table.
- Feature: create order, pay order, inspect accrual.
- State handling: loading, validation failure, payment failure, duplicate request handling.

### Tests

- Domain: order total calculation, inventory deduction guard, stamp coupon issuance rule, CRUD state guards, full-refund guard.
- Application: successful order payment creates payment and coupon history; full refund changes order/payment state without partial refund.
- Integration: duplicate payment request does not duplicate coupon issuance.
- UI: happy path and failure states if frontend is included.

### Excluded Scope

- Multi-product cart complexity beyond minimal order lines.
- Coupon exchange/redemption for coffee after ten stamps.
- Manual coupon grant/revoke admin workflow.
- Partial refund.
- External PG/VAN integration.
- Async event broker and outbox.

### Risks

- Scope can grow quickly if product catalog, cart, discount, partial refund, and coupon redemption are included too early.
- Inventory and payment transaction boundaries need one clear policy before implementation.
- Full refund must void issued stamp coupons without deleting issuance history.

### Decision

Approved MVP. Implement one paid-order stamp coupon issuance flow, minimum CRUD, payment-before-cancel, full refund, soft delete, and immutable coupon history before adding partial refund, coffee exchange, eventing, or batch reports.
