# Backend Architecture Rule

이 프로젝트는 DDD, hexagonal architecture, multi-module boundary, CQRS를 함께 사용한다.

## Module Boundary

```text
domain      -> domain model and JPA entity combined, invariants, domain events, domain services
application -> command/query use cases and ports
bootstrap   -> Spring Boot runtime assembly and REST inbound adapters
batch       -> scheduled/batch inbound adapters
infra       -> JPA/QueryDSL database adapters
external    -> external-system/message adapters
```

Dependency direction:

```text
bootstrap -> application + domain + batch + infra + external
batch     -> application -> domain
infra     -> application -> domain
external  -> application -> domain
```

`domain` may contain JPA entity annotations because this project uses domain model and JPA entity as one model. Narrow Spring Data `Repository<T, ID>` contracts may live in `application/provided`. QueryDSL adapters and persistence adapters live in `infra`. `external` must not contain JPA, QueryDSL, or database repositories.

## Domain And Entity Rule

도메인 모델과 JPA entity를 따로 분리하지 않는다.

세부 entity 생성 규칙은 `rules/jpa-entity-rule.md`를 따른다.

규칙:

- aggregate/entity는 `domain` 모듈에 둔다.
- JPA annotation은 `domain` 모듈의 aggregate/entity에 둘 수 있다.
- PK는 `id`로 통일하고, 별도 public id 요구가 생기기 전에는 중복 식별자 컬럼을 만들지 않는다.
- JPA는 field access를 사용하고, domain value object 접근은 계산 property getter로 제공할 수 있다.
- `@get:Transient`는 사용하지 않는다.
- 좁은 Spring Data `Repository<T, ID>` 계약은 `application/provided`에 둘 수 있다.
- `JpaRepository`처럼 넓은 Spring Data interface는 사용하지 않는다.
- QueryDSL repository/adapter와 persistence adapter는 `infra` 모듈에 둔다.
- domain model은 DB 저장을 위해 필요한 최소 JPA annotation만 가진다.
- domain model은 Spring `@Service`, `@Component`, repository, QueryDSL, web DTO를 알면 안 된다.
- JPA 때문에 비즈니스 메서드가 흐려지면 도메인 규칙을 우선하고, persistence 편의 코드는 `infra` adapter에서 보완한다.

Package:

```text
modules/domain/src/main/kotlin/com/example/cardservice/domain
  payment.model        -> aggregate/entity, value object, enum
  payment.event        -> domain event
  domainservice.payment -> multiple-domain domain service
```

## Domain Service Rule

여러 도메인 aggregate를 합치거나 한 aggregate에 넣기 애매한 순수 도메인 규칙은 `domain/domainservice` 아래에 둔다.

규칙:

- `domainservice`는 순수 도메인 로직만 가진다.
- repository, port, adapter, external client를 직접 호출하지 않는다.
- application facade/service가 필요한 aggregate를 로드한 뒤 domainservice에 전달한다.
- 특정 도메인 전용이면 `domainservice/{domain}` 패키지에 둔다.

## CQRS Rule

조회와 변경은 use case, port, adapter 수준에서 분리한다.

| Side | Purpose | Rule |
|---|---|---|
| Command | 생성, 수정, 취소, 상태 변경 | 도메인 aggregate를 로드/생성하고 도메인 규칙을 거쳐 저장한다. |
| Query | 목록, 상세, 리포트, 대시보드, 검색 | 도메인 aggregate를 변경하지 않고 QueryDSL 기반 read model/projection으로 조회한다. |

## Command Side Rule

Command use case는 다음 흐름을 따른다.

```text
bootstrap web request
  -> command inbound port
  -> application command use case
  -> domain aggregate / domain service
  -> command outbound port
  -> persistence adapter saves state and append-only records
```

규칙:

- 생성/수정/취소는 반드시 도메인 모델을 통해 처리한다.
- 상태 전이, 금액 검증, 중복 방지, 취소 가능 여부 같은 비즈니스 규칙은 domain entity 또는 domainservice에 둔다.
- command use case는 transaction boundary를 소유한다.
- command persistence adapter는 aggregate 저장과 ledger/outbox 같은 쓰기 모델 저장을 담당한다.
- command side에서 화면 목록/검색용 DTO를 직접 만들지 않는다.
- transaction 세부 기준은 `rules/transaction-rule.md`를 따른다.
- 동시성/idempotency 기준은 `rules/concurrency-rule.md`를 따른다.
- event/outbox 기준은 `rules/event-publication-rule.md`를 따른다.
- DB schema/index 기준은 `rules/database-schema-rule.md`를 따른다.

