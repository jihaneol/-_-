# Hooks

This folder contains validation and safety scripts used by `scripts/execute.py` before a phase is marked complete.

## Default Hook

Fallback validation:

```bash
scripts/hooks/validate.sh
```

The hook should stay deterministic and local. `scripts/execute.py validate` runs phase-specific commands from `## Validation` when they exist, then uses this hook only as the fallback.

## TDD Guard

`scripts/hooks/validate.sh` runs `scripts/hooks/enforce_tdd.py` before tests.

The guard fails when production source files change without an active phase, without a `Test First` plan or explicit `TDD Exception`, or without any test file change. If a feature or fix has no test yet, stop and add or update the test first.

## Dangerous Command Guard

`scripts/hooks/guard_command.py` blocks known destructive commands before harness execution, including hard resets, forced cleans, recursive removes, sudo, raw disk writes, filesystem formatting, broad process kills, and downloaded script execution.

Use:

```bash
python3 scripts/execute.py run -- <command>
```

for manual commands during long-running work.

## Circuit Breaker

`scripts/hooks/circuit_breaker.py` tracks repeated command failures in `harness/state/circuit-breaker.json`.

After the same command fails 6 consecutive times, the breaker opens and the agent must stop retrying. The next step is to write an alternative plan, reduce scope, or switch to another phase.

## Resume Recovery

Context-compression recovery is handled by `python3 scripts/execute.py resume`, not by a validation hook.

Run it after context compression, thread resume, or handoff. It reads local and Obsidian handoff records, prints the current/next phase context, and records the resume event.
