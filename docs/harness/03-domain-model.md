# Domain Model

## Bounded Contexts

| Context | Responsibility |
|---|---|
| Payment | Authorization, cancellation, payment status, idempotency |
| Ledger | Immutable money movement records |
| Settlement | Merchant daily settlement summaries |
| Reconciliation | Mismatch detection between ledger and settlement |

## Implemented Aggregates

| Aggregate | Root | Notes |
|---|---|---|
| Payment | `Payment` | Owns payment status, amount, merchant, order id, idempotency key |

## Target Aggregates

| Aggregate | Root | Notes |
|---|---|---|
| Settlement | `Settlement` | Owns daily merchant settlement result |
| ReconciliationReport | `ReconciliationReport` | Owns mismatch rows for one reconciliation run |

## Value Objects

| Value Object | Purpose |
|---|---|
| `Money` | Amount and currency validation |
| `MerchantId` | Merchant identity |
| `PaymentId` | Payment identity wrapping the entity `id` |
| `OrderId` | Partner order identity |
| `IdempotencyKey` | Duplicate request identity |

## Implemented Entities

| Entity | Purpose |
|---|---|
| Payment | Current payment state and request identity |

## Target Entities

| Entity | Purpose |
|---|---|
| Merchant | Payment recipient and settlement owner |
| PaymentLedger | Immutable money movement record |
| Settlement | Daily merchant settlement summary |
| ReconciliationReport | Detected differences between expected and actual financial records |
| PaymentEvent | Async event emitted after important payment changes |

## Payment Status

- `AUTHORIZED`
- `CANCELLED`
- `SETTLED`
- `FAILED`

## Invariants

- A payment request with the same idempotency key must not create two payments.
- A cancelled payment must not be cancelled twice.
- Ledger rows are append-only.
- Settlement must be reproducible from ledger rows.
- Reconciliation must detect missing, duplicated, or amount-mismatched records.

## Domain Design Rules

- Domain model and JPA entity are the same class in this project.
- Keep Spring repository, QueryDSL, adapter, and web details outside domain objects.
- JPA entity generation follows `rules/jpa-entity-rule.md`.
- Entity PK column is `id`; do not add a duplicate public id column unless a requirement appears.
- Use JPA field access and expose value objects through computed properties without `@get:Transient`.
- Put state transition rules inside the aggregate.
- Use domain events for important changes such as payment authorized and payment cancelled.
- Use application use cases to coordinate transactions and ports.
- Use adapters for database, broker, web, and batch details.
