# Harness Dev Log

## 2026-06-19

- Added admin/shop split planning in project docs.
- Chose `admin-api` and `shop-api` as backend runtime module names.
- Chose `docs/*`, `back/*`, `front/*`, and `common/*` branch prefixes.
- Clarified `common/*` is for non-docs, non-backend, non-frontend tooling/root-config work only.
- Created backend and frontend phase files for admin/shop runtime split.
- Split the backend HTTP runtime into `admin-api` and `shop-api`.
- Removed the legacy `bootstrap` Gradle module.
- Verified backend runtime split with `./gradlew test` on JDK 21.

## 2026-06-20

- Removed stale backend active phases 001-005 from `workflow/backend/phases`.
- Archived phase 001 and 004 as superseded by the commerce order payment duplicate-protection flow.
- Archived phase 002 and 003 as deferred payment-ledger backlog outside the current coupon exchange/shop MVP.
- Archived phase 005 as completed commerce coupon MVP.
- Updated backend execute state so no stale backend phase remains active.
- Added admin coupon exchange approval for ten issued coupons and one 5,000 KRW product.
- Added coupon exchange histories and coupon consistency report.
- Added shop coupon wallet API under `/api/shop/**`.
- Added Figma-inspired shop pages 05-12 and browser-checked desktop/mobile overflow.
- Added `scripts/local-stack.sh` and updated README for the current admin/shop split.
- Updated harness and frontend planning docs to match the completed scope.
- Validation passed: full Gradle test suite, frontend Vitest suite, frontend production build, design detector hook, and Testcontainers-enabled admin/shop API tests with rerun.
- Defined explicit 1st, 2nd, 3rd, 4th, and final frontend/backend completion loops in the harness docs.
- Completed 2nd loop: product responses now include `couponAccrualCount` and `exchangeEligible`, and the shop UI consumes those fields instead of recalculating coupon policy in components.
- 2nd loop validation passed: `:shop-api:test`, `:application:test`, `:admin-api:test`, frontend Vitest, frontend build, impeccable gate, shop product API curl, and desktop/mobile browser quick check.
- Planned 3rd loop from coffee ordering references: category-first menu, order-step rail, language/accessibility utilities, local drink options, pickup summary, smart cart summary, and coupon integration.
- Completed 3rd loop: shop home now presents a coffee kiosk ordering task; catalog has cafe categories and an order ticket; cart/detail show local drink options; checkout uses pickup/payment language.
- 3rd loop validation passed: frontend Vitest, frontend production build, impeccable gate, local front/shop/admin health checks, and desktop/mobile browser checks for kiosk home/catalog/cart with no horizontal overflow.
- Backfilled frontend phase contracts after identifying that completed shop UI loops had been tracked in `work/` and `docs/frontend-harness` but not in lane phase files.
- Archived completed frontend phases 009-012 and created active frontend phases 013-014 for coupon wallet/exchange readiness and final portfolio proof.
- Completed 4th loop: customer wallet, shop coupon summary, admin exchange summary, coupon tables, and consistency report now share the `적립 중`, `교환 가능`, `교환 완료`, and `회수` vocabulary.
- Completed final frontend proof loop: frontend tests, production build, impeccable gate, local HTTP health checks, and desktop/mobile browser checks passed.
- Archived frontend phases 013-014. No active frontend phase remains.

## Open Decisions

- Whether coupon exchange later creates a dedicated exchange order record or continues to rely on coupon history only.
- Whether shared web error handling is duplicated in API modules or extracted later.
- Whether settlement/reconciliation is implemented next or kept as a later portfolio slice.
