# Week Plan Harness

## Day 1

- Confirm scope and route/runtime boundaries.
- Keep admin/shop split in docs and phase files.
- Ensure branch/hook rules are active.

## Day 2

- Implement or migrate `admin-api` runtime.
- Add admin context and route tests.

## Day 3

- Implement or migrate `shop-api` runtime.
- Prove shop runtime does not expose admin-only APIs.

## Day 4

- Split frontend admin and shop apps.
- Preserve admin workflow tests.

## Day 5

- Align shop app with `/api/shop/**` contract.
- Keep customer coupon behavior narrow.

## Day 6

- Run backend and frontend validations.
- Add reviewer passes for API boundary and frontend boundary.
- Run the full-stack feature gate before reporting the admin/shop split as complete.

## Day 7

- Polish README and portfolio proof notes.
- Confirm local stack command, validation commands, and browser checks are recorded.

## Current Completion Evidence

- Backend application and API tests cover payment idempotency, refund, coupon exchange, coupon consistency, and shop boundary behavior.
- Frontend tests cover admin navigation, shop navigation, pages 05-12, purchase, payment, and coupon wallet refresh.
- README now includes the one-command local stack, workflows, hard parts, and validation commands.
- Record final verification and remaining risks.

## Completion Loop Plan

### 1st Loop - Customer Storefront Shape

- Status: completed.
- Frontend phase: `workflow/frontend/archive/2026-06-20/phase-010-shop-ecommerce-ux-pass.md`.
- Proof: customer-visible Figma tabs are hidden; header, home, catalog, cart preview, checkout, and my page are ecommerce-shaped.
- Validation: frontend tests, build, impeccable gate, and desktop/mobile browser pass.

### 2nd Loop - API-Owned Product Commerce Metadata

- Status: completed.
- Frontend phase: `workflow/frontend/archive/2026-06-20/phase-011-shop-product-commerce-metadata.md`.
- Backend: product list/detail responses include `couponAccrualCount` and `exchangeEligible`.
- Frontend: shop UI reads those fields instead of recalculating policy in components.
- Validation: shop API contract test, application/admin API tests, frontend test/build, impeccable gate, API curl, and desktop/mobile browser check.

### 3rd Loop - Coffee Kiosk Ordering UX

- Status: completed.
- Frontend phase: `workflow/frontend/archive/2026-06-20/phase-012-coffee-kiosk-ordering-ux.md`.
- Backend: no new route; checkout remains backed by order creation, payment authorization, and coupon wallet invalidation.
- Frontend: coffee kiosk references inform category-first menu, step rail, local drink options, pickup summary, and coupon/payment summary.
- Validation: RTL kiosk flow, frontend build, impeccable gate, dev-server health check, and desktop/mobile browser check.

### 4th Loop - Coupon Wallet and Exchange Readiness

- Status: completed.
- Frontend phase: `workflow/frontend/archive/2026-06-20/phase-013-coupon-wallet-exchange-readiness-ui.md`.
- Backend: wallet, exchange, and consistency report stay aligned with immutable histories.
- Frontend: customer wallet and admin exchange/consistency screens use the same status vocabulary.
- Validation: frontend RTL tests, production build, impeccable gate, and desktop/mobile browser proof passed.

### Final Loop - Portfolio Proof

- Status: completed.
- Frontend phase: `workflow/frontend/archive/2026-06-20/phase-014-frontend-portfolio-proof.md`.
- Proof: local stack command, validation commands, browser checks, README, and harness logs are complete.
- Result: no active frontend phase remains.
