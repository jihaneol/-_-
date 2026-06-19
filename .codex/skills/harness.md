# Harness Skill

Use this skill when converting the project brain and user discussion into executable implementation phases.

## Inputs

- All relevant files under `docs/`.
- Current user request.
- Existing active phase files under `harness/phases/`.
- Completed phase files under `harness/archive/` when historical context is needed.
- Current state in `harness/state/run-state.md`.

## Process

0. After context compression, thread resume, or handoff, run `python3 scripts/execute.py resume` before planning or editing.
1. Read the relevant `docs/` files first. For broad planning, read all of `docs/`.
2. Identify the actual goal, unresolved questions, and implementation risks.
3. Discuss ambiguous product, architecture, transaction, or UI decisions with the user before creating phases.
4. Split work into phases that can each be implemented and verified independently.
5. Create one markdown file per phase under `harness/phases/`.
6. Keep each phase small enough that one validation command can prove meaningful progress.
7. Run `python3 scripts/execute.py lint-phases` after phase files are ready.
8. Use `python3 scripts/execute.py show` before implementation and `python3 scripts/execute.py checkpoint "message"` during long or risky work.
9. Run manual commands through `python3 scripts/execute.py run -- <command>` during long-running implementation so dangerous-command and circuit-breaker guards apply.

## Resume Rule

`python3 scripts/execute.py resume` is the required recovery entrypoint. It reads local handoff, Obsidian active handoff, execute state, run state, Git state, and the next phase contract, then records that resume context was loaded.

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
- If a phase lists executable commands in `## Validation`, `scripts/execute.py validate` runs those commands. If no command is listed, it falls back to `scripts/hooks/validate.sh`.
- `Files To Touch` is enforced for production source changes during validation.
- `Done Criteria` checkboxes must be marked `[x]` before completion.
- A phase must not mix unrelated backend, frontend, and documentation work unless they share one done criterion.
- A phase that changes payment correctness, idempotency, ledger, settlement, reconciliation, concurrency, persistence, or UI operator flow requires review.
- Completed phases are removed from `harness/phases/` by `scripts/execute.py complete` and kept under `harness/archive/YYYY-MM-DD/`.
- Repeating the same failing command 6 times opens the circuit breaker. Stop retrying and choose an alternative plan, narrower scope, or another phase.
