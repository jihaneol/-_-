# Workflow Skill

Use this skill when converting the project brain and user discussion into executable implementation phases.

## Inputs

- All relevant files under `docs/`.
- Current user request.
- Existing active phase files under `workflow/backend/phases/` or `workflow/frontend/phases/`.
- Completed phase files under the lane `archive/` folder when historical context is needed.
- Current lane state in `workflow/backend/state/run-state.md` or `workflow/frontend/state/run-state.md`.

## Process

0. After context compression, thread resume, or handoff, run `python3 scripts/execute.py --lane <backend|frontend> resume` before planning or editing.
1. Read the relevant `docs/` files first. For broad planning, read all of `docs/`.
2. Identify the actual goal, unresolved questions, and implementation risks.
3. Discuss ambiguous product, architecture, transaction, or UI decisions with the user before creating phases.
4. Split work into phases that can each be implemented and verified independently.
5. Create one markdown file per phase under the correct lane folder.
6. Keep each phase small enough that one validation command can prove meaningful progress.
7. Run `python3 scripts/execute.py --lane <backend|frontend> lint-phases` after phase files are ready.
8. Use `python3 scripts/execute.py --lane <backend|frontend> show` before implementation and `python3 scripts/execute.py --lane <backend|frontend> checkpoint "message"` during long or risky work.
9. Run manual commands through `python3 scripts/execute.py --lane <backend|frontend> run -- <command>` during long-running implementation so dangerous-command and circuit-breaker guards apply.

## Resume Rule

`python3 scripts/execute.py --lane <backend|frontend> resume` is the required recovery entrypoint. It reads local handoff, Obsidian active handoff, lane execute state, lane run state, Git state, and the next phase contract, then records that resume context was loaded.

## Phase File Shape

```md
# Phase N: Title

## Goal

## Docs Read

## Scope

## Out of Scope

## Files To Touch

## Test First

## Implementation Steps

## Done Criteria

## Validation

## Review Focus
```

## Phase Rules

- A phase must name the docs it depends on.
- `docs/what/` defines goals and MVP scope, `docs/how/` defines data flow and implementation patterns, and `docs/why/` records tradeoffs and selection reasons.
- A phase that touches production code must include `## Test First` with the test file(s), expected first failure, and validation command.
- TDD is enforced by `scripts/hooks/enforce_tdd.py`; production source changes require an active phase, a Test First plan or explicit exception, and a test file change.
- A phase must name the smallest meaningful validation.
- If a phase lists executable commands in `## Validation`, `scripts/execute.py --lane <backend|frontend> validate` runs those commands. If no command is listed, it falls back to the lane validation hook.
- `Files To Touch` is enforced for production source changes during validation.
- `Done Criteria` checkboxes must be marked `[x]` before completion.
- A phase must not mix unrelated backend, frontend, and documentation work unless they share one done criterion.
- Backend phases live in `workflow/backend/phases/` and may touch `modules/**`, backend docs, and shared API contract docs.
- Frontend phases live in `workflow/frontend/phases/` and may touch `frontend/**`, UI docs, and shared API contract docs.
- Backend and frontend agents must run in separate Codex threads or worktrees for true parallel development.
- A phase that changes payment correctness, idempotency, ledger, settlement, reconciliation, concurrency, persistence, or UI operator flow requires review.
- Completed phases are removed from the lane `phases/` folder by `scripts/execute.py --lane <backend|frontend> complete` and kept under the lane `archive/YYYY-MM-DD/`.
- Repeating the same failing command 6 times opens the circuit breaker. Stop retrying and choose an alternative plan, narrower scope, or another phase.
