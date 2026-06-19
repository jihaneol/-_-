# Backend Workflow Skill

Use this skill for backend-only implementation in the parallel harness.

## Lane

- Phase files: `workflow/backend/phases/`
- Archive: `workflow/backend/archive/`
- State: `workflow/backend/state/`
- Runner: `python3 scripts/execute.py --lane backend`
- Validation fallback: `scripts/hooks/validate_backend.sh`

## Rules

- Read relevant `docs/` files before implementation.
- API behavior changes must update `docs/how/05-api-state-contract.md` before frontend work depends on them.
- Backend phases may touch `modules/**`, backend SQL/config, backend docs, and shared API contract docs.
- Backend phases must not touch `frontend/**`.
- Tests come first for behavior changes.
- Use `./gradlew test` or a narrower Gradle command in `## Validation`.

## Parallel Contract

Backend work should expose stable API contracts for the frontend lane. If the API contract is unsettled, document the proposed request/response shape and error model before coding.
