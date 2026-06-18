# Backend Architecture Rule

이 프로젝트는 DDD, hexagonal architecture, multi-module boundary, CQRS를 함께 사용한다.

## Module Boundary

```text
domain      -> pure domain model and invariants
application -> command/query use cases and ports
controller  -> REST/batch inbound adapters
external    -> persistence/message/external outbound adapters
bootstrap   -> Spring Boot runtime assembly
```

Dependency direction:

```text
controller -> application -> domain
external   -> application -> domain
bootstrap  -> controller + external + application + domain
```

`controller` and `external` must not depend on each other directly.

## CQRS Rule

조회와 변경은 use case, port, adapter 수준에서 분리한다.

| Side | Purpose | Rule |
|---|---|---|
| Command | 생성, 수정, 취소, 상태 변경 | 도메인 aggregate를 로드/생성하고 도메인 규칙을 거쳐 저장한다. |
| Query | 목록, 상세, 리포트, 대시보드, 검색 | 도메인 aggregate를 변경하지 않고 QueryDSL 기반 read model/projection으로 조회한다. |

## Command Side Rule

Command use case는 다음 흐름을 따른다.

```text
controller request
  -> command inbound port
  -> application command use case
  -> domain aggregate / domain service
  -> command outbound port
  -> persistence adapter saves state and append-only records
```

규칙:

- 생성/수정/취소는 반드시 도메인 모델을 통해 처리한다.
- 상태 전이, 금액 검증, 중복 방지, 취소 가능 여부 같은 비즈니스 규칙은 도메인에 둔다.
- command use case는 transaction boundary를 소유한다.
- command persistence adapter는 aggregate 저장과 ledger/outbox 같은 쓰기 모델 저장을 담당한다.
- command side에서 화면 목록/검색용 DTO를 직접 만들지 않는다.

## Query Side Rule

Query use case는 다음 흐름을 따른다.

```text
controller request
  -> query inbound port
  -> application query use case
  -> query outbound port
  -> QueryDSL read adapter
  -> projection / read model response
```

규칙:

- 조회는 QueryDSL을 기본 조회 기술로 사용한다.
- 목록, 검색, 운영자 대시보드, 정산/대사 리포트는 projection/read model을 반환한다.
- query side는 도메인 aggregate를 변경하지 않는다.
- query side에서 command aggregate를 억지로 조립하지 않는다.
- 단순 ID 단건 조회라도 화면/리포트 응답이면 query port를 통해 반환한다.

## Naming Rule

Application inbound ports:

```text
AuthorizePaymentCommandUseCase
CancelPaymentCommandUseCase
GetPaymentQueryUseCase
SearchPaymentsQueryUseCase
RunSettlementCommandUseCase
GetSettlementReportQueryUseCase
```

Application outbound ports:

```text
SavePaymentPort
LoadPaymentForCommandPort
AppendPaymentLedgerPort
SearchPaymentQueryPort
LoadPaymentDetailQueryPort
LoadSettlementReportQueryPort
```

External adapters:

```text
JpaPaymentCommandAdapter
QueryDslPaymentQueryAdapter
JpaLedgerCommandAdapter
QueryDslSettlementReportQueryAdapter
```

## Test Rule

- Command domain rules are tested in the `domain` module with BehaviorSpec.
- Command orchestration is tested in the `application` module with MockK ports.
- Query use cases are tested against query ports and projection contracts.
- QueryDSL adapters are tested in the `external` module with integration tests when database behavior matters.
- Controller tests must verify that command endpoints call command use cases and query endpoints call query use cases.

