# Decision Log

## 2026-06-25

- Decision: Remove generic `commerce` package grouping from backend source.
- Reason: `domain` already identifies the domain layer, and `commerce` became a broad umbrella that hid the actual business owner of each file.
- Decision: Group backend code directly by business domain.
- Reason: `order`, `product`, `inventory`, `member`, `coupon`, `outbox`, and `payment` are the concepts developers search for and modify.
- Decision: Rename `CommerceOrder` to `Order` and `OrderLine` to `OrderItem`.
- Reason: Package names now provide context; redundant prefixes and line/item ambiguity made the order model harder to read.
- Decision: Remove empty settlement/reconciliation packages.
- Reason: Placeholder packages imply implemented domains that do not exist yet.
- Decision: Replace the payment path's long order `SELECT FOR UPDATE` with a conditional order status update.
- Reason: Duplicate PG callbacks and payment retries still need single-winner semantics, but the order row should not be locked while the request performs the whole payment/coupon/outbox transaction tail.
- Decision: Keep coupon issuance synchronous for the conditional-update slice.
- Reason: Moving coupon issuance after commit changes the API contract and retry model; the first slice should isolate the order lock bottleneck.

## 2026-06-20

- Decision: Treat coupon exchange as an admin corrective workflow, not a customer shop action.
- Reason: Existing shop flow issues stamp coupons after payment; exchange changes operational state and must be controlled by admin.
- Decision: Use pessimistic locking for coupon exchange.
- Reason: Two admin actions against the same coupon must not both append successful exchange history.
- Decision: Model the Figma exchange approval as ten coupon state transitions plus one inventory deduction, without adding a new exchange-order table in this iteration.
- Reason: The current schema already has durable coupon histories and inventory locking. A dedicated exchange order table is useful later, but not required to prove the transactional workflow.
- Decision: Treat coupon consistency reporting as the current reporting/reconciliation proof for this coupon-exchange scope.
- Reason: Full settlement/reconciliation tables are a later payment-ledger feature. For the current scope, the correctness question is whether coupon state matches issue, void, and exchange histories.
- Decision: Implement Figma pages 05-08 as the preceding customer journey screens while Figma MCP is rate-limited.
- Reason: The 09-12 design metadata was available, but 05-08 could not be re-read. The safest implementation is a coherent customer flow that feeds 09-12 and is clearly marked in the screen-map document.
- Decision: Add `scripts/local-stack.sh` as the one-command local setup entry.
- Reason: README previously referenced removed `bootstrap` runtime and did not match the admin/shop split.
- Decision: Treat the first shop UX improvement as frontend-only.
- Reason: The transactional proof already exists in `/api/shop/**`. A persistent cart/search/category backend would expand scope beyond the current payment/coupon harness slice.
- Decision: Hide Figma page tabs from the customer-facing shop.
- Reason: Page numbers are useful for design verification but make the storefront look like an internal demo.
- Decision: Treat coffee kiosk research as a frontend-only UX loop.
- Reason: The current backend already proves product listing, order creation, payment authorization, and coupon wallet refresh. Real categories, drink options, quick-order storage, pickup scheduling, multilingual content, and hardware accessibility are larger backend/product features and remain out of scope for this pass.

## 2026-06-24

- Decision: Plan Kafka through transactional outbox instead of publishing directly from `OrderPaymentFacade`.
- Reason: Order/payment database state and broker delivery must not diverge when one side succeeds and the other fails.
- Decision: Keep coupon issuance synchronous for the first Kafka slice.
- Reason: The current portfolio proof depends on payment response, inventory deduction, coupon issuance, and coupon history being correct in one transaction. Moving coupon issuance async is a later step after idempotent consumers and operational retry visibility are proven.
- Decision: Use Kafka first for post-payment projections/audit, not customer-facing behavior.
- Reason: This gives a meaningful traffic/reliability challenge without changing shop/admin API contracts or weakening existing correctness tests.
