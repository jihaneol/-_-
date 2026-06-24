# Inbox

## 2026-06-20

- Request: Build a shopping mall coupon exchange administrator flow from the Figma design context, then update the current frontend and backend API to support it.
- Figma status: a node-specific Figma URL was not provided in chat yet, so implementation starts from the existing product design system and domain model. Apply visual adjustments after the Figma frame URL or screenshot is available.
- Request: Apply the Figma shopping mall screens too, including customer-facing shop planning, frontend UI, and backend API support where needed.
- Figma status: shop frames are in the same file. Metadata confirms main shop, coupon-centered my page, product detail, and checkout concepts. Additional design-context calls are rate-limited, so implementation uses the extracted frame structure and current design tokens.
- Request: Create all related planning and keep working with `payment-service-harness` until feature and design work for the current coupon exchange/shop scope is complete.
- Completion interpretation: presentable scope is paid-order coupon issuance, full refund, 10-coupon admin exchange, coupon consistency reporting, customer coupon wallet, and Figma-inspired shop pages 05-12.
- Request: Current shop frontend feels weak. Plan and implement improvements based on common ecommerce patterns while keeping the payment/coupon harness scope narrow.
- Request: Research Starbucks kiosk, Mega Coffee kiosk/app, and general coffee shop ordering pages, then plan and implement frontend improvements through the harness.
- Research notes: coffee ordering references emphasize category-first menu discovery, step-by-step options, quick order, pickup reservation, membership/coupon integration, language/accessibility support, and a persistent cart/payment summary.

## 2026-06-23

- Request: Replace unbounded list-style query endpoints with CQRS QueryDSL pagination. Collection responses such as member coupons and coupon histories must not assume "all" fits in memory or UI state.
- Planning note: backend query use cases should accept page/size/sort query models, infra should use QueryDSL projections/page results, and frontend TanStack Query keys/UI controls should include pagination state.

## 2026-06-24

- Request: Consider whether Kafka is worth adding as a backend performance/reliability challenge, then start planning it.
- Planning note: Kafka should not replace the current synchronous payment correctness path immediately. The first useful slice is transactional outbox plus Kafka publishing for post-payment events, with idempotent consumers and retryable delivery evidence.
