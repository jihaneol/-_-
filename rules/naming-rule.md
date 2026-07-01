# Naming Rule

DDD/hexagonal/CQRS를 쓰지만 변경 작업 타입명에는 `Command` 접미사를 붙이지 않는다. 조회 흐름만 `Query`를 명시한다.

## Packages

```text
web/{domain}
application/{domain}/{request|response|required|provided}
domain/{domain}
domain/domainservice/{domain}
infra/{domain}
external/{domain}
batch/{domain}
```

## Application

```text
# command
AuthorizePaymentUseCase
AuthorizePaymentService
AuthorizePaymentRequest
AuthorizePaymentResponse

CouponOrderUseCase
CouponOrderFacade
CreateCouponOrderRequest
CreateCouponOrderResponse

# query
GetPaymentQueryUseCase
GetPaymentQueryService
GetPaymentQuery
PaymentDetailResponse
PaymentPageResponse
```

- use case interface: `required`
- application이 필요로 하는 port: `provided`
- implementation: 도메인 루트 패키지의 `{Action}Service`, `{Feature}Facade`
- request/response/approval 타입: port 파일에 넣지 않고 `{Action}Requests.kt`, `{Action}Responses.kt`
- request 타입은 하나만 쓰고, 외부에서 받으면 안 되는 값은 public 생성 경로에 열지 않는다.
- path/header/auth/server-derived 값은 controller에서 `copy(...).also { ... }`로 직접 채운다.
- API 응답 모양이 use case response와 다를 때만 adapter 전용 response DTO를 둔다.
- page 입력: 공통 `Pagination` + 대상 id 별도 파라미터

## Ports And Adapters

```text
# required ports
AuthorizePaymentUseCase
CancelPaymentUseCase
CouponOrderUseCase
GetPaymentQueryUseCase
SearchPaymentsQueryUseCase

# provided ports
SavePaymentPort
LoadPaymentPort
AppendPaymentLedgerPort
SearchPaymentQueryPort
LoadPaymentDetailQueryPort
PublishPaymentEventPort
ExternalPaymentPort
AccrueCouponPort

# adapters
JpaPaymentAdapter
QueryDslPaymentQueryAdapter
JpaLedgerAdapter
QueryDslSettlementReportQueryAdapter
MockExternalPaymentAdapter
PaymentEventPublisherAdapter
```

## Controllers

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

- 단일 변경 기능: `{Feature}Controller`
- 같은 도메인 안에서도 운영 책임이 다르면 기능별 controller
- 조회 분리: `{Domain}QueryController`
- `ApiController` 접미사 금지

## Avoid And Use

```text
# Avoid                                # Use
AuthorizePaymentCommandUseCase          AuthorizePaymentUseCase
AuthorizePaymentCommandService          AuthorizePaymentService
AuthorizePaymentCommand                 AuthorizePaymentRequest
JpaPaymentCommandAdapter                JpaPaymentAdapter
LoadPaymentForCommandPort               LoadPaymentPort
CommerceService                         MemberService / ProductService / OrderService
CommerceRequests.kt                     MemberRequests.kt / OrderRequests.kt
```
