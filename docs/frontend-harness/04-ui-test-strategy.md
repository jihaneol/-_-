# UI Test Strategy Harness

## Test Stack

- Vitest.
- React Testing Library.
- MSW.

## Required Tests

- Admin app renders operator navigation and core screen.
- Shop app renders customer navigation and does not render admin navigation.
- Order payment flow shows issued coupon result.
- Coupon wallet shows usable issued stamp count.
- Error and loading states are visible for server requests.

## Regression Tests

- Shop API handlers must not include admin-only routes.
- Admin and shop query keys must not collide.
- Disabled submit state appears while mutations are pending.

## Validation

```bash
npm --prefix frontend test -- --run
npm --prefix frontend run build
```
