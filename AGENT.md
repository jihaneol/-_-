# Agent Constitution

This file is the operating constitution for Codex in this repository. It defines the order of authority and the loop for long-running implementation.

## Authority Order

1. User request in the current conversation.
2. `AGENT.md`.
3. `.codex/skills/*.md`.
4. `docs/`.
5. `harness/phases/*.md`.
6. `rules/*.md`.

If two files conflict, follow the higher authority and update the lower one when the task is about structure or process.

## Big Flow

```text
.codex/skills
  -> docs
  -> discuss with user
  -> harness/phases
  -> scripts/execute.py
  -> hooks
  -> review
```

## Folder Roles

| Path | Role |
|---|---|
| `.codex/skills/harness.md` | How to turn docs and discussion into implementation phases |
| `.codex/skills/review.md` | How to review completed phases |
| `docs/` | Project brain: what, how, why, and UI expectations |
| `AGENT.md` | Constitution and authority order |
| `scripts/hooks/` | Automatic validation scripts |
| `scripts/execute.py` | Phase runner and state manager |
| `harness/phases/` | Ordered implementation phase files |
| `harness/archive/` | Completed phase files, moved here by `scripts/execute.py complete` |
| `harness/state/` | Execution state and run handoff |
| `rules/` | Detailed coding rules |

## Rule Authoring

`rules/` 문서를 새로 만들거나 수정할 때는 `rules/rule-authoring-rule.md`를 따른다.

핵심 기준:

- 과하게 포괄적인 규칙보다 작고 적용 가능한 규칙을 만든다.
- 기본값, 금지할 것, 테스트 기준, trade-off와 문제점만 남긴다.
- 확장 가능성을 열어두되, 현재 필요 없는 추상화나 도구를 먼저 강제하지 않는다.

## Required Working Loop

0. After context compression, resume, or a fresh Codex handoff, run `python3 scripts/execute.py resume` before planning or editing.
1. Read all relevant `docs/` files before planning implementation.
2. Discuss unclear product, architecture, or UI decisions with the user before creating new phases.
3. Split implementation into small ordered phase files under `harness/phases/`.
4. Use `scripts/execute.py` to lint, inspect, start, checkpoint, validate, review, complete, archive, sync, and resume phases.
5. Run `scripts/hooks/validate.sh` before marking a phase complete.
6. Let `scripts/execute.py` update `harness/state/run-state.md`, `.codex/context/active-handoff.md`, and Obsidian handoff records after each phase event.
7. Use `.codex/skills/review.md` after each meaningful phase or milestone.

## Project Goal

Build `card-service` as a payment/card-service portfolio project that proves transaction correctness, concurrency handling, immutable records, settlement/reconciliation readiness, and an operator-facing UI.

## Default Stack

- Backend: Kotlin, Spring Boot, Java 21.
- Architecture: DDD, hexagonal architecture, Gradle module boundaries, and CQRS.
- Tests: Kotest BehaviorSpec, MockK, Spring Boot integration tests, Testcontainers when persistence behavior matters.
- Frontend: React, TypeScript, Vite, Feature-Sliced Design Lite, TanStack Query, React Hook Form, Zod, Vitest, React Testing Library, MSW.

## Non-Negotiables

- Do not implement from scattered ideas. Implementation starts from an approved phase file.
- TDD is mandatory for behavior changes. Write or update the failing test first, then change production code.
- If a feature or fix has no relevant test, `scripts/hooks/enforce_tdd.py` must fail and the work must stop until the test is added.
- Run shell commands through `python3 scripts/execute.py run -- ...` or `python3 scripts/execute.py validate` during long-running work so dangerous command and circuit-breaker guards apply.
- Keep `docs/` as the project brain, not as a work log.
- Keep detailed completed-work history in Obsidian, not in growing local logs.
- Use `scripts/execute.py` for phase state changes so Obsidian and local handoff files stay synchronized.
- On any resumed or compressed context, run `python3 scripts/execute.py resume` before relying on memory.
- Start phases from a clean worktree unless there is a deliberate `python3 scripts/execute.py start --allow-dirty` reason.
- Use `python3 scripts/execute.py checkpoint "message"` before risky edits, long pauses, or context-heavy changes.
- In long-running implementation mode, completed phases are auto-committed by `scripts/execute.py complete` after all gates pass.
- Completed phase files leave `harness/phases/` and move to `harness/archive/YYYY-MM-DD/`; active phases only stay in `harness/phases/`.
- Preserve user changes. Never revert unrelated files.
- Prefer finished, tested transactional slices over broad shallow scope.

## Completion Rule

A phase is complete only when:

- Its `## Test First` section was followed.
- Its done criteria are satisfied.
- Its `Done Criteria` checkboxes are marked `[x]`.
- Production source changes are within `Files To Touch`.
- The phase did not auto-commit pre-existing dirty files.
- The relevant validation hook passes or the reason it cannot run is documented.
- Dangerous command guard and circuit breaker did not block the validation path.
- `harness/state/run-state.md` records what changed, what was verified, and the next phase.
- Obsidian active work and today build-log detail have been updated by `scripts/execute.py`.
- Review-required phases have moved through `review_required` and have a review note before completion.
- The completed phase file has been archived by `scripts/execute.py`.
- The completed phase is auto-committed unless the user explicitly disables auto commit.
