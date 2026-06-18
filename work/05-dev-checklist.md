# Development Checklist

Use this checklist before and after implementing any feature.

## Before Coding

- [ ] The idea exists in `00-inbox.md` or `01-feature-candidates.md`.
- [ ] The feature is prioritized in `02-prioritized-roadmap.md`.
- [ ] `03-active-work.md` describes the current implementation contract.
- [ ] Backend impact is known.
- [ ] Frontend impact is known.
- [ ] Tests are listed.
- [ ] Excluded scope is explicit.

## During Coding

- [ ] Keep changes within active work scope.
- [ ] Update API contract if endpoints change.
- [ ] Update frontend state contract if query keys or mutations change.
- [ ] Add or update tests with the implementation.
- [ ] Record accepted tradeoffs in `04-decision-log.md`.
- [ ] Record why the work changed in Obsidian build log.

## After Coding

- [ ] Run relevant backend checks.
- [ ] Run relevant frontend checks.
- [ ] Archive completed work to Obsidian.
- [ ] Update `06-change-log.md` with only the latest local summary.
- [ ] Update `03-active-work.md` with the next current work or final status.
- [ ] Move follow-up ideas back to `00-inbox.md`.
- [ ] Delete completed temporary work files only after Obsidian has the record.
