# Phase 006: Admin Shop API Runtime Split

This phase replaces the single HTTP runtime boundary with explicit admin and shop API runtime modules.

## Goal

Split the current `bootstrap` HTTP runtime into `admin-api` and `shop-api` modules so operator workflows and customer workflows are exposed through separate Spring Boot entrypoints and route namespaces.

## Docs Read

- `docs/operations/01-feature-candidates.md`
- `docs/what/02-roadmap.md`
- `docs/how/00-architecture.md`
- `docs/how/02-api-contract.md`
- `docs/how/05-api-state-contract.md`
- `rules/backend-architecture.md`
- `rules/controller-code-rule.md`
- `rules/test-rule.md`

## Scope

- Add Gradle modules `admin-api` and `shop-api`.
- Move or copy the current web runtime wiring out of `bootstrap` into the new API modules.
- Expose admin controllers under `/api/admin/**`.
- Expose shop controllers under `/api/shop/**`.
- Keep `domain`, `application`, `infra`, `external`, and `batch` shared.
- Keep API request/response DTOs in `application` unless a later decision changes the DTO boundary.
- Add runtime smoke tests proving each API module starts.
- Add web tests proving admin-only routes are not exposed by `shop-api`.

## Out Of Scope

- Authentication and authorization.
- Separate databases per runtime.
- Separate domain/application modules.
- Frontend app split.
- Coupon redemption behavior beyond route placement.
- Docker Compose service split.

## Files To Touch

- `settings.gradle.kts`
- `modules/admin-api`
- `modules/shop-api`
- `modules/bootstrap`
- `docs/how/00-architecture.md`
- `docs/harness/09-dev-log.md`

## Test First

- Add a shop web test that expects an admin-only route such as `GET /api/admin/dashboard/summary` to be unavailable from the shop runtime.
- Add admin and shop context smoke tests before moving the runtime entrypoints.
- Validation commands:
  - `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test`
  - `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :shop-api:test`

## Implementation Steps

- [x] Add `admin-api` and `shop-api` modules to Gradle.
- [x] Add `AdminApiApplication` and `ShopApiApplication` entrypoints.
- [x] Move common web response and exception handling into both API modules or a shared web package if duplication becomes harmful.
- [x] Move admin controllers to `admin-api` and prefix routes with `/api/admin`.
- [x] Move shop controllers to `shop-api` and prefix routes with `/api/shop`.
- [x] Remove or retire the old `bootstrap` module once both runtime modules compile and tests pass.
- [x] Update tests to target the correct API module.
- [x] Run backend validation.

## Done Criteria

- [x] `admin-api` starts independently.
- [x] `shop-api` starts independently.
- [x] Admin routes are available only from `admin-api`.
- [x] Shop routes are available only from `shop-api`.
- [x] Shared application/domain/infra dependencies compile without duplicating business logic.
- [x] Old `bootstrap` runtime is removed or clearly marked as retired.
- [x] Relevant Gradle tests pass.

## Validation

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew :admin-api:test :shop-api:test`
- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`

## Review Focus

- Runtime boundary clarity between admin and shop APIs.
- Whether shop API accidentally exposes operator workflows.
- Whether module dependencies still point inward.
- Whether the split stayed at inbound adapter/runtime boundary rather than leaking into domain/application.
