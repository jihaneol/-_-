# Domain Model

## Bounded Contexts

| Context | Responsibility |
|---|---|
| Payment | Authorization, cancellation, payment status, idempotency |
| Ledger | Immutable money movement records |
| Settlement | Merchant daily settlement summaries |
| Reconciliation | Mismatch detection between ledger and settlement |

## Aggregates

| Aggregate | Root | Notes |
|---|---|---|
| Payment | `Payment` | Owns payment status, amount, merchant, order id, idempotency key |
| Settlement | `Settlement` | Owns daily merchant settlement result |
| ReconciliationReport | `ReconciliationReport` | Owns mismatch rows for one reconciliation run |

## Value Objects

| Value Object | Purpose |
|---|---|
| `Money` | Amount and currency validation |
| `MerchantId` | Merchant identity |
| `PaymentId` | Payment identity |
| `OrderId` | Partner order identity |
| `IdempotencyKey` | Duplicate request identity |

## Entities

| Entity | Purpose |
|---|---|
| Merchant | Payment recipient and settlement owner |
| Payment | Current payment state and request identity |
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

- Keep domain objects free of Spring annotations.
- Put state transition rules inside the aggregate.
- Use domain events for important changes such as payment authorized and payment cancelled.
- Use application use cases to coordinate transactions and ports.
- Use adapters for database, broker, web, and batch details.
