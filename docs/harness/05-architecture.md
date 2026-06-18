# Architecture

## Style

Use DDD with hexagonal architecture.

The domain model must not depend on Spring, JPA, Kafka/RabbitMQ, or web DTOs. External technologies live in adapters. Application use cases coordinate domain behavior through ports.

## Package Shape

```text
com.example.cardservice
  payment
    domain
      model
      event
      service
    application
      port.in
      port.out
      usecase
    adapter.in.web
    adapter.in.batch
    adapter.out.persistence
    adapter.out.message
  settlement
    domain
    application
    adapter
  reconciliation
    domain
    application
    adapter
```

## Hexagonal Flow

```text
HTTP / Batch inbound adapter
  -> inbound port
  -> application use case
  -> domain aggregate
  -> outbound port
  -> persistence/message adapter
```

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

- `domain` depends on nothing outside the domain module/package.
- `application` depends on `domain` and port interfaces.
- `adapter` depends inward on `application` and `domain`.
- Web DTOs must not leak into use cases or domain objects.
- JPA entities must not be the domain model unless the tradeoff is documented.

## Reliability Decisions

- Start with a unique constraint on `idempotency_key`.
- Keep ledger append-only.
- Prefer transactional outbox over publishing directly inside business transactions.
- Record failed event delivery for retry instead of hiding failures in logs.
