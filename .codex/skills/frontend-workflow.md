# Frontend Workflow Skill

Use this skill for frontend-only implementation in the parallel harness.

## Lane

- Phase files: `workflow/frontend/phases/`
- Archive: `workflow/frontend/archive/`
- State: `workflow/frontend/state/`
- Runner: `python3 scripts/execute.py --lane frontend`
- Validation fallback: `scripts/hooks/validate_frontend.sh`

## Rules

- Build from `docs/how/05-api-state-contract.md`, `docs/what/03-frontend-goal.md`, and UI docs.
- Frontend phases may touch `frontend/**`, frontend/UI docs, and shared API contract docs.
- Frontend phases must not touch `modules/**`.
- Use MSW when the backend endpoint is not ready or when testing failure states.
- Tests come first for behavior changes.
- Use `npm --prefix frontend test -- --run` and `npm --prefix frontend run build` in `## Validation`.

## UI Contract

The UI is an operator tool, not a landing page. New screens must expose loading, empty, error, success, and disabled-submit states where relevant.
