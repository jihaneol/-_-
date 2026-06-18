# Agent Guide

This repository uses `$payment-service-harness` as the default project harness.

## Project Goal

Build `card-service` as a payment/card-service portfolio project that proves transaction correctness, concurrency handling, immutable records, settlement/reconciliation readiness, and an operator-facing UI.

## Default Stack

- Backend: Kotlin, Spring Boot, Java 21.
- Architecture: DDD with hexagonal architecture, Gradle module boundaries, and CQRS.
- Tests: Kotest BehaviorSpec, MockK, Spring Boot integration tests, Testcontainers when persistence behavior matters.
- Frontend: React, TypeScript, Vite, Feature-Sliced Design Lite, TanStack Query, React Hook Form, Zod, Vitest, React Testing Library, MSW.

## Working Rules

- Use `$payment-service-harness` for payment, card, ledger, settlement, reconciliation, concurrency, backend architecture, and frontend admin planning work.
- Use `$bilingual-commit-records` when committing work or updating Obsidian Git trace records.
- Commit only when the user explicitly asks.
- Keep detailed completed-work history in Obsidian, not in local `work/` history files.
- Keep `work/03-active-work.md` focused on one current implementation contract.
- Do not start implementation from a new idea until it has moved through the project work intake flow.

## Backend Module Boundaries

- `domain`: pure domain model, value objects, domain events, domain services; no Spring or adapter dependencies.
- `application`: inbound ports, outbound ports, and use cases; depends on `domain`.
- `controller`: REST/batch inbound adapters and web DTOs; depends on `application` and `domain`.
- `external`: persistence, message, and external-system outbound adapters; depends on `application` and `domain`.
- `bootstrap`: executable Spring Boot runtime assembly; depends on the runtime modules.

`controller` and `external` must not depend on each other directly.

## CQRS Rules

- Separate command and query use cases, ports, and adapters.
- Command side handles create/update/cancel/state-changing workflows through domain aggregates and domain rules.
- Query side handles list/detail/search/dashboard/report workflows through QueryDSL read adapters and projection/read models.
- Command persistence must not be used to build operational query screens directly.
- Query adapters must not mutate domain state.

See `rules/backend-architecture.md` for the detailed backend architecture rule.

## Work Intake Flow

Use this flow for new features and scope changes:

```text
work/00-inbox.md
  -> work/01-feature-candidates.md
  -> work/02-prioritized-roadmap.md
  -> work/03-active-work.md
```

Rules:

- `00-inbox.md`: raw ideas, user requests, and open questions.
- `01-feature-candidates.md`: shaped feature candidates with value, scope, risks, API impact, UI impact, and tests.
- `02-prioritized-roadmap.md`: current Now/Next/Later ordering.
- `03-active-work.md`: only the one work item currently being implemented.

If two tasks share one completion criterion and must be verified together, they may be one active work item. If they have different review, release, or verification criteria, keep them separate and sequence them in `02-prioritized-roadmap.md`.

See `rules/work-intake.md` for the detailed rule.

## Completion Flow

When a work item is complete:

1. Verify it with the relevant tests or document why it was not verified.
2. Archive the detailed work record to Obsidian under the `card-service` project folder.
3. Update Obsidian current work with the final state and next work.
4. Replace `work/03-active-work.md` with the next active work item.
5. If the user asks for a commit, commit with a Korean-first title and update Obsidian with only the short hash and title.

## Important Local Rules

- Git workflow: `rules/git-workflow.md`.
- Obsidian archive policy: `rules/obsidian-archive-policy.md`.
- Work intake and active-work rule: `rules/work-intake.md`.
- Backend architecture rule: `rules/backend-architecture.md`.
