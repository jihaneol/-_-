# Run State

This file is the handoff point for long-running Codex work. Update it whenever a task is paused, completed, blocked, or handed to the next run.

## Current Focus

- Active work: none
- Current task: backend phase cleanup
- Current status: stale backend phases 001-005 were removed from active execution and archived under `workflow/backend/archive/2026-06-20/`.

## Last Known Verification

- `JAVA_HOME=$(/usr/libexec/java_home -v 21) ./gradlew test`
- Result: passed before main integration.
- `python3 scripts/execute.py --lane backend status`
- Result: no active backend phase remains after cleanup.

## Next Files To Read

- `AGENT.md`
- `work/03-active-work.md`
- `docs/harness/07-week-plan.md`
- `docs/harness/09-dev-log.md`
- `workflow/backend/state/execute-state.json`

## Next Suggested Step

Start a new backend phase only after a fresh feature is approved in `work/03-active-work.md`.

## Guardrails

- Do not treat archived backend phases 001-005 as active work.
- Dedicated payment ledger work is deferred backlog, not current MVP scope.
- Keep completed-work history in Obsidian, not in growing local work logs.
- Use `scripts/execute.py` for phase state and `scripts/hooks/validate.sh` for automatic validation.

## scripts/execute.py 2026-06-19T03:42:30+00:00

- Resume context loaded
- Current phase: -
- Historical next phase at that time: `workflow/phases/phase-001-idempotency-lookup.md` was later archived as superseded on 2026-06-20.
- Git: 90d82ff docs: 하네스 문서 구조와 phase 보관 정리; M .codex/skills/workflow.md
 M AGENT.md
 M scripts/execute.py
 M workflow/README.md
 M scripts/hooks/README.md

## scripts/execute.py 2026-06-23T14:29:52+00:00

- Started phase: `phase-007-paginated-query-cqrs.md`
