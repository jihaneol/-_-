# UI Test Strategy Harness

## Test Stack

- Vitest.
- React Testing Library.
- MSW.

## Required Tests

- Admin app renders operator navigation and core screen.
- Shop app renders customer navigation and does not render admin navigation.
- Shop app can open Figma-inspired pages 05-12.
- Order payment flow shows issued coupon result.
- Coupon wallet shows usable issued stamp count.
- Error and loading states are visible for server requests.
- Customer storefront does not expose visible Figma page-number tabs.
- Cart/order preview appears before checkout.
- Coffee kiosk pass renders the kiosk home heading, category rail, order-step rail, drink option controls, and pickup checkout copy.

## Regression Tests

- Shop API handlers must not include admin-only routes.
- Admin and shop query keys must not collide.
- Disabled submit state appears while mutations are pending.
- Browser checks confirm no horizontal overflow on 05-12 at desktop and mobile widths.

## Validation

```bash
npm --prefix frontend test -- --run
npm --prefix frontend run build
```
