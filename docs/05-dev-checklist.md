# Development Checklist

Use this checklist before and after implementing any feature.

## Before Coding

- [ ] The idea exists in `00-inbox.md` or `01-feature-candidates.md`.
- [ ] The feature is prioritized in `02-roadmap.md`.
- [ ] `harness/phases/*.md` describes the current implementation contract.
- [ ] `python3 execute.py lint-phases` passes.
- [ ] `harness/phases/*.md` includes `Test First`.
- [ ] The first expected failing test is named.
- [ ] Backend impact is known.
- [ ] Frontend impact is known.
- [ ] Tests are listed.
- [ ] Excluded scope is explicit.

## During Coding

- [ ] Write or update the test before production code.
- [ ] Keep changes within active work scope.
- [ ] Update API contract if endpoints change.
- [ ] Update frontend state contract if query keys or mutations change.
- [ ] Add or update tests with the implementation.
- [ ] Record accepted tradeoffs in Obsidian decision log.
- [ ] Record why the work changed in Obsidian build log based on the conversation.

## After Coding

- [ ] Production code changes include a related test change.
- [ ] All completed phase `Done Criteria` are checked with `[x]`.
- [ ] Changed production files are inside `Files To Touch`.
- [ ] Run relevant backend checks.
- [ ] Run relevant frontend checks.
- [ ] Archive completed work to Obsidian with conversation basis, summary, changed files, reason, verification, user reflection prompts, risks, next work, and Git commit hash/title.
- [ ] Update `harness/state/run-state.md` and `harness/state/execute-state.json` with final status.
- [ ] Move follow-up ideas back to `00-inbox.md`.
- [ ] Delete completed temporary work files only after Obsidian has the record.
