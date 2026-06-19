# Phase 013: Coupon Wallet Exchange Readiness UI

This phase is the next active frontend loop for aligning customer coupon wallet state with admin exchange readiness.

## Goal

Make customer wallet progress and admin exchange readiness use the same state vocabulary and provide clear inspection evidence for issue, void, and exchange histories.

## Docs Read

- `work/03-active-work.md`
- `docs/harness/04-api-contract.md`
- `docs/frontend-harness/02-screen-map.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/frontend-harness/04-ui-test-strategy.md`

## Scope

- Refine customer my page coupon wallet state copy and empty/loading states.
- Refine admin member exchange panel state copy to match customer wallet vocabulary.
- Ensure exchange-ready, accumulating, exchanged, and voided states read consistently.
- Keep customer screens from exposing admin consistency diagnostics.
- Add or update RTL assertions for customer wallet and admin exchange/consistency surfaces.

## Out Of Scope

- New backend routes.
- Customer self-exchange.
- Exchange order ledger.
- Authentication and role-based access control.
- Settlement batch.

## Files To Touch

- `frontend/apps/admin/AdminApp.tsx`
- `frontend/apps/shop/ShopApp.tsx`
- `frontend/src/app/styles/global.css`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/*`
- `work/*`

## Test First

- Add assertions for wallet/exchange readiness copy before changing UI copy.
- First expected failure: customer and admin surfaces use inconsistent labels or missing exchange readiness states.

## Implementation Steps

- [x] Audit current customer wallet labels.
- [x] Audit current admin exchange labels.
- [x] Update copy and state blocks.
- [x] Update RTL assertions.
- [x] Run frontend validation and browser checks.

## Done Criteria

- [x] Customer wallet clearly distinguishes accumulating, exchange-ready, exchanged, and voided states.
- [x] Admin exchange panel uses the same user-facing state vocabulary.
- [x] Shop still does not expose admin consistency diagnostics.
- [x] RTL tests pass.
- [x] Frontend build passes.
- [x] Impeccable gate passes.
- [x] Desktop/mobile browser checks pass for touched shop/admin screens.

## Validation

- `npm --prefix frontend test -- --run` - passed on 2026-06-20.
- `npm --prefix frontend run build` - passed on 2026-06-20.
- `bash scripts/hooks/validate_impeccable.sh` - passed on 2026-06-20.
- Browser proof on 2026-06-20:
  - shop desktop home/catalog/cart/my page: no horizontal overflow, no admin diagnostics, no Figma page tabs.
  - shop mobile home/catalog: no horizontal overflow, no admin diagnostics, no Figma page tabs.
  - admin desktop/member and admin mobile/member: no horizontal overflow; shared state vocabulary visible.

## Result

- Shared status labels now render `적립 중`, `교환 가능`, `교환 완료`, and `회수` across the shop wallet and admin coupon tables.
- Admin member exchange summary now exposes the same wallet state vocabulary used by the customer surface.
- Customer shop screens still hide admin-only consistency diagnostics.

## Review Focus

- Whether state vocabulary is consistent across customer and operator surfaces.
- Whether customer-safe boundaries are preserved.
- Whether the UI makes exchange readiness obvious without adding a customer exchange action.
