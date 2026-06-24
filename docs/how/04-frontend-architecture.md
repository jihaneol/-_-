# Frontend Architecture

## Style

Use Feature-Sliced Design Lite.

This is intentionally lighter than full enterprise FSD. Keep boundaries clear without creating too many tiny folders.

## Folder Shape

```text
frontend/
  admin/
    index.html
    package.json
    src/
      AdminApp.tsx
      main.tsx
  shop/
    index.html
    package.json
    src/
      ShopApp.tsx
      main.tsx
  shared/
    package.json
    src/
      styles/
      shared/
        api/
        test/
        ui.tsx
      entities/
        commerce/
    pages/
      main/
      members/
      products/
      orders-payments/
```

## Layer Rules

- `admin` wires the operator runtime, navigation, Vite config, and deployable bundle.
- `shop` wires the customer shop runtime, Vite config, and deployable bundle.
- `shared/src/styles` keeps global styles.
- `shared/src/pages` composes widgets and features into route-level screens.
- `widgets` combine entities and features into larger screen blocks.
- `features` represent user actions such as authorize, cancel, run settlement, and run reconciliation.
- `shared/src/entities` holds domain-facing frontend models, query hooks, and display helpers.
- `shared/src/shared` holds reusable UI, API client, configuration, and low-level utilities.

## State Rules

- Use TanStack Query for server state.
- Use React Hook Form for form state.
- Use Zod for request validation and response parsing where useful.
- Use local component state for UI-only controls.
- Add Zustand only if cross-page UI state becomes real.

## Dependency Direction

```text
admin/shop -> shared pages -> shared entities -> shared primitives
```

Lower layers must not import from higher layers.

## Runtime Split

- Admin and shop are separate frontend projects with separate local origins.
- `npm --prefix frontend run dev:admin` serves the admin app at `http://127.0.0.1:5173/`.
- `npm --prefix frontend run dev:shop` serves the shop app at `http://127.0.0.1:5174/`.
- `npm --prefix frontend run build:admin` builds the admin app into `frontend/dist/admin`.
- `npm --prefix frontend run build:shop` builds the shop app into `frontend/dist/shop`.
- `npm --prefix frontend run build` builds both deployable app bundles.
