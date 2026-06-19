# Run State

This file is the handoff point for long-running Codex work. Update it whenever a task is paused, completed, blocked, or handed to the next run.

## Current Focus

- Active work: `harness/phases/phase-001-idempotency-lookup.md`
- Current task: Payment idempotency lookup
- Current status: Agent operating structure uses `docs/what`, `docs/how`, `docs/why`, `AGENT.md`, `hooks/`, `execute.py`, and `harness/phases/`. Feature implementation has not started in this run.

## Last Known Verification

- `python3 execute.py status`
- Result: phase `phase-001-idempotency-lookup.md` detected as pending
- `hooks/validate.sh`
- Result: passed

## Next Files To Read

- `AGENT.md`
- `.codex/skills/harness.md`
- `harness/phases/phase-001-idempotency-lookup.md`
- `docs/how/01-domain-model.md`
- `docs/how/02-api-contract.md`
- `docs/how/00-architecture.md`
- `docs/how/03-test-strategy.md`

## Next Suggested Step

Discuss whether phase 001 should be split into smaller phases before implementation. The first likely slice is idempotency lookup before saving payment.

## Guardrails

- Do not implement work that is not summarized in `harness/phases/phase-001-idempotency-lookup.md`.
- Keep completed-work history in Obsidian, not in growing local work logs.
- Use `execute.py` for phase state and `hooks/validate.sh` for automatic validation.
