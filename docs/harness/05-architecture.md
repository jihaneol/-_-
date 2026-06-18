# Architecture

## Style

Use DDD with hexagonal architecture and CQRS.

The domain model must not depend on Spring, JPA, Kafka/RabbitMQ, or web DTOs. External technologies live in adapters. Application use cases coordinate domain behavior through ports.

Command workflows create or change state through domain aggregates. Query workflows read projection/read models through QueryDSL and must not mutate domain state.

## Module Shape

```text
card-service
  domain
  application
  controller
  external
  bootstrap
```

| Module | Responsibility | Depends on |
|---|---|---|
| `domain` | Aggregates, value objects, domain events, domain services | none |
| `application` | Command/query inbound ports, outbound ports, use cases | `domain` |
| `controller` | REST and batch inbound adapters | `application`, `domain` |
| `external` | Persistence, message, external-system outbound adapters | `application`, `domain` |
| `bootstrap` | Spring Boot application, runtime configuration, resources, module assembly | all runtime modules |

## Package Shape

```text
domain/src/main/kotlin/com/example/cardservice
  payment.domain.model
  payment.domain.event
  payment.domain.service

application/src/main/kotlin/com/example/cardservice
  payment.application.port.in.command
  payment.application.port.in.query
  payment.application.port.out.command
  payment.application.port.out.query
  payment.application.usecase.command
  payment.application.usecase.query

controller/src/main/kotlin/com/example/cardservice
  payment.adapter.in.web
  payment.adapter.in.batch

external/src/main/kotlin/com/example/cardservice
  payment.adapter.out.persistence.command
  payment.adapter.out.persistence.query
  payment.adapter.out.message

bootstrap/src/main/kotlin/com/example/cardservice
  CardServiceApplication
```

## Hexagonal Flow

Command flow:

```text
HTTP / Batch inbound adapter
  -> command inbound port
  -> application command use case
  -> domain aggregate
  -> command outbound port
  -> persistence/message adapter saves state
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

## Payment Authorization Flow

```text
PaymentController
  -> AuthorizePaymentCommandUseCase
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

- `domain` depends on no project module and no Spring/JPA/web/broker library.
- `application` depends on `domain`; it owns all command/query inbound and outbound port interfaces.
- `controller` depends inward on `application` and `domain`; it owns web DTOs and request validation.
- `external` depends inward on `application` and `domain`; it owns command persistence, QueryDSL read adapters, broker, and external client details.
- `bootstrap` depends on every runtime module and is the only executable Spring Boot module.
- Web DTOs must not leak into use cases or domain objects.
- JPA entities must not be the domain model unless the tradeoff is documented.
- `controller` and `external` must not depend on each other directly.

## CQRS Rules

- Command use cases handle creation, modification, cancellation, and status changes.
- Command use cases must load/create domain aggregates, execute domain rules, then save through command outbound ports.
- Query use cases handle list, detail, search, dashboard, settlement report, and reconciliation report reads.
- Query use cases must read through query outbound ports backed by QueryDSL adapters.
- Query responses should be projections/read models, not mutable domain aggregates.
- Command adapters and query adapters are separated even if they read from the same database.
- QueryDSL belongs in the `external` module; `domain` and `application` must not depend on QueryDSL.

## Reliability Decisions

- Start with a unique constraint on `idempotency_key`.
- Keep ledger append-only.
- Prefer transactional outbox over publishing directly inside business transactions.
- Record failed event delivery for retry instead of hiding failures in logs.
