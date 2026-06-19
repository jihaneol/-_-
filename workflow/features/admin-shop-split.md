# Feature: Admin Shop Split

This pipeline defines the full-stack completion gate for separating the service into admin and shop surfaces.

## Goal

Complete the admin/shop split only after the backend API runtimes and the frontend admin/shop apps are both implemented, validated, reviewed, and recorded.

## Pipeline

- backend: phase-006-admin-shop-api-runtime-split.md
- frontend: phase-007-admin-shop-frontend-app-split.md

## Completion Rule

- Backend validation must pass before frontend implementation starts.
- Frontend validation must pass before the feature is reported as complete.
- A completed backend phase alone is only a lane milestone, not the feature delivery.
- Final completion requires `python3 scripts/execute.py feature gate admin-shop-split` to pass.

## Review Focus

- Admin APIs and shop APIs are separated by runtime and route namespace.
- Admin UI does not leak into the customer shop app.
- Customer shop UI uses `/api/shop/**` contracts and does not depend on admin APIs.
- Shared code is explicit and small.
