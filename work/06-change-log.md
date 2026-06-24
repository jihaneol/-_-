# Change Log

## 2026-06-24

- Planned the Kafka/outbox traffic-spike story around payment p95/p99 improvement, not core payment correctness.
- Added the Before baseline for the Kafka comparison: payment/refund transactions synchronously write `payment_operational_projections`.
- Added schema, domain entity, application repository port, and `OrderPaymentFacade` wiring for the synchronous projection.
- Added application and Testcontainers-backed integration assertions that duplicate idempotent payment retries do not duplicate projection rows.
- Added `load-tests/payment-spike-sync-projection.js` and Docker wrapper `scripts/load-test-payment-before.sh` for the k6 baseline.
- Ran a smoke load test with `VUS=2 DURATION=5s`; result was `payment_latency p95=568.1ms`, `http_req_failed=0.00%`, and `43` completed iterations.
- Verified application behavior test, full Gradle test suite, Testcontainers admin integration test, local stack health, and manual shop payment projection smoke.

## 2026-06-20

- Started coupon exchange admin scope from the current commerce coupon model.
- Added `ISSUED -> EXCHANGED` coupon domain transition and immutable `EXCHANGED` coupon history.
- Added admin API `POST /api/admin/members/{memberId}/coupon-exchanges` to exchange ten issued coupons for one 5,000 KRW product.
- Updated admin frontend member/coupon screen into a coupon exchange manager with product selection, disabled states, success/error feedback, and query invalidation.
- Updated harness and frontend API-state docs for the coupon exchange workflow.
- Verified backend tests, frontend tests, frontend build, UI validation hook, and local server health.
- Added autonomous completion loop rules to the payment-service harness, repository constitution, and workflow guide.
- Added admin coupon consistency report API and member-screen report panel for member/order status-history comparison.
- Added shop coupon wallet API for customer-safe issued/exchanged/voided counts and recent histories.
- Added Figma-inspired shop pages 05-12: program intro, coupon guide, product list, order preview, main, my page, detail, and checkout.
- Added `scripts/local-stack.sh` as the one-command local stack entry for MySQL, admin API, shop API, and frontend.
- Updated README and harness planning docs to align with the completed coupon exchange/shop scope and validation evidence.
- Planned and implemented a shop ecommerce UX pass: customer-visible design tabs removed, header rebuilt around home/catalog/benefits/my page/cart/search, home made product-led, catalog/cart interactions improved, and tests updated for the storefront flow.
- Added explicit 1st, 2nd, 3rd, 4th, and final frontend/backend loops to active work and harness plans.
- Started 2nd loop implementation: API-owned product commerce metadata consumed by the shop UI.
- Completed 2nd loop: product API returns coupon accrual and exchange eligibility metadata, shop UI consumes it, and backend/frontend/browser validations passed.
- Completed 3rd loop coffee kiosk UX: researched Starbucks/MegaMGC/general kiosk patterns, added kiosk utilities, order-step rail, cafe category layout, order ticket, local drink options, pickup checkout copy, and validation evidence.
- Backfilled missing frontend phase files: archived completed phases 009-012 and added active phases 013-014 under `workflow/frontend/phases`.
- Completed 4th loop coupon wallet/exchange readiness: shop wallet and admin exchange/consistency UI now share `적립 중`, `교환 가능`, `교환 완료`, and `회수` status vocabulary.
- Completed final frontend proof loop: frontend tests, build, impeccable gate, local health checks, and desktop/mobile browser checks passed; frontend phase queue is empty after archiving.
