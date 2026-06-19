# Frontend Architecture

## Style

Use Feature-Sliced Design Lite.

This is intentionally lighter than full enterprise FSD. Keep boundaries clear without creating too many tiny folders.

## Folder Shape

```text
frontend/
  src/
    app/
      providers/
      router/
      styles/
    pages/
      commerce-dashboard/
    widgets/
      order-table/
      coupon-history-table/
    features/
      create-member/
      create-product/
      create-order/
      pay-order/
      refund-order/
    entities/
      commerce/
    shared/
      api/
      config/
      lib/
      ui/
      types/
```

## Layer Rules

- `app` wires providers, router, and global styles.
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
app -> pages -> widgets -> features -> entities -> shared
```

Lower layers must not import from higher layers.