## Query Side Rule

Query use case는 다음 흐름을 따른다.

```text
bootstrap web request
  -> query inbound port
  -> application query use case
  -> query outbound port
  -> QueryDSL read adapter
  -> projection / read model response
```

규칙:

- 조회는 QueryDSL을 기본 조회 기술로 사용한다.
- 목록, 검색, 운영자 대시보드, 정산/대사 리포트는 projection/read model을 반환한다.
- 개수가 커질 수 있는 목록 조회는 QueryDSL 기반 pagination을 기본값으로 한다.
- `All`, `listAll`, top-level 전체 배열 조회는 금지한다. 참조 데이터처럼 작고 상한이 명확한 경우만 예외로 허용하고, 그 상한을 문서화한다.
- 목록 query는 `page`, `size`, `sort` 또는 cursor 조건을 입력으로 받고, total count가 필요한 운영 화면은 `totalElements`, `totalPages`, `hasNext`를 함께 반환한다.
- page size는 API 기본값과 최대값을 둔다. 기본 `20`, 최대 `100`을 우선 기준으로 사용한다.
- query port는 Spring Data `Page`를 그대로 노출하지 않고 application query result/projection으로 변환한다.
- query side는 도메인 aggregate를 변경하지 않는다.
- query side에서 command aggregate를 억지로 조립하지 않는다.
- 단순 ID 단건 조회라도 화면/리포트 응답이면 query port를 통해 반환한다.

## Naming Rule Link

네이밍은 `rules/naming-rule.md`를 따른다.

Required ports:

```text
AuthorizePaymentUseCase
CancelPaymentUseCase
GetPaymentQueryUseCase
SearchPaymentsQueryUseCase
RunSettlementUseCase
GetSettlementReportQueryUseCase
```

Provided ports:

```text
SavePaymentPort
LoadPaymentPort
AppendPaymentLedgerPort
SearchPaymentQueryPort
LoadPaymentDetailQueryPort
LoadSettlementReportQueryPort
```

Infra adapters:

```text
JpaPaymentAdapter
QueryDslPaymentQueryAdapter
JpaLedgerAdapter
QueryDslSettlementReportQueryAdapter
```

External adapters:

```text
MockExternalPaymentAdapter
PaymentEventPublisherAdapter
```

변경 흐름에는 `Command` 접미사를 붙이지 않고, 조회 흐름에만 `Query`를 표시한다.

## Test Rule

- Command domain rules are tested in the `domain` module with BehaviorSpec.
- Command orchestration is tested in the `application` module with MockK ports.
- Query use cases are tested against query ports and projection contracts.
- QueryDSL adapters are tested in the `infra` module with integration tests when database behavior matters.
- Controller tests must verify that command endpoints call command use cases and query endpoints call query use cases.
- Transaction, concurrency, and outbox guarantees must have integration tests when database behavior matters.
- 공통 테스트 기준은 `rules/test-rule.md`를 따른다.

## Controller Rule Link

컨트롤러 코드는 `rules/controller-code-rule.md`를 따른다.

핵심 기준:

```text
bootstrap web adapter -> application request/result/response -> application use case
```

`request` 모델은 `application` 모듈의 별도 패키지에 둔다.
API 응답이 use case result와 1:1이면 별도 response DTO 없이 Result를 그대로 감싼다.
API 응답 모양이 result와 다를 때만 `response` 모델을 `application` 모듈의 별도 패키지에 둔다.

## Service Rule Link

서비스 코드는 `rules/service-code-rule.md`를 따른다.

핵심 기준:

```text
required use case -> application service -> domain -> provided port
```

## Port And Adapter Comment Rule

port와 adapter는 `rules/port-adapter-comment-rule.md`를 따른다.

핵심 기준:

```text
port    -> 어느 계층이 어떤 기능을 요청하는지 설명한다.
adapter -> 어떤 기술/외부 시스템으로 어떤 port를 구현하는지 설명한다.
```

## Error Message Rule Link

에러 메시지는 `rules/error-message-rule.md`를 따른다.
