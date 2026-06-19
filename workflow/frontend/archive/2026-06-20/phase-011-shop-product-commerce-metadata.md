# Phase 011: Shop Product Commerce Metadata

This phase makes the shop UI consume product commerce metadata from the API instead of recalculating coupon policy inside components.

## Goal

Keep coupon accrual and exchange eligibility display contract-driven from product responses.

## Docs Read

- `docs/harness/04-api-contract.md`
- `docs/frontend-harness/03-api-state-contract.md`
- `docs/frontend-harness/05-frontend-week-plan.md`
- `work/03-active-work.md`

## Scope

- Add frontend product fields for `couponAccrualCount` and `exchangeEligible`.
- Update shop product cards, catalog rows, detail, cart, and checkout to render metadata from API response.
- Update MSW fixtures and RTL assertions.
- Coordinate with backend response DTO changes.

## Out Of Scope

- Dynamic accrual policy editor.
- Category/search backend.
- Customer self-exchange.
- Product option persistence.

## Files To Touch

- `frontend/src/entities/commerce/types.ts`
- `frontend/apps/shop/ShopApp.tsx`
- `frontend/src/app/App.test.tsx`
- `docs/frontend-harness/*`
- `work/*`

## Test First

- Update MSW product fixture to include product commerce metadata.
- First expected failure: frontend type/test mismatch if components still expect old product shape.

## Implementation Steps

- [x] Extend `Product` type with metadata fields.
- [x] Update product fixture builder.
- [x] Replace price-derived UI calculations with API metadata reads.
- [x] Run frontend validation.

## Done Criteria

- [x] Shop UI reads `couponAccrualCount` from product data.
- [x] Shop UI reads `exchangeEligible` from product data.
- [x] RTL shop flow still passes.
- [x] Frontend build passes.
- [x] Impeccable gate passes.
- [x] Browser quick check confirms metadata displays without `undefined`.

## Validation

Passed:

- `npm --prefix frontend test -- --run`
- `npm --prefix frontend run build`
- `bash scripts/hooks/validate_impeccable.sh`
- Desktop/mobile browser quick check.

## Review Focus

- Whether frontend no longer owns coupon policy math.
- Whether product metadata remains customer-safe.
- Whether tests cover the new response shape.
