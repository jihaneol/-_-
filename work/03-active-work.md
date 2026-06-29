# Active Work

## Backend Package Cleanup

Remove broad umbrella package folders and keep backend code grouped directly by business domain.

- Domain package target: `domain/{order,product,inventory,member,coupon,outbox,payment}`.
- Application package target: `application/{order,product,inventory,member,coupon,dashboard,outbox,payment}`.
- Infra package target: `infra/{order,inventory,coupon,outbox,payment}`.
- Web package target: API runtime modules use `web/{order,product,inventory,member,coupon,dashboard,payment}` without an extra `commerce` or `shop` folder.
- Naming target: `CommerceOrder -> Order`, `OrderLine -> OrderItem`, `CommerceOrderRepository -> OrderRepository`.
- Cleanup rule: do not keep empty placeholder domains such as settlement or reconciliation until real implementation exists.
- Response rule: controllers use `toApplicationResponse(type = ApplicationResponseType.OK)` and change status by passing `ApplicationResponseType`.

## Coupon Exchange Admin

Build a narrow operational exchange workflow from the Figma admin design:

- Domain: `Coupon.exchange()` only allows `ISSUED -> EXCHANGED`.
- Policy: 10 issued coupons can be exchanged for one 5,000 KRW product.
- Inventory: exchange approval deducts one product inventory item.
- History: `CouponHistory.exchanged(coupon)` records one immutable event per exchanged coupon.
- Application: `CouponExchangeUseCase.approveCouponExchange(memberId, productId)` owns transaction and write locks.
- Infra: load the target inventory and ten issued coupons with `PESSIMISTIC_WRITE`.
- API: `POST /api/admin/members/{memberId}/coupon-exchanges`.
- Frontend: operator selects a member, inspects coupon count/history, selects a 5,000 KRW product, and approves exchange.
- Reporting: operator can inspect coupon consistency by member/order after exchange.
- Completion loop: if API, UI, history, inventory, consistency report, or validation proof is missing, continue with the next smallest tested slice before reporting completion.

Figma dependency:

- Figma design source: `쇼핑몰 쿠폰 교환 관리자 디자인`, admin frames 01-04.

## Shop Coupon Wallet UI

Extend the customer-facing shop from the same Figma file:

- Main shop screen: 82px white header, centered 1296px content, 300px hero band, coupon floating card, and product tiles.
- Figma pages 05-08 are implemented as the preceding customer journey: program intro, coupon guide, product list, and order confirmation.
- Figma pages 09-12 are split into `home`, `mypage`, `detail`, and `checkout` customer views.
- Customer state: signup creates a demo member; purchase creates and pays an order; successful payment refetches the customer coupon wallet.
- Wallet: show issued, exchanged, and voided coupons, a 10-slot stamp board, next-exchange count, and recent coupon history.
- API: `GET /api/shop/members/{memberId}/coupon-wallet` returns customer-safe coupon summary only.
- Boundary: shop screens keep using `/api/shop/**` and must not call admin coupon inspection, consistency, refund, inventory, or dashboard routes.

## Harness Completion Contract

The presentable scope is complete only when these artifacts exist and validate:

- Core transactional workflow: shop/admin order payment issues coupons once under an idempotency key.
- Corrective workflow: admin full refund voids issued coupons; admin exchange approval consumes ten issued coupons and one inventory unit.
- Immutable records: coupon issue, void, and exchange histories are append-only.
- Reporting output: admin dashboard, customer coupon wallet, and coupon consistency report expose operational state.
- Consistency check: `GET /api/admin/coupon-consistency` compares coupon state with issue, void, and exchange histories by member/order.
- Concurrency proof: idempotency uniqueness and pessimistic locks are documented and covered by application/integration tests.
- Frontend proof: admin UI executes/inspects operations; shop UI renders Figma-inspired pages 05-12 without admin leakage.
- Portfolio proof: README and harness docs describe local setup, hard parts, validation commands, tradeoffs, and remaining scope.

## Shop Ecommerce UX Pass

The next implementation slice improves the customer-facing shop while preserving the current backend contract:

