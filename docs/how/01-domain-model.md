# Domain Model

## Bounded Contexts

| Context | Responsibility |
|---|---|
| Payment | Authorization, cancellation, payment status, idempotency |
| Ledger | Immutable money movement records |
| Settlement | Merchant daily settlement summaries |
| Reconciliation | Mismatch detection between ledger and settlement |
| Commerce | Member, product, inventory, order, payment-to-coupon issuance |
| Coupon | Stamp coupon issuance and immutable coupon history |

## Implemented Aggregates

| Aggregate | Root | Notes |
|---|---|---|
| Payment | `Payment` | Owns payment status, amount, merchant, order id, idempotency key |

## Target Aggregates

| Aggregate | Root | Notes |
|---|---|---|
| Settlement | `Settlement` | Owns daily merchant settlement result |
| ReconciliationReport | `ReconciliationReport` | Owns mismatch rows for one reconciliation run |
| Member | `Member` | Owns member profile and soft-delete state |
| Product | `Product` | Owns product price, sale status, and soft-delete state |
| Inventory | `Inventory` | Owns stock quantity and stock adjustment guards |
| Order | `Order` | Owns order lines, total amount, cancellation, payment, and full refund state |
| Coupon | `Coupon` | Represents one issued stamp coupon for a member and source payment/order |

## Value Objects

| Value Object | Purpose |
|---|---|
| `Money` | Amount and currency validation |
| `MerchantId` | Merchant identity |
| `PaymentId` | Payment identity wrapping the entity `id` |
| `OrderId` | Partner order identity |
| `IdempotencyKey` | Duplicate request identity |
| `MemberId` | Member identity |
| `ProductId` | Product identity |
| `CouponId` | Issued coupon identity |

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
| CouponHistory | Append-only record for coupon issuance, refund reversal, and future exchange |

## Payment Status

- `AUTHORIZED`
- `CANCELLED`
- `SETTLED`
- `FAILED`
- `REFUNDED`

## Order Status

- `CREATED`
- `CANCELLED`
- `PAID`
- `REFUNDED`

## Coupon Policy

- The MVP uses stamp coupons, not point balance.
- A successful order payment issues one coupon stamp per `5000 KRW` paid amount.
- Example: `12000 KRW` issues two coupon stamps.
- Ten coupon stamps can later be exchanged for one coffee, but exchange is out of MVP.
- Coupons are managed as issued records. `CouponHistory` is append-only.
- Full refund marks issued coupon stamps as `VOIDED` and records reversal history. Partial refund is not allowed.

## Coupon Status

- `ISSUED`
- `VOIDED`
- `EXCHANGED`

## Invariants

- A payment request with the same idempotency key must not create two payments.
- A cancelled payment must not be cancelled twice.
- Ledger rows are append-only.
- Settlement must be reproducible from ledger rows.
- Reconciliation must detect missing, duplicated, or amount-mismatched records.
- A paid order cannot be cancelled; it can only be fully refunded.
- A pre-payment order can be cancelled.
- A full refund cannot be executed twice.
- A payment request with the same idempotency key must not issue duplicate coupons.
- A fully refunded order must void all coupon stamps issued from that order.
- Inventory cannot go below zero.
- Member, product, and order delete actions are soft deletes.
- Coupon and payment histories are append-only and cannot be deleted.

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
