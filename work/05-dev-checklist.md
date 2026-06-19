# Dev Checklist

## Pre-dev

- [x] Capture request in `work/00-inbox.md`.
- [x] Shape scope in feature candidates and active work.
- [x] Keep Figma dependency explicit.

## Post-dev

- [x] Backend tests pass.
- [x] Frontend tests pass.
- [x] Frontend build passes.
- [x] Figma admin coupon exchange screens reviewed.
- [x] Admin API and frontend dev servers respond locally.
- [x] Coupon consistency report API and UI exist.
- [x] Shop coupon wallet API exists.
- [x] Shop pages 05-12 are implemented and browser-checked on desktop/mobile.
- [x] README explains local setup, workflows, hard parts, and validation.
- [x] Harness docs describe backend, frontend, API, state, test, and design scope.
- [x] Testcontainers-enabled admin/shop API tests pass with rerun.
- [x] Shop ecommerce UX pass hides customer-visible design tabs and browser-checks home/catalog/cart on desktop/mobile.

## Completion Loop Gate

- [x] 1st loop storefront shape: customer-visible navigation, catalog, cart preview, checkout, and my page pass frontend/browser checks.
- [x] 2nd loop product metadata: product API returns `couponAccrualCount` and `exchangeEligible`, shop UI consumes them, and API/frontend tests pass.
- [x] 3rd loop coffee kiosk UX: shop UI shows kiosk heading, category-first menu, order-step rail, option controls, pickup checkout copy, and payment wallet refresh.
- [x] 4th loop coupon wallet/exchange readiness: customer wallet and admin exchange/consistency screens agree with backend histories.
- [x] Final loop portfolio proof: full validation, browser evidence, README, and harness logs are current.

## Remaining Later-Scope Checks

- [ ] Dedicated settlement batch is intentionally deferred.
- [ ] Exchange order ledger is intentionally deferred.
- [ ] Authentication/authorization is intentionally deferred.
