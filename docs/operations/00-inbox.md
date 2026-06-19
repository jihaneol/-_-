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
