# Architecture

Visual diagrams: `docs/harness/10-architecture-diagram.md`

## Style

Use DDD with hexagonal architecture and CQRS.

The domain model and JPA entity are intentionally combined in this project. Domain may use JPA annotations, but QueryDSL, persistence adapters, external clients, Kafka/RabbitMQ, and web DTOs stay outside the domain model. Narrow Spring Data `Repository<T, ID>` contracts may live in `application/provided`. Application use cases coordinate domain behavior through ports.

Command workflows create or change state through domain aggregates. Query workflows read projection/read models through QueryDSL and must not mutate domain state.

## Module Shape

```text
card-service
  modules/domain
  modules/application
  modules/bootstrap
  modules/batch
  modules/infra
  modules/external
```

| Module | Responsibility | Depends on |
|---|---|---|
| `domain` | Aggregates/JPA entities, value objects, domain events, cross-domain domain services | none |
| `application` | Command/query inbound ports, outbound ports, use cases | `domain` |
| `bootstrap` | Spring Boot runtime, REST inbound adapters, global web error handling | `application`, `domain`, `batch`, `infra`, `external` |
| `batch` | Scheduled/batch inbound adapters for settlement, reconciliation, and operational jobs | `application`, `domain` |
| `infra` | JPA command persistence and QueryDSL read adapters | `application`, `domain` |
| `external` | External-system and message adapters | `application`, `domain` |

## Package Shape

```text
modules/domain/src/main/kotlin/com/example/cardservice/domain
  payment.model
  payment.event
  domainservice.payment

modules/application/src/main/kotlin/com/example/cardservice/application
  common
  payment.request
  payment.response
  payment.required
  payment.provided
  payment

modules/bootstrap/src/main/kotlin/com/example/cardservice
  CardServiceApplication
  web.common
  web.payment

modules/batch/src/main/kotlin/com/example/cardservice/batch
  payment

modules/infra/src/main/kotlin/com/example/cardservice/infra
  payment.persistence
  payment.query

modules/external/src/main/kotlin/com/example/cardservice/external
  payment.message
```

## Hexagonal Flow

Change flow:

```text
HTTP inbound adapter
  -> required use case
  -> application service
  -> domain aggregate/JPA entity
  -> provided port
  -> infra persistence adapter saves state
  -> external/message adapter publishes or calls external systems when needed
```

Query flow:

```text
HTTP / Batch inbound adapter
  -> query inbound port
  -> application query use case
  -> query outbound port
  -> QueryDSL read adapter
  -> projection / read model response
```

Current implementation note:

- The first implemented runtime flow is `POST /api/coupon-orders`.
- Query use cases and QueryDSL read adapters are rules for the next read-side work item and are not implemented yet.
- Ledger, settlement, reconciliation, and outbox flows are target architecture until their active work items are selected.

## Payment Authorization Flow

```text
PaymentController
  -> AuthorizePaymentUseCase
  -> Payment.authorize(...)
  -> SavePaymentPort
  -> AppendLedgerPort
  -> PublishPaymentEventPort or SaveOutboxEventPort
```

## Batch Flow

```text
SettlementJob inbound adapter
  -> RunDailySettlementUseCase
  -> LoadLedgerPort
  -> Settlement aggregate
  -> SaveSettlementPort
```

## Async Stretch Shape

```text
Application use case
  -> writes Payment + PaymentLedger + OutboxEvent in one transaction
  -> OutboxPublisher adapter publishes to Kafka/RabbitMQ
  -> PaymentEventConsumer adapter handles retryable downstream work
```

## Dependency Rules

- `domain` depends on no project module and no Spring/web/broker library.
- Domain aggregate and JPA entity are the same class in this project.
- JPA annotations may live in `domain`.
- Narrow Spring Data `Repository<T, ID>` contracts may live in `application/provided`.
- Persistence adapters and QueryDSL adapters live in `infra`.
- JPA entity generation follows `rules/jpa-entity-rule.md`.
- Entity PK column is `id`; duplicate public id columns are not added until there is a clear requirement.
- Domain value object accessors may be computed properties because JPA field access is explicit; do not add `@get:Transient`.
- Cross-domain pure domain logic lives under `modules/domain/src/main/kotlin/com/example/cardservice/domain/domainservice`.
- `application` depends on `domain`; it owns required/provided ports and use case services.
- `application` owns request/response models used by inbound adapters.
- `bootstrap` owns HTTP routing, request validation wiring, Swagger/OpenAPI annotations, global HTTP error handling, and runtime assembly.
- `batch` owns scheduled/batch inbound adapters and delegates work to application use cases.
- `infra` depends inward on `application` and `domain`; it owns JPA command persistence and QueryDSL read adapters.
- `external` depends inward on `application` and `domain`; it owns external-system clients, message adapters, and broker details.
- `bootstrap` is the only executable Spring Boot module.
- Web DTOs must not leak into use cases or domain objects.
- Controller request/response models live under `application/{domain}/request` and `application/{domain}/response`.
- JPA entities are the domain model in this project. Keep QueryDSL and persistence adapter technology in `infra`.
- `infra` is the only DB access module. `external` must not contain JPA, QueryDSL, or database repositories.

## CQRS Rules

- Change use cases handle creation, modification, cancellation, and status changes.
- Change use cases must load/create domain aggregates, execute domain rules, then save through provided ports.
- Query use cases handle list, detail, search, dashboard, settlement report, and reconciliation report reads.
- Query use cases must read through query outbound ports backed by QueryDSL adapters.
- Query responses should be projections/read models, not mutable domain aggregates.
- Change persistence adapters and query adapters are separated inside `infra` even if they read from the same database.
- QueryDSL belongs in the `infra` module; `domain`, `application`, `bootstrap`, `batch`, and `external` must not depend on QueryDSL.

## Reliability Decisions

- Start with a unique constraint on `idempotency_key`.
- Keep ledger append-only.
- Prefer transactional outbox over publishing directly inside business transactions.
- Record failed event delivery for retry instead of hiding failures in logs.
