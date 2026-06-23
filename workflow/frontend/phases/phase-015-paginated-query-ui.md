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
- Update API client methods for member, product, order, coupon, and coupon-history page responses.
- Include `page`, `size`, `sort`, and selected member/filter state in TanStack Query keys.
- Add compact pager controls to admin member, product, order, coupon, and history tables.
- Keep current shop flow working while reading paginated product/coupon/history contracts.

## Out Of Scope

- Infinite scroll.
- Search backend.
- Product category persistence.
- Export/download UX.
- Redesigning unrelated shop screens.

## Files To Touch

- `frontend/src/entities/commerce/types.ts`
- `frontend/src/entities/commerce/api.ts`
- `frontend/src/shared/ui.tsx`
- `frontend/src/app/styles/global.css`
- `frontend/src/pages/main/MainPage.tsx`
- `frontend/src/pages/members/MembersPage.tsx`
- `frontend/src/pages/products/ProductsPage.tsx`
- `frontend/src/pages/orders-payments/OrdersPaymentsPage.tsx`
- `frontend/src/apps/shop/ShopApp.tsx`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/03-api-state-contract.md`

## Test First

- Update MSW fixtures to return `{ items, page, size, totalElements, totalPages, hasNext }`.
- Update MSW fixtures for member, product, order, coupon, and coupon-history APIs.
- Keep RTL assertions for admin/shop flows after page response conversion.

## Implementation Steps

- [x] Add `PageResponse<T>` and paginated query parameter helpers.
- [x] Update coupon/history API methods and query keys.
- [x] Add admin pager UI for coupon and history tables.
- [x] Update shop coupon consumers to avoid unbounded list assumptions.
- [x] Update member, product, and order API methods to return page responses.
- [x] Add shared pagination controls for admin member, product, order, and shop catalog lists.
- [x] Update dashboard recent orders to read from paged order response.
- [x] Run frontend tests/build.

## Done Criteria

- [x] Frontend no longer calls member/product/order/coupon/history APIs as unbounded arrays.
- [x] Query keys include pagination state.
- [x] Admin list UI exposes page navigation for member, product, order, coupon, and history lists.
- [x] Shop catalog exposes product page navigation.
- [x] Existing purchase, coupon wallet, and exchange flows still pass.
- [x] Frontend tests and build pass.

## Validation

`npm --prefix frontend test -- --run`
`npm --prefix frontend run build`

Result: both passed on 2026-06-24.

Additional validation:

- `bash scripts/hooks/validate_impeccable.sh`
- `python3 scripts/hooks/audit_harness.py --lane frontend --changed-only`

Result: passed on 2026-06-24. Frontend audit reports pre-existing archive/state warnings for older phases.

## Review Focus

Frontend reviewer: verify pagination state, loading/empty behavior, query invalidation, and no optimistic updates for money-moving flows.
