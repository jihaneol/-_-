# Agent Review Loop

Use these reviewers as separate passes after milestones.

Reviewer files under `.codex/agents/*.md` are role prompts, not automatic background processes. A reviewer only runs when Codex explicitly spawns a sub-agent with one of these prompts or with an equivalent review request.

Run rule:

- Give the reviewer a narrow scope and concrete files.
- Ask for findings first, ordered by severity.
- Ask for file paths and concrete fixes.
- Do not ask the reviewer to confirm the answer you already want.
- After the reviewer returns, record important findings in Obsidian.

## payment-domain-reviewer

When to run:

- After authorization and cancellation are implemented.
- After settlement and reconciliation are implemented.

Prompt:

```text
Review this payment backend for domain correctness. Focus on payment authorization, cancellation, ledger immutability, settlement, reconciliation, and failure cases. Return findings ordered by severity with file references and concrete fixes.
```

## database-performance-reviewer

When to run:

- After `sql/` table/index schema files exist.
- After merchant/date query and settlement query exist.

Prompt:

```text
Review the MySQL schema and query patterns for a payment backend. Focus on constraints, indexes, transaction boundaries, batch queries, and data growth risks. Return findings and missing measurements.
```

## concurrency-reliability-reviewer

When to run:

- After idempotency and duplicate request handling exist.
- Before adding async event publishing.

Prompt:

```text
Review concurrency and reliability risks. Focus on duplicate payment requests, idempotency, locking, retries, outbox/event delivery, consumer reprocessing, and crash consistency. Include test scenarios for uncovered risks.
```

## portfolio-readiness-reviewer

When to run:

- On Day 7 before final polish.

Prompt:

```text
Review this project as hiring evidence for a payment server role. Check whether the README, workflow docs, tests, and implementation prove the required skills. Identify weak claims, missing proof, and unclear tradeoffs.
```
