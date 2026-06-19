# Phase 006: Commerce Admin Frontend

This phase adds the operator UI required by the harness for the commerce order coupon MVP.

## Goal

Create a React admin UI where an operator can create member/product/inventory data, create and pay orders, run full refunds, and inspect issued coupon stamps and histories.

## Docs Read

- `docs/what/03-frontend-goal.md`
- `docs/what/04-screen-map.md`
- `docs/how/04-frontend-architecture.md`
- `docs/how/05-api-state-contract.md`
- `docs/how/06-ui-test-strategy.md`

## Scope

- Scaffold `frontend/` with React, TypeScript, Vite, TanStack Query, React Hook Form, Zod, Vitest, React Testing Library, and MSW.
- Build operational screens for commerce dashboard, member/product/inventory setup, order payment, full refund, coupon stamps, and coupon histories.
- Keep UI dense and back-office oriented.
- Add MSW-backed UI tests for one successful paid-order coupon issuance flow.

## Out Of Scope

- Coupon coffee exchange.
- Partial refund.
- Authentication.
- Full design system extraction.

## Files To Touch

- `frontend`
- `docs/what/03-frontend-goal.md`
- `docs/what/04-screen-map.md`
- `docs/how/04-frontend-architecture.md`
- `docs/how/05-api-state-contract.md`
- `docs/how/06-ui-test-strategy.md`

## Test First

- Add a UI test that renders the order workflow and verifies paid amount plus issued coupon count.
- Validation command: `npm test -- --run`

## Implementation Steps

- [x] Scaffold frontend project.
- [x] Add typed API client and query keys.
- [x] Add commerce dashboard and order workflow screen.
- [x] Add coupon history inspection screen.
- [x] Add MSW handlers and UI test.
- [x] Run frontend tests and dev server.

## Done Criteria

- [x] Operator can execute the commerce order coupon MVP workflow from the UI.
- [x] Loading, error, success, and empty states are visible.
- [x] UI tests pass.
- [x] Dev server starts and URL is provided.

## Validation

- `npm test -- --run`
- `npm run build`

## Review Focus

- The UI exposes the important backend states without hiding failure cases.
- The screen is operational, not a marketing page.
