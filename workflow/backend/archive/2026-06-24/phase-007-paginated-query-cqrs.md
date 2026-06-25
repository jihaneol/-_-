# Phase 7: Paginated Query CQRS

## Goal

Convert collection reads from unbounded list-style queries to QueryDSL-backed paginated query results.

## Docs Read

- `rules/backend-architecture.md`
- `rules/service-code-rule.md`
- `rules/controller-code-rule.md`
- `docs/harness/04-api-contract.md`
- `docs/how/05-api-state-contract.md`
- `work/03-active-work.md`

## Scope

- Add shared page query/result models for application query flows.
- Migrate coupon and coupon-history list endpoints first.
- Add page metadata response shape: `items`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`.
- Implement QueryDSL read adapter methods for paginated coupon and history projections.
- Update controller tests and integration tests for page shape and bounded reads.

## Out Of Scope

- Cursor pagination.
- Full-text search.
- Export APIs.
- Migrating every admin table in the first backend slice.
- Frontend UI changes; those are handled by the paired frontend phase.

## Files To Touch

- `modules/application/src/main/kotlin/com/example/cardservice/application/commerce`
- `modules/infra/src/main/kotlin/com/example/cardservice/infra`
- `modules/admin-api/src/main/kotlin/com/example/cardservice/web/commerce`
- `modules/shop-api/src/main/kotlin/com/example/cardservice/web/shop`
- `modules/admin-api/src/test/kotlin/com/example/cardservice/web/commerce`
- `modules/shop-api/src/test/kotlin/com/example/cardservice/web/shop`
- `docs/how/05-api-state-contract.md`

## Test First

- Add or update controller tests to expect `$.data.items`, `$.data.page`, `$.data.size`, and `$.data.hasNext` for coupon and history list routes.
- Add QueryDSL adapter integration coverage for limit/offset/sort/count if an infra test harness already exists; otherwise document the test gap in the phase.

## Implementation Steps

- [x] Introduce page query/result model names aligned with rules.
- [x] Change coupon and history query use cases from `List<T>` to page results.
- [x] Implement QueryDSL-backed paginated read adapter methods.
- [x] Update admin/shop controllers to accept `page`, `size`, and `sort`.
- [x] Update tests and API contract evidence.

## Done Criteria

- [x] Coupon and coupon-history collection endpoints no longer expose unbounded list query contracts.
- [x] Query side uses QueryDSL adapter paths, not command aggregate loading.
- [x] API response includes `items` and page metadata.
- [x] Backend tests pass with Java 21.
- [x] Remaining unpaginated collection endpoints are listed as follow-up scope.

## Validation

`JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`

Result: passed on 2026-06-23.

`python3 scripts/hooks/audit_harness.py --lane backend --changed-only`

Result: passed on 2026-06-23.

`python3 scripts/hooks/audit_harness.py --lane backend --all-response-mappers`

Result: passed on 2026-06-23.

## Follow-up Scope

- Product, member, and order admin collection reads still use bounded follow-up scope from earlier list APIs.
- Infra-level QueryDSL slice tests for limit/offset/count can be added when the repository test harness is expanded.

## Review Focus

Database reviewer: verify QueryDSL paging, count query, sort fields, index needs, and absence of unbounded reads.
