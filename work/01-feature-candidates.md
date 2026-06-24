# Feature Candidates

## Coupon Exchange Admin

- Value: Operators can correct the customer coupon lifecycle by exchanging issued stamp coupons and inspecting the immutable history record.
- Scope: Add an admin approval action that exchanges exactly ten `ISSUED` coupons for one 5,000 KRW product, deducts inventory, persists `EXCHANGED` state, appends ten immutable coupon history rows, exposes API and React admin UI.
- Risks: Without write locks, two operators could exchange the same issued coupons or deduct the same inventory concurrently. The command flow must lock inventory and the ten selected coupons.
- Excluded: Customer self-service redemption, partial coupon payment, exchange cancellation, exchange order numbering, and settlement accounting.
- API impact: `POST /api/admin/members/{memberId}/coupon-exchanges`.
- UI impact: Add an operator coupon exchange panel to the admin member/coupon surface and expose the consistency report.
- Tests: Domain transition test, application orchestration test, controller mapping test, Testcontainers integration path for payment/refund, and frontend MSW validation.

## Customer Coupon Wallet And Shop Pages

- Value: Customers can understand coupon earning, buy products, and verify their coupon wallet without leaking admin operations.
- Scope: Add `/api/shop/**` coupon wallet summary, React shop views for Figma-inspired pages 05-12, purchase-to-wallet invalidation, and customer-safe recent history.
- Risks: Shop screens may accidentally expose admin-only refund, inventory, or consistency diagnostics. Query keys and API namespaces must stay separate.
- Excluded: Authentication, real cart persistence, customer self-exchange, discounts, delivery persistence, and payment-provider integration.
- API impact: `GET /api/shop/members/{memberId}/coupon-wallet`, existing shop member/product/order/payment routes.
- UI impact: Add 05 program, 06 coupon guide, 07 catalog, 08 order preview, 09 main, 10 my page, 11 detail, 12 checkout.
- Tests: Frontend tab/navigation test, purchase coupon count test, shop runtime boundary test, and desktop/mobile browser overflow check.

## Portfolio Harness Polish

- Value: A reviewer can understand the hard parts, local setup, validation commands, and tradeoffs without reading the entire chat.
- Scope: Align README, `work/`, `docs/harness`, and `docs/frontend-harness` with implemented behavior and validation evidence.
- Risks: Overclaiming unimplemented settlement or ledger behavior. Documents must distinguish current proof from later targets.
- Excluded: Building settlement batch, Kafka/RabbitMQ, Kubernetes, and real authentication in this scope.
- API impact: none.
- UI impact: none.
- Tests: documentation review plus existing backend/frontend gates.

## Paginated Query CQRS

- Value: Operator and shop list screens remain stable as members, products, orders, coupons, and histories grow beyond demo data.
- Scope: Convert unbounded list query endpoints to `{Feature}PageQuery`/`{Feature}PageResult`, implement QueryDSL-backed query ports/adapters, return `{Feature}PageResponse` objects with `items` and page metadata, and update frontend query state.
- Risks: Migrating all lists at once can break UI flows and tests. Start with coupon/coupon-history lists, then members/products/orders.
- Excluded: cursor-based infinite scroll, server-side full-text search, export/download APIs, and replacing command persistence adapters.
- API impact: collection routes add `page`, `size`, `sort`; response `data` becomes `{ items, page, size, totalElements, totalPages, hasNext }`.
- UI impact: admin tables and shop catalog keep page state, render pager/load-more controls, and include page/filter values in TanStack Query keys.
- Tests: controller contract tests for page shape, QueryDSL adapter integration tests for limit/offset/count/sort, frontend MSW tests for pager behavior.

## Kafka Transactional Outbox Eventing

