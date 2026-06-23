# Phase 15: Paginated Query UI

## Goal

Update admin and shop list screens to consume paginated query responses without fetching unbounded lists.

## Docs Read

- `docs/how/05-api-state-contract.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/harness/04-api-contract.md`
- `rules/frontend-rule.md`
- `work/03-active-work.md`

## Scope

- Add shared `PageResponse<T>` frontend type.
- Update API client methods for coupon and coupon-history page responses first.
- Include `page`, `size`, `sort`, and selected member/filter state in TanStack Query keys.
- Add compact pager controls to admin coupon and history tables.
- Keep current shop flow working while reading paginated product/coupon/history contracts as backend phases land.

## Out Of Scope

- Infinite scroll.
- Search backend.
- Product category persistence.
- Export/download UX.
- Redesigning unrelated shop screens.

## Files To Touch

- `frontend/src/entities/commerce/types.ts`
- `frontend/src/entities/commerce/api.ts`
- `frontend/src/pages/members/MembersPage.tsx`
- `frontend/src/apps/shop/ShopApp.tsx`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/03-api-state-contract.md`

## Test First

- Update MSW fixtures to return `{ items, page, size, totalElements, totalPages, hasNext }`.
- Add RTL assertions for next/previous disabled states and page reset after member selection changes.

## Implementation Steps

- [x] Add `PageResponse<T>` and paginated query parameter helpers.
- [x] Update coupon/history API methods and query keys.
- [x] Add admin pager UI for coupon and history tables.
- [x] Update shop coupon consumers to avoid unbounded list assumptions.
- [x] Run frontend tests/build.

## Done Criteria

- [x] Frontend no longer calls coupon/history APIs as unbounded arrays.
- [x] Query keys include pagination state.
- [x] Admin list UI exposes page navigation.
- [x] Existing purchase, coupon wallet, and exchange flows still pass.
- [x] Frontend tests and build pass.

## Validation

`npm --prefix frontend test -- --run`
`npm --prefix frontend run build`

Result: both passed on 2026-06-23.

## Review Focus

Frontend reviewer: verify pagination state, loading/empty behavior, query invalidation, and no optimistic updates for money-moving flows.
