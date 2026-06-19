# Phase 009: Shop Figma Pages 05-12

This phase implements the customer-facing shop screens inspired by the Figma coupon exchange shopping mall design.

## Goal

Expose a customer-safe purchase and coupon wallet journey across pages 05-12 while preserving the admin/shop API boundary.

## Docs Read

- `docs/frontend-harness/02-screen-map.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/frontend-harness/04-ui-test-strategy.md`
- `work/03-active-work.md`
- Figma design context for `쇼핑몰 쿠폰 교환 관리자 디자인`

## Scope

- Add customer shop views for program intro, coupon guide, catalog, order preview, home, my page, detail, and checkout.
- Use `GET /api/shop/members/{memberId}/coupon-wallet` for customer-safe coupon counts and recent history.
- Keep shop screens on `/api/shop/**`.
- Preserve purchase-to-wallet invalidation after payment.

## Out Of Scope

- Customer self-exchange.
- Persistent cart.
- Search/category backend.
- Delivery persistence.
- Authentication.

## Files To Touch

- `frontend/apps/shop/ShopApp.tsx`
- `frontend/src/entities/commerce/api.ts`
- `frontend/src/entities/commerce/types.ts`
- `frontend/src/app/styles/global.css`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/*`
- `work/*`

## Test First

- Update the split-app RTL test to cover shop navigation, purchase, payment, and wallet refresh.
- First expected failure: shop app cannot render pages 05-12 or wallet state.

## Implementation Steps

- [x] Add shop page state for pages 05-12.
- [x] Add coupon wallet query wiring.
- [x] Add home, my page, detail, checkout, catalog, guide, program, and order preview views.
- [x] Update shop test fixture and assertions.
- [x] Run frontend validation and browser checks.

## Done Criteria

- [x] Shop pages 05-12 render.
- [x] Shop purchase flow pays an order and refreshes coupon wallet.
- [x] Shop does not expose admin navigation or admin-only APIs.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] Browser desktop/mobile checks show no horizontal overflow.

## Validation

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
- Desktop/mobile browser check for shop pages and overflow.

## Review Focus

- Whether customer screens avoid admin leakage.
- Whether coupon state is understandable without exposing operator diagnostics.
- Whether Figma-inspired screens remain task-focused rather than decorative.
