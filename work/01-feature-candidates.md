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
