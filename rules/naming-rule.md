# Naming Rule

이 프로젝트는 DDD, hexagonal architecture, CQRS를 사용하지만 타입명에는 변경 작업을 뜻하는 `Command` 접미사를 붙이지 않는다.

조회 흐름만 `Query`를 명시한다.

## Package Rule

```text
modules/bootstrap/src/main/kotlin/com/example/cardservice/web
  common
  {domain}

modules/application/src/main/kotlin/com/example/cardservice/application
  common
  {domain}
    request
    response
    required
    provided
    {Action}Service.kt
    {Feature}Facade.kt
    {Action}Models.kt

modules/domain/src/main/kotlin/com/example/cardservice/domain
  {domain}
  domainservice/{domain}

modules/infra/src/main/kotlin/com/example/cardservice/infra
  {domain}

modules/external/src/main/kotlin/com/example/cardservice/external
  {domain}

modules/batch/src/main/kotlin/com/example/cardservice/batch
  {domain}
```

## Application Naming

변경 흐름:

```text
AuthorizePaymentUseCase
AuthorizePaymentService
AuthorizePaymentInput
AuthorizePaymentResult

CancelPaymentUseCase
CancelPaymentService
CancelPaymentInput
CancelPaymentResult

CouponOrderUseCase
CouponOrderFacade
CreateCouponOrderInput
CreateCouponOrderResult
```

조회 흐름:

```text
GetPaymentQueryUseCase
GetPaymentQueryService
GetPaymentQuery
PaymentDetailQueryResult

SearchPaymentsQueryUseCase
SearchPaymentsQueryService
SearchPaymentsQuery
SearchPaymentsQueryResult

SearchPaymentsPageQuery
PaymentPageResult
PaymentPageResponse
```

규칙:

- 변경 흐름에는 `Command`를 붙이지 않는다.
- 조회 흐름에는 `Query`를 붙인다.
- use case interface는 `required` 패키지에 둔다.
- infra/external/batch/application 구현체가 필요로 하는 port는 `provided` 패키지에 둔다.
- use case 구현체는 도메인 루트 패키지에 둔다.
- 단일 도메인 규칙 실행은 `{Action}Service`를 사용하고, 여러 use case/port를 조율하는 흐름은 `{Feature}Facade`를 사용한다.
- use case/port input, result, request, approval 모델은 port 인터페이스 파일에 같이 두지 않고 도메인 루트의 `{Action}Models.kt` 파일로 분리한다.
- API request는 `request` 패키지에 둔다.
- API 응답 모양이 use case result와 다를 때만 response DTO를 만들고 `response` 패키지에 둔다.
- Result와 API 응답이 1:1이면 별도 response DTO와 단순 복사용 `toResponse()`를 만들지 않고 Result를 그대로 `ApiResponse<T>`에 담는다.
- paginated 목록 조회는 `{Feature}PageQuery`, `{Feature}PageResult`, `{Feature}PageResponse` 이름을 사용한다.

## Port Naming

Controller가 호출하는 required port:

```text
AuthorizePaymentUseCase
CancelPaymentUseCase
CouponOrderUseCase
GetPaymentQueryUseCase
SearchPaymentsQueryUseCase
```

Application이 필요로 하는 provided port:

```text
SavePaymentPort
LoadPaymentPort
AppendPaymentLedgerPort
SearchPaymentQueryPort
LoadPaymentDetailQueryPort
PublishPaymentEventPort
ExternalPaymentPort
AccrueCouponPort
```

## Adapter Naming

```text
JpaPaymentAdapter
QueryDslPaymentQueryAdapter
JpaLedgerAdapter
QueryDslSettlementReportQueryAdapter
MockExternalPaymentAdapter
PaymentEventPublisherAdapter
```

규칙:

- DB 쓰기 adapter는 `Jpa{Domain}Adapter`로 둔다.
- QueryDSL 조회 adapter는 `QueryDsl{Feature}QueryAdapter`로 둔다.
- 외부 시스템 mock은 `Mock{ExternalSystem}Adapter`로 둔다.
- 메시지 발행은 `{Event}PublisherAdapter`로 둔다.

## Controller Naming

```text
PaymentController
PaymentQueryController
CouponOrderController
MemberController
ProductController
InventoryController
OrderController
OrderPaymentController
CouponController
```

규칙:

- 단일 변경 기능이면 `{Feature}Controller`를 사용한다.
- 한 도메인 안에서도 운영 책임이 다르면 기능별 controller 이름을 사용한다.
- 조회가 분리되면 `{Domain}QueryController`를 사용한다.
- `ApiController` 접미사는 사용하지 않는다.

## Forbidden Names

```text
AuthorizePaymentCommandUseCase
AuthorizePaymentCommandService
AuthorizePaymentCommand
JpaPaymentCommandAdapter
LoadPaymentForCommandPort
```

대신 사용:

```text
AuthorizePaymentUseCase
AuthorizePaymentService
AuthorizePaymentInput
CouponOrderUseCase
CouponOrderFacade
JpaPaymentAdapter
LoadPaymentPort
```
