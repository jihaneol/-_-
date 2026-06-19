# Frontend Architecture Harness

## Target Shape

```text
frontend/
  apps/
    admin/
    shop/
  packages/
    shared/
```

If the existing Vite setup cannot support this in one step, migrate incrementally while preserving admin tests.

## Boundaries

- Admin app imports admin pages, admin API client, and shared utilities.
- Shop app imports shop pages, shop API client, and shared utilities.
- Admin and shop feature modules must not import from each other.
- Shared code should stay small: API base client, primitive UI, formatting, and common types.

## Query Namespaces

- Admin query keys start with `admin`.
- Shop query keys start with `shop`.

## Build Goal

Each app should have a clear npm script for test/build once the split is complete.
