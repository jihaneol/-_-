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

### 2026-06-18 - Archive work history to Obsidian

Status: accepted  
Context: Project-local work files should stay clean and represent the current final state, while historical reasons and completed work still need to be recoverable.  
Decision: Keep current working contracts in the repository and archive modification reasons, completed work, and deleted temporary work context to Obsidian Idea Lab.  
Alternatives considered: keep all history in repository Markdown; rely only on Git history.  
Consequences: every meaningful planning/documentation change should update the Obsidian build log before local cleanup.  
Follow-up: use `work/08-obsidian-archive-policy.md` before deleting completed work files.

### 2026-06-18 - Use project folders inside Obsidian

Status: accepted  
Context: Multiple projects will use the same Obsidian Idea Lab areas, so flat files named by project can become hard to scan.  
Decision: Store card-service Obsidian records under project-specific folders: `07.Build Logs/card-service`, `09.Context Handoffs/01.Active Work/card-service`, and `04.Decisions/card-service`.  
Alternatives considered: keep Korean project-prefixed filenames in shared folders.  
Consequences: future project records should use the same `{project-name}/` folder pattern.  
Follow-up: update any automation or policy references to the nested paths.
