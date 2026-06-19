# Phase 010: Shop Ecommerce UX Pass

This phase turns the customer shop from a design-page demo into a normal ecommerce storefront.

## Goal

Make the shop feel product-led and customer-facing while preserving the current transaction and coupon backend contract.

## Docs Read

- `docs/frontend-harness/02-screen-map.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/frontend-harness/04-ui-test-strategy.md`
- `work/01-feature-candidates.md`
- `work/03-active-work.md`
- `.agents/skills/impeccable/SKILL.md`
- `.agents/skills/impeccable/reference/product.md`

## Scope

- Hide visible Figma page-number tabs from customer navigation.
- Rework header around home, catalog, benefits, my page, cart, search, and account actions.
- Make home product-led.
- Add ecommerce catalog toolbar and local cart/order preview.
- Preserve checkout and coupon wallet behavior.

## Out Of Scope

- Persistent cart API.
- Search backend.
- Category persistence.
- Product image management.
- Reviews, delivery persistence, discounts, and customer self-exchange.

## Files To Touch

- `frontend/apps/shop/ShopApp.tsx`
- `frontend/src/app/styles/global.css`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/*`
- `work/*`

## Test First

- Update RTL test to assert ecommerce navigation, no visible design tabs, cart preview, checkout, and wallet refresh.
- First expected failure: old shop heading or visible Figma tabs remain.

## Implementation Steps

- [x] Hide visible customer Figma tabs.
- [x] Rework header and home storefront.
- [x] Add product-led catalog and local cart preview.
- [x] Update checkout path assertions.
- [x] Run frontend and browser validation.

## Done Criteria

- [x] Customer-visible design page tabs are gone.
- [x] Header, home, catalog, cart preview, checkout, and my page read as ecommerce flow.
- [x] Existing payment and coupon wallet flow still passes.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] Impeccable gate passes.
- [x] Desktop/mobile browser check has no horizontal overflow.

## Validation

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
- Desktop/mobile browser check for home, catalog, and cart.

## Review Focus

- Whether the shop is customer-facing rather than a design verification page.
- Whether local cart affordances avoid overclaiming persistent cart support.
- Whether coupon benefits remain supporting information, not the whole page.
