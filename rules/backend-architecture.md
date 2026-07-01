# Backend Architecture Rule

이 프로젝트는 DDD, hexagonal architecture, multi-module boundary, CQRS를 함께 사용한다.

## Modules

```text
domain      -> domain model + JPA entity, invariants, domain events/services
application -> command/query use cases, required/provided ports
admin-api   -> admin runtime assembly, REST inbound adapters
shop-api    -> shop runtime assembly, REST inbound adapters
batch       -> scheduled/batch inbound adapters
infra       -> JPA/QueryDSL database adapters
external    -> external-system/message adapters
```

```text
admin-api -> application + domain + batch + infra + external
shop-api  -> application + domain + infra + external
batch     -> application -> domain
infra     -> application -> domain
external  -> application -> domain
```

`external` must not contain JPA, QueryDSL, or database repositories.

## Domain

```text
domain/order                 -> order aggregate/entity, enum
domain/product               -> product aggregate/entity, enum
domain/inventory             -> inventory aggregate/entity
domain/member                -> member aggregate/entity
domain/coupon                -> coupon aggregate/entity and history
domain/outbox                -> outbox entity
domain/payment               -> payment aggregate/value object
domain/payment.event         -> payment domain event
domain/domainservice/payment -> multiple-domain domain service
```

```kotlin
@Entity
@Access(AccessType.FIELD)
class Payment protected constructor() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Long = 0L
        protected set

    fun refund() {
        require(statusValue == PaymentStatus.AUTHORIZED) { "이미 환불된 결제입니다." }
        statusValue = PaymentStatus.REFUNDED
    }
}
```

- 도메인 모델과 JPA entity를 분리하지 않는다.
- JPA annotation은 `domain` aggregate/entity에 둘 수 있다.
- PK는 `id`; field access; 계산 property로 value object를 제공한다.
- `@get:Transient`, `JpaRepository`, QueryDSL import, web DTO import는 domain에 두지 않는다.
- 세부 기준: `rules/jpa-entity-rule.md`

## Domain Service

```text
application service/facade -> aggregate load/create -> domain/domainservice/{domain} -> save via port
```

Domain service는 순수 도메인 로직만 가진다. repository, port, adapter, external client를 직접 호출하지 않는다.

## CQRS

| Side | Flow |
|---|---|
| Command | request -> required use case -> application service/facade -> domain -> provided port -> persistence |
| Query | params -> query use case -> query service -> query port -> repository/QueryDSL adapter -> projection |

```kotlin
// command
class OrderService(...) : OrderUseCase
class OrderPaymentFacade(...) : OrderPaymentUseCase

// query
class ProductQueryService(...) : ProductQueryUseCase
class QueryDslCouponQueryAdapter(...) : CouponQueryPort
```

Command:

- 생성/수정/취소는 도메인 모델을 통해 처리한다.
- 상태 전이, 금액 검증, 중복 방지, 취소 가능 여부는 domain entity/domainservice에 둔다.
- transaction boundary는 command service/facade가 소유한다.
- 화면 목록/검색용 DTO를 command side에서 만들지 않는다.

Query:

- 단순 단일 aggregate 목록/상세: 좁은 Spring Data repository + `Pageable` 내부 사용 가능
- join, 동적 조건, projection, 대시보드, 정산/대사, count 최적화: QueryDSL
- entity를 그대로 노출하지 않고 projection/read model로 반환한다.
- 무제한 `All/listAll` top-level 배열 조회 금지.
- `Pageable`/`Page`는 API/use case 경계 밖으로 노출하지 않는다.

## Naming

```text
AuthorizePaymentUseCase
CancelPaymentUseCase
GetPaymentQueryUseCase
SearchPaymentsQueryUseCase

SavePaymentPort
LoadPaymentPort
SearchPaymentQueryPort

JpaPaymentAdapter
QueryDslPaymentQueryAdapter
MockExternalPaymentAdapter
PaymentEventPublisherAdapter
```

변경 흐름에는 `Command` 접미사를 붙이지 않고, 조회 흐름에만 `Query`를 표시한다. 세부 기준: `rules/naming-rule.md`.

## Controller And Service

```text
web adapter -> application request/response -> application use case
required use case -> application service/facade -> domain -> provided port
```

- Controller: `rules/controller-code-rule.md`
- Service/facade: `rules/service-code-rule.md`
- Port/adapter KDoc: `rules/port-adapter-comment-rule.md`

## Tests

```text
domain      -> BehaviorSpec domain rules
application -> MockK port orchestration
infra       -> QueryDSL/JPA integration when DB behavior matters
api         -> controller command/query routing and error response
```

Transaction, concurrency, and outbox guarantees need integration tests when database behavior matters.

## References

- `rules/querydsl-rule.md`
- `rules/transaction-rule.md`
- `rules/concurrency-rule.md`
- `rules/event-publication-rule.md`
- `rules/database-schema-rule.md`
- `rules/test-rule.md`
- `rules/error-message-rule.md`
