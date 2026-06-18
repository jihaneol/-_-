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
- coupon order endpoint
- Mock external payment adapter with 300ms delay
- JPA payment persistence adapter
- In-memory coupon accrual adapter

## Module Boundaries

```text
domain      -> domain model and JPA entity combined, invariants
application -> use cases and in/out ports
bootstrap   -> Spring Boot runtime assembly and REST inbound adapters
batch       -> scheduled/batch inbound adapters
infra       -> JPA/QueryDSL database adapters
external    -> external-system/message adapters
```

Domain aggregate and JPA entity are intentionally the same model in this project. Narrow Spring Data `Repository<T, ID>` contracts may live in `application/provided`; persistence adapters, QueryDSL adapters, and database access implementations live in `infra`.

Dependency direction:

```text
bootstrap -> application + domain + batch + infra + external
batch     -> application -> domain
infra     -> application -> domain
external  -> application -> domain
```

## API

```http
POST /api/coupon-orders
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
