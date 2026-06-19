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

## Open Decisions

- Whether coupon exchange creates an order record or only coupon history.
- Whether shared web error handling is duplicated in API modules or extracted later.