- Hide visible Figma/design page tabs from the customer storefront.
- Header: logo, catalog, benefits, my page, cart, search, and account actions.
- Home: product-led hero, benefit strip, membership signup, coupon wallet summary, and recommended product shelf.
- Catalog: ecommerce-style toolbar with category/filter chips and product cards.
- Cart/order preview: local selected product review before checkout; no persistent cart API in this pass.
- Checkout: keep existing order creation/payment/coupon issuance flow.
- Tests: update shop RTL flow to assert ecommerce navigation, cart preview, checkout, and wallet refresh.

## Frontend/Backend Completion Loops

Run the project through explicit loops until the harness done criteria are met. Each loop must update planning docs before code, then finish with backend, frontend, and browser evidence that matches the touched surface.

### 1st Loop - Customer Storefront Shape

- Goal: make the shop feel like a normal ecommerce storefront instead of a Figma page index.
- Frontend: hide design page tabs, rework header, home, catalog, cart preview, checkout entry, and my-page route.
- Backend: no new route; preserve `/api/shop/**` boundary.
- Evidence: RTL shop flow, frontend build, impeccable validation, desktop/mobile browser pass.
- Status: completed.

### 2nd Loop - API-Owned Product Commerce Metadata

- Goal: stop duplicating coupon policy math in the shop UI.
- Backend: product responses expose customer-safe commerce metadata: coupon accrual count and exchange eligibility.
- Frontend: catalog, detail, cart, checkout, and product cards render those fields from the API.
- Tests: shop product route contract test plus RTL fixture update.
- Evidence: backend/API tests, frontend tests/build, impeccable gate, API curl, and desktop/mobile browser quick check passed.
- Status: completed.

### 3rd Loop - Coffee Kiosk Ordering UX

- Goal: make the customer shop feel like a cafe kiosk/mobile order flow while keeping the current backend contract.
- Research basis: Starbucks app order/menu/rewards, Starbucks Korea kiosk language/accessibility reports, MegaMGC quick order/pickup/saved-menu notes, and coffee kiosk case-study patterns for image-led navigation, step options, smart cart, and membership integration.
- Backend: no new API; order creation/payment and coupon wallet remain authoritative.
- Frontend: add order-step rail, language/accessibility utility controls, cafe category rail, kiosk-style menu layout, static drink-option controls, pickup summary, and coupon/payment summary.
- Tests: assert kiosk heading, category/order-step UI, option controls, pickup checkout copy, payment success, and wallet refresh.
- Evidence: frontend RTL test, production build, impeccable gate, dev-server health check, and desktop/mobile browser checks passed.
- Status: completed.

### 4th Loop - Coupon Wallet and Exchange Readiness

- Goal: make customer coupon progress and admin exchange approval inspectable from both sides.
- Backend: keep wallet and consistency report aligned with issue, void, and exchange histories.
- Frontend: my page, admin member screen, exchange panel, and consistency report expose the same state vocabulary.
- Tests: coupon wallet, exchange approval, consistency report, and admin UI exchange flow.
- Status: completed.

### Final Loop - Portfolio Proof

- Goal: make the project presentable from a clean local run.
- Backend/frontend: run the full validation gate and record the result.
- Browser: verify admin and shop on desktop/mobile with no overflow or admin leakage.
- Docs: README, harness dev log, and change log include setup, hard parts, proof commands, and remaining scope.
- Status: completed.

## Backend Phase Cleanup

- Status: completed.
- Removed stale backend phases 001-005 from active execution.
- Archived phase 001 and 004 as superseded by the commerce order payment duplicate-protection flow.
- Archived phase 002 and 003 as deferred payment-ledger backlog.
- Archived phase 005 as completed commerce coupon MVP.
- Obsidian record: `07.Build Logs/card-service/days/2026-06-20-3일차.md`.

## Planned: Paginated Query CQRS

- Problem: collection endpoints currently behave like "all" reads, but member/product/order/coupon/history data can grow without a stable upper bound.
- Backend direction: convert list query use cases to `{Feature}PageQuery` and `{Feature}PageResult`, backed by QueryDSL read adapters in `infra`.
- API direction: collection responses return `data.items` plus `page`, `size`, `totalElements`, `totalPages`, and `hasNext`.
- Frontend direction: TanStack Query keys include page/size/sort/filter state; admin tables and shop catalog render pagination controls and never fetch unbounded lists.
- Rollout: start with coupon and coupon-history lists because they grow fastest through payment/refund/exchange flows, then migrate members/products/orders.

