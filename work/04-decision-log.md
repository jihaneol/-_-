# Decision Log

Record product and engineering decisions here. Add new entries instead of overwriting history.

## Decision Template

```md
## YYYY-MM-DD - Decision Title

Status: proposed | accepted | superseded
Context:
Decision:
Alternatives considered:
Consequences:
Follow-up:
```

## Decisions

### 2026-06-18 - Use `work/` as feature planning source

Status: accepted  
Context: New features and ideas need a place to be collected before they affect implementation.  
Decision: Use the project-local `work/` folder for feature intake, candidate shaping, roadmap priority, active work, decisions, and change history.  
Alternatives considered: keep everything in `docs/harness`; use external notes only.  
Consequences: development starts from `work/03-active-work.md` when scope changes.  
Follow-up: keep `work/03-active-work.md` current before coding.
