# Work Inbox

Raw ideas, feature requests, questions, and possible improvements go here first.

Do not implement directly from this file. Shape promising items in `01-feature-candidates.md` first.

## Intake Template

```md
## YYYY-MM-DD - Idea Title

Source:
Problem:
User / operator:
Expected value:
Rough idea:
Unknowns:
```

## Inbox

### 2026-06-19 - Admin and shop runtime split

Source: user planning
Problem: The frontend and backend should not keep mixing operator workflows and customer shopping workflows in one runtime surface.
User / operator: operator, customer, developer, future portfolio reviewer
Expected value: make the project boundary clearer by separating admin and shop apps in both frontend and backend.
Rough idea: split the backend HTTP runtime boundary into `modules/admin-api` and `modules/shop-api`, and split the frontend into admin and shop apps while keeping domain/application/infra shared.
Unknowns: exact frontend workspace shape, whether existing `bootstrap` is renamed or replaced in phases, and how much compatibility to keep for existing `/api/*` routes.

### 2026-06-19 - Customer shop signup and coupon redemption

Source: user request
Problem: The frontend lacks user-facing signup, post-purchase coupon count visibility, and a way to buy goods with coupons.
User / operator: customer, developer, future portfolio reviewer
Expected value: turns the admin-oriented commerce MVP into a demonstrable customer shopping flow.
Rough idea: add a customer shop page that can create a member, select products, pay an order, show usable stamp count, and redeem ten stamps for one product.
Unknowns: whether redemption should create a separate order record, whether coupon redemption should support arbitrary products, and whether redemption inventory deduction is required.

### 2026-06-19 - Order payment coupon accrual flow

Source: user planning
Problem: Need a simple commerce flow where a paid order grants a coupon accrual/reward history.
User / operator: developer, future portfolio reviewer
Expected value: expands the payment service into a transaction-heavy order/reward scenario while keeping the first version small.
Rough idea: model member, product, inventory, order, payment, coupon, and immutable history around one successful order payment flow.
Unknowns: exact coupon accrual rule, whether payment reuses the existing authorization flow, cancellation/refund behavior, and whether inventory is reserved before or during payment.

### 2026-06-18 - Initial project setup

Source: project planning  
Problem: Need a controlled place to collect new features before changing development scope.  
User / operator: developer, reviewer, future portfolio reader  
Expected value: avoid scope drift and make implementation decisions traceable.  
Rough idea: use lane `workflow/*/phases/` folders as the source of truth for new feature planning.  
Unknowns: none yet.
