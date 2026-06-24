# Frontend Week Plan Harness

## Day 1

- Decide final app folder shape.
- Add tests that distinguish admin app from shop app.

## Day 2

- Move current operator UI into admin app.
- Preserve admin workflow tests.

## Day 3

- Move approved customer screens into shop app.
- Remove admin navigation from shop app.

## Day 4

- Split API clients and query keys.
- Update MSW handlers.

## Day 5

- Align shop app with `/api/shop/**` contract.
- Add coupon wallet tests.

## Day 6

- Run frontend validation.
- Run frontend reviewer pass.

## Day 7

- Polish app boundaries and README screenshots.
- Record browser validation for pages 05-12.

## Current Completion Evidence

- `frontend/test/App.test.tsx` covers admin navigation, shop navigation, pages 05-12, signup, payment, and wallet refresh.
- `bash scripts/hooks/validate_impeccable.sh` passes against the current frontend.
- Browser automation checked pages 05-12 at 1440px and 390px widths with no horizontal overflow.
- `npm --prefix frontend test -- --run` and `npm --prefix frontend run build` pass after the 05-12 implementation.

## Shop Ecommerce UX Pass

- Remove customer-visible design page tabs.
- Rework header around catalog/search/cart/account.
- Make home product-led and move coupon copy into loyalty/benefit modules.
- Rework catalog cards and cart/order preview.
- Verify purchase-to-wallet flow still passes.

Status: completed for the first frontend-only pass. Persistent cart/search/category APIs remain out of scope.

## Frontend/Backend Completion Loops

### 1st Loop - Customer Storefront Shape

- Status: completed.
- Phase: `workflow/frontend/archive/2026-06-20/phase-010-shop-ecommerce-ux-pass.md`.
- Frontend proof: header, product-led home, ecommerce catalog, local cart preview, checkout, and my page.
- Backend proof: shop app still calls only `/api/shop/**`.

### 2nd Loop - API-Owned Product Commerce Metadata

- Status: completed.
- Phase: `workflow/frontend/archive/2026-06-20/phase-011-shop-product-commerce-metadata.md`.
- Frontend proof: product cards, rows, detail, cart, and checkout render `couponAccrualCount` from the API.
- Backend proof: product responses expose `couponAccrualCount` and `exchangeEligible`.
- Test proof: API contract test, RTL fixture, frontend build, impeccable gate, and desktop/mobile browser check cover the fields.

### 3rd Loop - Coffee Kiosk Ordering UX

- Status: completed.
- Phase: `workflow/frontend/archive/2026-06-20/phase-012-coffee-kiosk-ordering-ux.md`.
- Frontend proof: kiosk heading, cafe categories, order-step rail, drink options, pickup checkout, and coupon summary.
- Backend proof: existing product/order/payment/wallet APIs remain the only authoritative state.
- Test proof: RTL flow, frontend build, impeccable gate, dev-server health check, and desktop/mobile browser check.

### 4th Loop - Coupon Wallet and Exchange Readiness

- Status: completed.
- Phase: `workflow/frontend/archive/2026-06-20/phase-013-coupon-wallet-exchange-readiness-ui.md`.
- Frontend proof: my page and admin exchange UI show matching coupon status vocabulary.
- Backend proof: wallet and consistency report agree after issue, void, and exchange flows.
- Validation: RTL tests, frontend build, impeccable gate, and desktop/mobile browser checks passed.

### Final Loop - Portfolio Proof

- Status: completed.
- Phase: `workflow/frontend/archive/2026-06-20/phase-014-frontend-portfolio-proof.md`.
- Frontend proof: desktop/mobile browser screenshots or automation notes show no overflow and no admin leakage.
- Validation: final frontend tests, build, impeccable gate, local health checks, and browser proof passed.
