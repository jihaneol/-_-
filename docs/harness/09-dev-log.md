# Harness Dev Log

## 2026-06-19

- Added admin/shop split planning in project docs.
- Chose `admin-api` and `shop-api` as backend runtime module names.
- Chose `docs/*`, `back/*`, `front/*`, and `common/*` branch prefixes.
- Clarified `common/*` is for non-docs, non-backend, non-frontend tooling/root-config work only.
- Created backend and frontend phase files for admin/shop runtime split.

## Open Decisions

- Whether coupon exchange creates an order record or only coupon history.
- Whether current `bootstrap` is removed in the same phase or retired after compatibility tests.
- Whether shared web error handling is duplicated in API modules or extracted later.
