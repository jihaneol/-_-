# Phase 007: Admin Shop Frontend App Split

This phase separates the existing React frontend into explicit admin and shop applications.

## Goal

Split the current single frontend runtime into admin and shop apps so operator workflows and customer workflows can evolve separately while sharing only intentional UI, API, and type utilities.

## Docs Read

- `docs/operations/01-feature-candidates.md`
- `docs/what/02-roadmap.md`
- `docs/what/04-screen-map.md`
- `docs/how/04-frontend-architecture.md`
- `docs/how/05-api-state-contract.md`
- `docs/how/06-ui-test-strategy.md`
- `rules/frontend-rule.md`

## Scope

- Introduce a frontend app structure for admin and shop runtimes.
- Keep admin screens out of the shop app bundle.
- Keep shop screens out of the admin app bundle.
- Split API clients or query keys into `admin` and `shop` namespaces.
- Preserve existing admin UI behavior.
- Carry forward customer shop spike behavior only after it matches the approved shop API contract.

## Out Of Scope

- Authentication and authorization.
- Full design system extraction.
- Backend API implementation.
- Coupon redemption behavior changes beyond frontend routing and API namespace usage.

## Files To Touch

- `frontend/package.json`
- `frontend/vite.config.ts`
- `frontend/src`
- `frontend/apps`
- `frontend/packages`
- `docs/what/04-screen-map.md`
- `docs/how/04-frontend-architecture.md`
- `docs/how/05-api-state-contract.md`
- `docs/how/06-ui-test-strategy.md`

## Test First

- Add or update UI tests that render the admin app and shop app separately.
- First failing test: shop app does not render admin navigation.
- Validation commands:
  - `npm --prefix frontend test -- --run`
  - `npm --prefix frontend run build`

## Implementation Steps

- [x] Choose and document the frontend app folder shape.
- [x] Move current operator pages into the admin app.
- [x] Move approved customer pages into the shop app.
- [x] Split route entrypoints and Vite build scripts.
- [x] Split API clients/query keys into admin and shop namespaces.
- [x] Update MSW handlers and UI tests.
- [x] Run frontend validation.

## Done Criteria

- [x] Admin app renders operator navigation and workflows.
- [x] Shop app renders customer shopping workflows only.
- [x] Admin and shop apps can be tested independently.
- [x] Admin and shop apps can be built from documented npm scripts.
- [x] Shared frontend code is intentional and not a dumping ground.

## Validation

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `npm --prefix frontend run build:admin`
- `npm --prefix frontend run build:shop`

## Review Focus

- Whether app boundaries match backend API boundaries.
- Whether customer UI avoids operational/admin controls.
- Whether shared code remains small and purposeful.
