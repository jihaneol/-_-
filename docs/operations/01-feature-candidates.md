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

## Feature: Admin And Shop Runtime Split

Status: draft
Source: 2026-06-19 user planning
Target user: operator, customer, developer, future portfolio reviewer

### Problem

The current frontend and backend surfaces are operator-oriented and share one runtime boundary. Customer shopping workflows should not be mixed with admin workflows, and shop APIs should not expose admin capabilities such as product creation, inventory adjustment, full member listing, or operational refunds.

### Proposed Behavior

1. The frontend is split into two apps: an admin app and a shop app.
2. The backend HTTP runtime boundary is split into `modules/admin-api` and `modules/shop-api`.
3. `admin-api` exposes operator workflows under `/api/admin/**`.
4. `shop-api` exposes customer workflows under `/api/shop/**`.
5. `domain`, `application`, `infra`, `external`, and `batch` remain shared modules.
6. API contracts and screen maps document admin and shop responsibilities separately before implementation phases are created.

### Backend Impact

- API: introduce `/api/admin/**` and `/api/shop/**` namespaces.
- Domain: no split. Existing domain models remain shared.
- Persistence: no split. Existing infra adapters remain shared.
- Async/batch: no immediate change.

### Frontend Impact

- Page: separate admin routes from shop routes.
- Widget: shared UI can stay in a shared package only when genuinely reusable.
- Feature: admin flows and shop flows should not import each other's feature modules.
- State handling: admin and shop query keys should be separated.

### Tests

- Domain: none for the runtime split itself.
- Application: none unless use case contracts change.
- Integration: each API runtime should expose only its route namespace.
- UI: admin app smoke test and shop app smoke test.

### Excluded Scope

- Authentication and authorization.
- Separate databases per runtime.
- Separate domain/application modules per runtime.
- Full deployment automation for two services.

### Risks

- Module split can become too large if mixed with customer coupon feature behavior.
- Route migration can break existing frontend tests unless compatibility is planned.
- Shared web configuration and error handling need an explicit owner to avoid duplication.

### Decision

Draft direction agreed: use `admin-api` and `shop-api` as explicit backend runtime modules. Do not implement until roadmap, architecture, API contract, and lane phase files are approved.

## Feature: Customer Shop Signup And Coupon Redemption

Status: draft
Source: 2026-06-19 user request
Target user: customer, developer, future portfolio reviewer

### Problem

The current UI is mostly operator-facing. A customer cannot sign up from the shop app, see how many usable coupon stamps they have after purchase, or use earned coupons to get a product.

### Proposed Behavior

1. A customer can create a demo member from the shop app.
2. A customer can choose themselves, add sale products to a cart, and pay by the existing order payment flow.
3. After payment, the UI refreshes and shows the member's usable issued coupon count.
4. A customer can redeem ten issued coupons for one selected sale product.
5. Redeemed coupons become `EXCHANGED` and immutable coupon history records are appended.
6. Redemption deducts inventory for the selected product.

### Backend Impact

- API: add shop-scoped endpoints under `/api/shop/**`, including coupon exchange.
- Domain: add coupon exchange state transition and history type.
- Persistence: reuse existing coupon and coupon history tables.
- Async/batch: none.

### Frontend Impact

- Page: build shop-app customer-facing signup, shopping, coupon status, and redemption flow.
- Widget: member signup form, selected member coupon summary, cart checkout, coupon exchange action.
- Feature: create member, pay order, inspect coupon count, redeem coupons.
- State handling: validation, loading, success, API errors, and disabled redemption below ten stamps.

### Tests

- Domain: exchange only issued coupons.
- Application: exchange ten issued coupons and records histories.
- Integration: optional follow-up for full HTTP flow.
- UI: signup, paid purchase coupon count, and coupon redemption happy path.

### Excluded Scope

- Partial coupon payment mixed with money payment.
- Product-specific coupon pricing rules.
- Redemption order receipt and cancellation.

### Risks

- Redemption can become a full order/payment workflow if mixed tender or cancellations are added.
- Existing MVP previously deferred exchange, so this feature must stay narrow.

### Decision

Draft only. This feature depends on the admin/shop runtime split decision and must not be implemented until backend and frontend phases are created.

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
