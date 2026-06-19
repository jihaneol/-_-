# Harness Skill

Use this skill when converting the project brain and user discussion into executable implementation phases.

## Inputs

- All relevant files under `docs/`.
- Current user request.
- Existing phase files under `harness/phases/`.
- Current state in `harness/state/run-state.md`.

## Process

1. Read the relevant `docs/` files first. For broad planning, read all of `docs/`.
2. Identify the actual goal, unresolved questions, and implementation risks.
3. Discuss ambiguous product, architecture, transaction, or UI decisions with the user before creating phases.
4. Split work into phases that can each be implemented and verified independently.
5. Create one markdown file per phase under `harness/phases/`.
6. Keep each phase small enough that one validation command can prove meaningful progress.
7. Run `python3 execute.py lint-phases` after phase files are ready.
8. Use `python3 execute.py show` before implementation and `python3 execute.py checkpoint "message"` during long or risky work.

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
- A phase that touches production code must include `## Test First` with the test file(s), expected first failure, and validation command.
- A phase must name the smallest meaningful validation.
- If a phase lists executable commands in `## Validation`, `execute.py validate` runs those commands. If no command is listed, it falls back to `hooks/validate.sh`.
- `Files To Touch` is enforced for production source changes during validation.
- `Done Criteria` checkboxes must be marked `[x]` before completion.
- A phase must not mix unrelated backend, frontend, and documentation work unless they share one done criterion.
- A phase that changes payment correctness, idempotency, ledger, settlement, reconciliation, concurrency, persistence, or UI operator flow requires review.
