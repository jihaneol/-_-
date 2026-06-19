# Harness

`harness/` is the execution workspace. It is not the project brain.

## Flow

1. Read `docs/`.
2. Discuss unclear decisions with the user.
3. Create ordered phase files in `harness/phases/`.
4. Run `python3 execute.py lint-phases`.
5. Run `python3 execute.py status`.
6. Inspect the next phase with `python3 execute.py show`.
7. Resume after context compression or handoff with `python3 execute.py resume`.
8. Sync handoff records with `python3 execute.py sync` when needed.
9. Start the next phase with `python3 execute.py start`.
10. Save mid-work recovery notes with `python3 execute.py checkpoint "message"`.
11. Validate with `python3 execute.py validate`.
12. If completion reports review required, review the phase and run `python3 execute.py review "note"`.
13. Mark all completed `Done Criteria` checkboxes as `[x]`.
14. Complete with `python3 execute.py complete`.

Completion automatically moves the finished phase into `harness/archive/YYYY-MM-DD/` and commits the result when all gates pass.

`execute.py` automatically syncs phase state to:

- `harness/state/execute-state.json`
- `harness/state/run-state.md`
- `.codex/context/active-handoff.md`
- Obsidian active handoff: `09.Context Handoffs/01.Active Work/card-service/현재작업.md`
- Obsidian day log under `07.Build Logs/card-service/days/`

## Commands

| Command | Purpose |
|---|---|
| `python3 execute.py status` | Print phase state without syncing Obsidian |
| `python3 execute.py lint-phases` | Validate phase file shape, numbering, TDD plan, and scope warnings |
| `python3 execute.py show [phase]` | Print the phase implementation contract |
| `python3 execute.py sync` | Sync Obsidian active work and local handoff |
| `python3 execute.py resume` | Load Obsidian/local handoff, print recovery context, and record resume |
| `python3 execute.py start` | Mark the next pending phase in progress; requires a clean worktree |
| `python3 execute.py start --allow-dirty` | Start with existing dirty files recorded as baseline; completion refuses to auto-commit those files |
| `python3 execute.py checkpoint "message"` | Record a recovery point during implementation |
| `python3 execute.py validate` | Run phase validation commands or fallback hook |
| `python3 execute.py review "note"` | Accept review when a phase is in `review_required` |
| `python3 execute.py complete` | Complete a validated phase |

## Completion Gates

`execute.py complete` requires:

- Passing validation.
- All `Done Criteria` checkboxes marked `[x]`.
- Review accepted when the phase has `Review Focus`.
- No pre-existing dirty files from phase start remain changed.
- Completed phase file archived under `harness/archive/YYYY-MM-DD/`.
- Auto commit succeeds or there are no changes to commit.

`execute.py validate` requires:

- TDD guard passes.
- Production source changes stay within `Files To Touch`.

## Contents

| Path | Role |
|---|---|
| `harness/phases/` | Ordered implementation phase files |
| `harness/archive/` | Completed phase files grouped by completion date |
| `harness/state/` | Execution state, run handoff, and phase status |
