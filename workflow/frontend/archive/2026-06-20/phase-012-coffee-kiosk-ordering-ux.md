# Phase 012: Coffee Kiosk Ordering UX

This phase applies coffee kiosk and cafe mobile-ordering patterns to the customer shop.

## Goal

Make the shop feel closer to a cafe kiosk/mobile order flow with fast category choice, clear order steps, local drink options, pickup payment, and coupon visibility.

## Docs Read

- `work/01-feature-candidates.md`
- `docs/frontend-harness/02-screen-map.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/frontend-harness/04-ui-test-strategy.md`
- Starbucks app reference: `https://www.starbucks.com/rewards/mobile-apps/`
- Starbucks Korea kiosk report: `https://www.yna.co.kr/view/AKR20251113131700030`
- MegaMGC Coffee app reference: `https://apps.apple.com/us/app/%EB%A9%94%EA%B0%80mgc%EC%BB%A4%ED%94%BC/id1473428031`
- Coffee kiosk case study: `https://topping.io/portfolio/kiosk/beauty-coffee-kiosk-platform`
- Kiosk UI guidance: `https://brunch.co.kr/%40rladbtls003/18`

## Scope

- Add kiosk utility bar for order mode, language chips, large-text/help controls, member, and coupon count.
- Add order-step rail for menu selection, options/cart, pickup payment, and coupon check.
- Add cafe category rail and order ticket to catalog.
- Add local drink option controls to cart/detail.
- Change checkout language from delivery to pickup payment.
- Keep existing shop APIs as the authoritative state.

## Out Of Scope

- Persisted categories.
- Saved menu/quick order backend.
- Real pickup scheduling.
- Translation.
- Kiosk hardware accessibility controls.
- Menu option persistence.
- Payment-provider integration.

## Files To Touch

- `frontend/apps/shop/ShopApp.tsx`
- `frontend/src/app/styles/global.css`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/*`
- `work/*`

## Test First

- Update RTL shop flow to assert kiosk heading, kiosk mode, category rail, option controls, pickup checkout copy, payment success, and wallet refresh.
- First expected failure: old generic ecommerce heading and checkout copy remain.

## Implementation Steps

- [x] Add kiosk utility bar.
- [x] Add order-step rail.
- [x] Add cafe category layout and order ticket.
- [x] Add local drink options.
- [x] Reword checkout into pickup payment.
- [x] Update tests and docs.
- [x] Run frontend validation and browser checks.

## Done Criteria

- [x] Kiosk home heading renders.
- [x] Kiosk mode and order-step rail render.
- [x] Catalog shows cafe categories and order ticket.
- [x] Cart/detail expose local drink option controls.
- [x] Checkout uses pickup payment language.
- [x] Payment-to-wallet flow still passes.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] Impeccable gate passes.
- [x] Desktop/mobile browser checks show no horizontal overflow.

## Validation

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
- Local front/shop/admin health checks.
- Desktop/mobile browser checks for kiosk home, catalog, and cart.

## Review Focus

- Whether kiosk affordances are clearly local/frontend-only where backend does not persist them.
- Whether the customer can identify the next order action quickly.
- Whether the UI avoids over-decoration and remains task-focused.
