# card-service

Kotlin Spring Boot 기반 카드 결제 서비스 포트폴리오 프로젝트입니다.

## Current Scope

- Backend scaffold
- DDD and hexagonal multi-module structure
- MySQL local environment
- Flyway baseline migration
- Kotest BehaviorSpec
- MockK dependency
- Testcontainers MySQL integration test setup
- Initial `Payment` aggregate skeleton
- Starbucks coupon order endpoint
- Mock external payment adapter with 300ms delay
- In-memory payment and coupon accrual adapters

## Module Boundaries

```text
domain      -> pure domain model and invariants
application -> use cases and in/out ports
controller  -> REST and batch inbound adapters
external    -> persistence/message/external outbound adapters
bootstrap   -> Spring Boot runtime assembly
```

Dependency direction:

```text
controller -> application -> domain
external   -> application -> domain
bootstrap  -> controller + external + application + domain
```

## API

```http
POST /api/starbucks-coupon-orders
```

```json
{
  "customerId": "customer-1",
  "orderId": "order-1",
  "idempotencyKey": "idem-1",
  "quantity": 2
}
```

## Commands

Use Java 21 for local development.

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

```bash
./gradlew test
```

```bash
./gradlew :bootstrap:bootRun
```

```bash
docker compose up -d mysql
```

## Project Notes

- Current work contract: `work/03-active-work.md`
- Backend harness: `docs/harness`
- Frontend harness: `docs/frontend-harness`
- Completed work history is archived in Obsidian.
