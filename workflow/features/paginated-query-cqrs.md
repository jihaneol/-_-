# Feature: Paginated Query CQRS

## Goal

Move growing collection reads to CQRS query-side pagination so admin and shop screens do not rely on unbounded "all" reads.

## Pipeline

- backend: phase-007-paginated-query-cqrs.md
- frontend: phase-015-paginated-query-ui.md

## Completion Rule

- Backend pagination contracts and QueryDSL adapter validation must pass before frontend treats page metadata as stable.
- Frontend completion requires updated query keys, page controls, MSW fixtures, tests, and build.
- Final completion requires `python3 scripts/execute.py feature gate paginated-query-cqrs` to pass after both lane phases are completed.

## Review Focus

- Query side uses QueryDSL projections/page results.
- No top-level unbounded list contract remains for migrated routes.
- Frontend query keys include page/filter/sort state.
- Pagination controls handle empty, first, middle, and last page states.
