# Frontend Architecture Harness

## Target Shape

```text
frontend/
  admin/
  shop/
  shared/
```

The root `frontend` package is a workspace runner. Admin and shop are separate Vite projects, while `shared` contains reusable API, type, UI, style, and page modules.

## Boundaries

- Admin app imports admin pages, admin API client, and shared utilities.
- Shop app imports shop pages, shop API client, and shared utilities.
- Admin and shop feature modules must not import from each other.
- Shared code should stay small: API base client, primitive UI, formatting, and common types.
- Admin and shop currently share commerce entity types, but the API functions and query keys are namespaced.

## Query Namespaces

- Admin query keys start with `admin`.
- Shop query keys start with `shop`.

## Build Goal

Each app should have a clear npm script for test/build once the split is complete.

## Current Scripts

```bash
npm --prefix frontend run dev
npm --prefix frontend run dev:admin
npm --prefix frontend run dev:shop
npm --prefix frontend run build
npm --prefix frontend run build:admin
npm --prefix frontend run build:shop
npm --prefix frontend test -- --run
```
