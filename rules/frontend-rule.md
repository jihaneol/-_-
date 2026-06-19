# Frontend Rule

## Scope

Use this rule for all changes under `frontend/**`.

## Defaults

- Use React, TypeScript, Vite, TanStack Query, React Hook Form, Zod, Vitest, React Testing Library, and MSW.
- Keep the UI dense and operational. Do not build marketing pages for admin workflows.
- Build from `docs/how/05-api-state-contract.md`; do not infer API behavior from backend source during frontend-only work.
- Keep page components focused on composition. Move repeated action logic, tables, or forms into feature/widget/shared modules when they grow.

## Required States

Every operator workflow must show the relevant states:

- loading
- empty
- API validation error
- server error
- success feedback
- disabled submit while pending

## TDD

- Write or update a Vitest/React Testing Library test before changing frontend behavior.
- Use MSW for API success and failure paths.
- A frontend phase must validate with:

```bash
npm --prefix frontend test -- --run
npm --prefix frontend run build
```

## Parallel Work

- Frontend phases must not edit `modules/**`.
- If a backend API change is needed, update `docs/how/05-api-state-contract.md` and hand it to the backend lane.
- If the backend is not ready, implement against MSW using the documented contract.