## Planned: Kafka Transactional Outbox Eventing

- Problem: payment traffic should not wait on every downstream concern, but payment, coupon, inventory, and audit correctness cannot rely on best-effort broker publishing.
- Traffic scenario: a cafe promotion sends 100-500 concurrent payment attempts through the shop API. Oversell prevention, payment idempotency, coupon issuance, and coupon history remain synchronous and strongly consistent, while projection/audit/notification-like work is moved behind Kafka.
- Backend direction: keep the current order/payment/coupon transaction authoritative, then append `OutboxEvent` rows for `OrderPaid` and `OrderRefunded` inside the same transaction.
- Publisher direction: a scheduled publisher or batch adapter reads pending outbox rows, publishes to Kafka, marks success, and leaves failed rows visible for retry.
- Consumer direction: consumers record processed event ids before applying side effects, so replay or duplicate Kafka delivery does not duplicate projections.
- First consumer scope: admin operational projection or event audit table. Do not move coupon issuance async until the outbox and idempotent consumer proof is stable.
- Topics: `commerce.order-events.v1` for order/payment lifecycle events; later topics can split by bounded context if volume or ownership requires it.
- Baseline experiment: first add a deliberately synchronous projection/audit update on the payment path and load test it so the performance problem is observable.
- Outbox experiment: replace that synchronous projection/audit update with outbox append plus Kafka consumer and rerun the same load profile.
- Metrics: p50/p95/p99 payment latency, successful payments per second, duplicate payment/coupon count, failed payment count, outbox pending count, publish retry count, projection lag, and consumer duplicate replay count.
- Rollout: local Docker Compose Kafka, outbox table and ports, publisher, one idempotent consumer, retry/failure tests, then k6/Gatling latency comparison.
- Non-goal: Kafka is not used for inventory locking, payment idempotency, or coupon issuance in the first slice.

### Before Baseline Status

- Status: implemented.
- Synchronous baseline: payment/refund transactions now write `payment_operation_records` rows directly.
- Load-test script: `scripts/load-test-payment-before.sh` runs `load-tests/payment-spike-sync-projection.js` through Dockerized k6.
- Validation: application behavior test, full Gradle test suite, Testcontainers admin flow, local stack health, and manual shop payment projection smoke passed on 2026-06-24.
- Smoke measurement: `VUS=2 DURATION=5s scripts/load-test-payment-before.sh` passed with `payment_latency p95=568.1ms`, `http_req_failed=0.00%`, and `43` completed iterations.
- Full baseline still needed: run the same script with a realistic VUS/duration such as `VUS=50 DURATION=30s`.

## Completed: Order Payment Conditional Update

- Problem: `payOrder` begins with order `PESSIMISTIC_WRITE` and holds the order row lock through payment, inventory, coupon, history, and outbox writes.
- Direction: replaced the payment path's order lock lookup with plain order lookup plus a conditional `CREATED -> PAID` update.
- Success rule: conditional order update returns `1` for the winning payment request; `0` means the order was already paid/cancelled/deleted and the transaction rolls back.
- Coupon rule: coupon issuance can move to after-commit/outbox later, but it must only run after the payment transaction commits. This slice keeps coupon issuance synchronous to avoid changing the API contract.
- Non-goal: refund and coupon exchange pessimistic locks are not changed in this slice.
- Validation: application behavior test, full Gradle test, and Testcontainers order flow integration test passed.

## Completed: Member Auth And JWT Runtime Filters

- Member identity now requires unique `username` and required `password`; passwords are encoded before persistence.
- `name` and `email` are optional profile fields. Blank `name` generates a nickname; blank `email` is stored as null.
- Member role is `ADMIN` or `USER`.
- Admin runtime exposes `/api/admin/auth/login` and protects `/api/admin/**` with `ROLE_ADMIN`.
- Shop runtime exposes `/api/shop/auth/signup` and `/api/shop/auth/login`, and protects user-specific `/api/shop/**` routes with `ROLE_USER` while keeping product catalog reads public.
- Frontend admin app now requires admin login before rendering operations, and shop signup stores the returned JWT for order/payment/wallet requests.
- Validation: backend module tests, frontend Vitest, frontend production build, and `validate_impeccable.sh` passed on 2026-06-29. The UI hook emitted its existing `frontend/shared` path warning with exit code 0.
