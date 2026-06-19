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
