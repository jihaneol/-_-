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
- `.codex/agents/*.md` files are reviewer role prompts. They do not run automatically; spawn a sub-agent explicitly when the user asks for reviewer/sub-agent validation or when a milestone review is needed.

## Backend Module Boundaries

- `modules/domain`: domain model and JPA entity combined, value objects, domain events, and cross-domain domain services; no Spring repository or adapter dependencies.
- `modules/application`: domain-root use case services/models, inbound ports, outbound ports, and API request/response models; depends on `domain`.
- `modules/bootstrap`: executable Spring Boot runtime assembly and REST inbound adapters; depends on `application`, `domain`, `batch`, `infra`, and `external`.
- `modules/batch`: scheduled/batch inbound adapters for settlement, reconciliation, and operational jobs; depends on `application` and `domain`.
- `modules/infra`: JPA/QueryDSL database adapters; depends on `application` and `domain`.
- `modules/external`: external-system/message adapters; depends on `application` and `domain`.

`bootstrap`, `batch`, `infra`, and `external` Gradle module names remain unchanged, but their folders live under `modules/`. Adapter responsibilities stay separate. JPA entity annotations may live in `domain`; narrow Spring Data `Repository<T, ID>` contracts may live in `application/provided`; QueryDSL adapters and persistence adapters live in `infra`.

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
- Controller code rule: `rules/controller-code-rule.md`.
- Comment preservation rule: `rules/comment-preservation-rule.md`.
- Naming rule: `rules/naming-rule.md`.
- Enum code rule: `rules/enum-code-rule.md`.
- JPA entity rule: `rules/jpa-entity-rule.md`.
- Port/adapter comment rule: `rules/port-adapter-comment-rule.md`.
- Service code rule: `rules/service-code-rule.md`.
- Error message rule: `rules/error-message-rule.md`.
