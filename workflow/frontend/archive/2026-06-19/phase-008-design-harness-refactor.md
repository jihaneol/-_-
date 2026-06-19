# Phase 008: Design Harness Refactor

This phase applies the Impeccable design harness to the current admin and shop frontend surfaces.

## Goal

Create product/design context for the frontend and refactor the shared visual system so admin and shop screens feel consistent, task-focused, readable, and detector-clean.

## Docs Read

- `PRODUCT.md`
- `DESIGN.md`
- `docs/how/04-frontend-architecture.md`
- `docs/what/04-screen-map.md`
- `.agents/skills/impeccable/SKILL.md`
- `.agents/skills/impeccable/reference/product.md`

## Scope

- Add root `PRODUCT.md` and `DESIGN.md` for the design harness.
- Refactor global frontend styles into token-driven product UI rules.
- Improve admin navigation, panels, tables, notices, metrics, forms, and shop product/coupon presentation.
- Preserve existing admin/shop app boundaries and API behavior.
- Keep visual changes lightweight and production-oriented.

## Out Of Scope

- Backend API changes.
- New business workflows.
- Authentication and authorization.
- Full design system extraction into separate component packages.
- Replacing the existing product font in this phase.

## Files To Touch

- `PRODUCT.md`
- `DESIGN.md`
- `frontend/apps`
- `frontend/src`
- `workflow/frontend/phases`

## Test First

- Existing split-app tests must keep passing.
- First expected failure if the refactor regresses behavior: admin navigation or shop purchase/coupon flow test fails.
- Validation commands:
  - `npm --prefix frontend test -- --run`
  - `npm --prefix frontend run build`
  - `bash scripts/hooks/validate_impeccable.sh`

## Implementation Steps

- [x] Write product and design context files.
- [x] Refactor global CSS into explicit tokens and states.
- [x] Refine admin shell density, table readability, forms, metrics, and notices.
- [x] Refine shop signup, coupon summary, and product purchase presentation.
- [x] Run tests, build, and Impeccable detector.

## Done Criteria

- [x] Design harness context exists at the project root.
- [x] Admin and shop screens share one coherent visual system.
- [x] Existing split-app behavior tests pass.
- [x] Frontend build passes.
- [x] Impeccable detector passes.

## Validation

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`

## Review Focus

- Whether the result stays product-focused rather than decorative.
- Whether admin/shop boundaries remain visually clear but consistent.
- Whether the visual system is easier to maintain than ad hoc CSS values.