- Situation: A promotion or lunch-time cafe rush drives many concurrent payment requests into the shop API. Inventory/payment/coupon correctness still needs strong DB transaction guarantees, but dashboard projection, audit, notification, and settlement-prep work should not lengthen the payment request path.
- Value: Prove backend reliability and latency control under payment traffic spikes by decoupling post-payment work from the request path without losing events or duplicating side effects.
- Scope: Add an `outbox_event` table, write `OrderPaid` and `OrderRefunded` events in the same transaction as order/payment state changes, publish pending outbox rows to Kafka from a batch/scheduled publisher, and add idempotent consumers for read-side projections or operational audit records.
- Performance experiment: compare a baseline that updates an operational projection/audit row synchronously during payment with an outbox version that only appends the outbox row in the payment transaction. Use the same concurrent payment load and compare p95/p99 latency, throughput, DB lock wait symptoms, outbox pending count, projection lag, and consumer replay safety.
- Risks: Moving coupon issuance itself to async too early can weaken the current strong payment/coupon correctness proof. Start with observable side effects such as admin dashboard projection, audit/event log, or notification stub before moving core coupon issuance.
- Excluded: exactly-once Kafka semantics, distributed transactions between DB and Kafka, real PG integration, real email/SMS, Kubernetes, Schema Registry, and customer-facing UI for broker internals.
- API impact: no customer API change in the first slice; optionally add admin-only outbox/event delivery inspection after the backend proof exists.
- UI impact: none initially; later admin dashboard can show projection lag, pending outbox count, failed delivery count, and last published event time.
- Tests: application test that outbox rows are saved atomically with payment/refund, publisher integration test with Kafka/Testcontainers, consumer idempotency test, duplicate-event replay test, and load test comparing synchronous vs outbox request latency.

## Shop Ecommerce UX Pass

- Value: The customer-facing shop should feel like a normal ecommerce storefront, not a coupon-feature demo page.
- Scope: Hide design/debug page tabs from customers, restructure the header around catalog/search/cart/account, make the home page product-led, improve catalog/detail/cart/checkout affordances, and keep coupon benefits as a supporting loyalty layer.
- Risks: Adding fake catalog behavior can overclaim backend support. The first pass must reuse the existing `/api/shop/products`, order, payment, and coupon wallet APIs.
- Excluded: persistent cart API, search backend, product image upload, category persistence, reviews, delivery persistence, discounts, and customer self-exchange.
- API impact: none for this pass.
- UI impact: shop app layout, home, catalog, cart/order preview, detail, checkout, and my page.
- Tests: update RTL shop flow, run frontend build, run design detector, and browser-check desktop/mobile overflow.

## Coffee Kiosk Ordering UX Pass

- Value: The customer-facing shop should feel closer to a cafe kiosk/mobile order flow: fast category choice, clear order steps, simple options, and a visible cart/payment summary.
- References:
  - Starbucks app: order ahead, menu exploration, and rewards signup. https://www.starbucks.com/rewards/mobile-apps/
  - Starbucks Korea kiosk reports: multilingual kiosk and accessibility considerations. https://www.yna.co.kr/view/AKR20251113131700030
  - MegaMGC Coffee app notes: quick order, pickup reservation, group order, and saved menu usability. https://apps.apple.com/us/app/%EB%A9%94%EA%B0%80mgc%EC%BB%A4%ED%94%BC/id1473428031
  - Coffee kiosk case study: image-led menu navigation, step-by-step custom options, smart cart, upsell recommendation, and membership/payment integration. https://topping.io/portfolio/kiosk/beauty-coffee-kiosk-platform
  - General kiosk UI guidance: reduce unnecessary choices and prioritize order completion under queue pressure. https://brunch.co.kr/%40rladbtls003/18
- Scope: Rework shop home/catalog/cart/checkout presentation with kiosk-style step rail, language/accessibility controls, cafe category rail, static drink-option controls, pickup summary, and coupon integration.
- Risks: Overclaiming real menu categories, drink options, quick-order storage, pickup reservation, or multilingual behavior. These stay frontend affordances unless backed by API later.
- Excluded: persistent cart, saved favorites API, menu option persistence, real pickup scheduling, multilingual translation, kiosk hardware controls, payment-provider integration, and product image management.
- API impact: none in this pass; continue using `/api/shop/products`, order creation, payment, and coupon wallet.
- UI impact: shop header, home hero, catalog, cart/order preview, checkout, and tests.
- Tests: update RTL shop flow for kiosk heading, category/step UI, drink options, pickup checkout copy, frontend build, design detector, and browser overflow check.
