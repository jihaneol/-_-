# Hooks

This folder contains validation and safety scripts used by `scripts/execute.py` before a phase is marked complete.

## Lane Hooks

Fallback validation is lane-specific:

```bash
scripts/hooks/validate_backend.sh
scripts/hooks/validate_frontend.sh
scripts/hooks/validate_impeccable.sh
```

`scripts/hooks/validate.sh backend|frontend` remains as a small compatibility wrapper.

The hooks should stay deterministic and local. `scripts/execute.py --lane <backend|frontend> validate` runs phase-specific commands from `## Validation` when they exist, then uses the lane hook only as the fallback.

## TDD Guard

The lane validation hooks run `scripts/hooks/enforce_tdd.py --lane <backend|frontend>` before tests.

The guard fails when production source files change without an active phase, without a `Test First` plan or explicit `TDD Exception`, or without any test file change. If a feature or fix has no test yet, stop and add or update the test first.

## Harness Audit

The lane validation hooks run a broad harness audit before expensive tests:

```bash
python3 scripts/hooks/audit_harness.py --lane backend --changed-only
python3 scripts/hooks/audit_harness.py --lane frontend --changed-only
```

This audit checks the large failure modes only: current/active phase state, completed phase archive records, accepted validation status, and obvious simple `Result.toResponse()` copy mappers in changed Kotlin files.

Before final handoff, run the stricter state check:

```bash
python3 scripts/hooks/audit_harness.py --lane backend --strict-state
python3 scripts/hooks/audit_harness.py --lane frontend --strict-state
```

To scan existing response mappers as a one-off cleanup check:

```bash
python3 scripts/hooks/audit_harness.py --lane backend --all-response-mappers
```

## Dangerous Command Guard

`scripts/hooks/guard_command.py` blocks known destructive commands before workflow execution, including hard resets, forced cleans, recursive removes, sudo, raw disk writes, filesystem formatting, broad process kills, and downloaded script execution.

Use:

```bash
python3 scripts/execute.py --lane backend run -- <command>
python3 scripts/execute.py --lane frontend run -- <command>
```

for manual commands during long-running work.

## Impeccable Detector

`scripts/hooks/validate_impeccable.sh` runs the project-local Impeccable detector against `frontend/apps` and `frontend/src`.

The frontend lane hook runs it after tests and build. Use it directly when checking UI quality without running the full frontend validation:

```bash
scripts/hooks/validate_impeccable.sh
```

Detector config and narrow ignores live in `.impeccable/config.json`. The Codex edit hook is installed at `.codex/hooks.json` and uses the project-local skill under `.agents/skills/impeccable`.

## Branch Name Guard

Commits are guarded by `.githooks/pre-commit`, which calls `scripts/hooks/enforce_branch_name.py`.

Allowed branch prefixes:

- `docs/*`: planning, harness, workflow phase files, and rules.
- `back/*`: backend work. Frontend paths are blocked.
- `front/*`: frontend work. Backend paths are blocked.
- `common/*`: non-docs, non-backend, non-frontend work such as hooks, scripts, Gradle/root config, and local workflow tooling.

Install the tracked hooks in a local clone with:

```bash
git config core.hooksPath .githooks
```

## Circuit Breaker

`scripts/hooks/circuit_breaker.py` tracks repeated command failures in the lane state folder.

After the same command fails 6 consecutive times, the breaker opens and the agent must stop retrying. The next step is to write an alternative plan, reduce scope, or switch to another phase.

## Resume Recovery

Context-compression recovery is handled by `python3 scripts/execute.py --lane <backend|frontend> resume`, not by a validation hook.

Run it after context compression, thread resume, or handoff. It reads local and Obsidian handoff records, prints the lane current/next phase context, and records the resume event.
