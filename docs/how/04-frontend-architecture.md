# Frontend Architecture

## Style

Use Feature-Sliced Design Lite.

This is intentionally lighter than full enterprise FSD. Keep boundaries clear without creating too many tiny folders.

## Folder Shape

```text
frontend/
  apps/
    admin/
      AdminApp.tsx
      main.tsx
    shop/
      ShopApp.tsx
      main.tsx
  src/
    app/
      styles/
    pages/
      main/
      members/
      products/
      orders-payments/
    entities/
      commerce/
    shared/
      api/
      test/
      ui.tsx
```

## Layer Rules

- `apps/admin` wires the operator runtime, navigation, and admin entrypoint.
- `apps/shop` wires the customer shop runtime and shop entrypoint.
- `app` keeps global styles and legacy compatibility exports only.
- `pages` compose widgets and features into route-level screens.
- `widgets` combine entities and features into larger screen blocks.
- `features` represent user actions such as authorize, cancel, run settlement, and run reconciliation.
- `entities` hold domain-facing frontend models, query hooks, and display helpers.
- `shared` holds reusable UI, API client, configuration, and low-level utilities.

## State Rules

- Use TanStack Query for server state.
- Use React Hook Form for form state.
- Use Zod for request validation and response parsing where useful.
- Use local component state for UI-only controls.
- Add Zustand only if cross-page UI state becomes real.

## Dependency Direction

```text
apps -> pages -> entities -> shared
```

Lower layers must not import from higher layers.

## Runtime Split

- `frontend/index.html` loads `apps/admin/main.tsx`.
- `frontend/shop.html` loads `apps/shop/main.tsx`.
- `npm --prefix frontend run build` builds both apps.
- `npm --prefix frontend run build:admin` builds the admin app.
- `npm --prefix frontend run build:shop` builds the shop app.
