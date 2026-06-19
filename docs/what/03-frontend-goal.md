# Frontend Goal

## Role

Build an operator-facing React admin UI for the commerce order coupon backend.

The frontend is not a marketing site. It exists to make payment flows, settlement results, reconciliation mismatches, and failure states visible.

## Product Position

This UI should help a reviewer understand the backend system quickly:

- Create a member.
- Create a product and initial inventory.
- Create an order.
- Pay the order and inspect issued stamp coupons.
- Run a full refund and inspect voided coupons.
- Inspect coupon history.
- See API errors and retryable states clearly.

## Stack

- React
- TypeScript
- Vite
- React Router
- TanStack Query
- React Hook Form
- Zod
- Vitest
- React Testing Library
- MSW

## UI Style

- Build a dense back-office interface.
- Prefer tables, filters, status badges, forms, tabs, and confirmation dialogs.
- Avoid landing pages, hero sections, decorative cards, and marketing copy.
- Make important states visible: loading, empty, error, success, stale data, and retry.
