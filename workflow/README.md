# Workflow

`workflow/` is the lane-based execution workspace. It is not the project brain.

## Lanes

| Lane | Active phases | State | Archive | Validation |
|---|---|---|---|---|
| Backend | `workflow/backend/phases/` | `workflow/backend/state/` | `workflow/backend/archive/` | `scripts/hooks/validate_backend.sh` |
| Frontend | `workflow/frontend/phases/` | `workflow/frontend/state/` | `workflow/frontend/archive/` | `scripts/hooks/validate_frontend.sh` |

## Commands

Use the same command shape for both lanes:

```bash
scripts/backend status
scripts/frontend status
```

These wrappers call `python3 scripts/execute.py --lane backend` and `python3 scripts/execute.py --lane frontend`.

## Feature Pipelines

Use `workflow/features/*.md` when one user-visible feature needs both backend and frontend work.

The pipeline keeps lane work separate but prevents early completion reports:

```md
## Pipeline

- backend: phase-006-admin-shop-api-runtime-split.md
- frontend: phase-007-admin-shop-frontend-app-split.md
```

Feature commands:

| Command | Purpose |
|---|---|
| `python3 scripts/execute.py feature list` | List known full-stack pipelines |
| `python3 scripts/execute.py feature show <name>` | Show backend/frontend phase order |
| `python3 scripts/execute.py feature next <name>` | Show the next incomplete lane phase |
| `python3 scripts/execute.py feature gate <name>` | Pass only when every phase has completed validation |
| `python3 scripts/execute.py feature complete <name>` | Record the full-stack feature as completed |

Do not tell the user a full-stack feature is complete until `feature gate` passes.

Common commands:

| Command | Purpose |
|---|---|
| `resume` | Recover context after compression or handoff |
| `lint-phases` | Validate phase file shape |
| `show [phase]` | Print the phase contract |
| `start` | Mark the next pending phase in progress |
| `checkpoint "message"` | Save a recovery point |
| `validate` | Run TDD, scope, command guard, circuit breaker, and lane tests |
| `review "note"` | Accept required phase review |
| `complete` | Archive the phase and auto-commit if gates pass |

## Parallel Rule

True parallel development requires separate Codex threads or worktrees:

- Backend lane edits backend files and shared API contract docs.
- Frontend lane edits frontend files and shared API contract docs.
- `docs/how/05-api-state-contract.md` is the handoff contract between lanes.

Backend agents do not edit `frontend/**`. Frontend agents do not edit `modules/**`.

## Full-Stack Completion Loop

For a backend-to-frontend feature, use this loop:

1. Run the backend phase until backend validation passes and the phase completes.
2. Check `python3 scripts/execute.py feature next <feature>`.
3. If the next step is frontend, switch to the frontend lane and continue implementation there.
4. Run frontend validation until it passes and the phase completes.
5. Run `python3 scripts/execute.py feature gate <feature>`.
6. Report completion only after the gate passes.
