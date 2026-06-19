# Hooks

This folder contains validation scripts used by `execute.py` before a phase is marked complete.

## Default Hook

Fallback validation:

```bash
hooks/validate.sh
```

The hook should stay deterministic and local. `execute.py validate` runs phase-specific commands from `## Validation` when they exist, then uses this hook only as the fallback.

## TDD Guard

`hooks/validate.sh` runs `hooks/enforce_tdd.py` before tests.

The guard fails when production source files change without any test file change. If a feature or fix has no test yet, stop and add or update the test first.
