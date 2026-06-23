# Service And Facade Code Rule

service/facade는 application layer의 use case 구현체다. 컨트롤러, DB, 외부 시스템 세부 기술을 알지 않고 required port를 구현하며 provided port를 호출한다.

긴 복사용 예시는 `docs/how/references/service-code-examples.md`를 필요할 때만 읽는다.

## Package Rule

```text
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/{Action}Service.kt
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/{Feature}Facade.kt
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/{Action}Models.kt
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/required
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/provided
```

## Service And Facade Rule

- service/facade는 `required` use case interface를 구현한다.
- service/facade 타입 선언 바로 위에는 어떤 use case 흐름을 조율하는지 한글 KDoc으로 설명한다.
- service/facade는 별도 `service` 폴더를 만들지 않고 도메인 루트 패키지에 둔다.
- 단일 도메인 규칙 실행은 `{Action}Service`를 사용한다.
- 여러 use case와 provided port를 묶는 조율 흐름은 `{Feature}Facade`를 사용한다.
- 회원, 상품, 재고, 주문, 결제, 쿠폰처럼 책임이 다른 기능은 하나의 service에 합치지 않는다.
- CRUD성 기능도 aggregate 책임이 다르면 `MemberService`, `ProductService`, `InventoryService`, `OrderService`처럼 분리한다.
- 결제처럼 여러 aggregate와 port를 조율하는 흐름은 `OrderPaymentFacade`처럼 별도 facade로 분리한다.
- service/facade는 domain model을 생성/로드하고 domain 규칙을 실행한다.
- service/facade는 DB, 외부 API, 메시징을 직접 호출하지 않고 `provided` port를 호출한다.
- service/facade는 controller response wrapper인 `ApiResponse<T>`를 알면 안 된다.
- service/facade는 HTTP status, Swagger, validation annotation을 알면 안 된다.
- transaction boundary는 변경 use case service/facade가 소유한다. 실제 `@Transactional` 적용 위치는 service/facade다.
- 하나의 service/facade 클래스 안에 변경 transaction과 조회 `readOnly` transaction을 같이 두지 않는다. 변경 흐름은 `{Feature}Service`/`{Feature}Facade`, 조회 흐름은 `{Feature}QueryService`/`{Feature}QueryFacade`로 분리한다.
- save/update/delete를 수행하는 service/facade는 query use case interface를 구현하지 않는다.
- `@Transactional(readOnly = true)`가 붙은 query service/facade는 `save`, `saveAll`, domain state change method를 호출하지 않는다.
- transaction 세부 기준은 `rules/transaction-rule.md`를 따른다.
- 동시성/idempotency가 있는 흐름은 `rules/concurrency-rule.md`를 따른다.
- event/outbox를 다루는 흐름은 `rules/event-publication-rule.md`를 따른다.
- 조회 service/facade에는 `Query`를 이름에 붙인다.
- 변경 service/facade에는 `Command`를 붙이지 않는다.
- 예외 메시지는 `rules/error-message-rule.md`를 따른다.

## Input And Result Rule

- 변경 흐름 입력은 `{Action}Input`을 사용한다.
- 변경 흐름 결과는 `{Action}Result`를 사용한다.
- 조회 흐름 입력은 `{Action}Query`를 사용한다.
- 조회 흐름 결과는 `{Action}QueryResult` 또는 `{Projection}QueryResult`를 사용한다.
- pagination 입력은 공통 `Pagination(page, size, sort)`을 사용한다.
- 회원 ID, 주문 ID, 상품 ID 같은 조회 대상 식별자는 `Pagination`에 넣지 않고 use case/port 메서드의 별도 파라미터로 받는다.
- `{Feature}PageQuery`, `{MemberFeaturePageQuery}`처럼 page/size/sort와 식별자를 묶은 조회별 page query class를 계속 만들지 않는다.
- 목록 조회 결과는 `{Feature}PageResult`를 사용하고 `items`, `page`, `size`, `totalElements`, `totalPages`, `hasNext`를 포함한다.
- 목록 조회 use case/port는 `List<T>`를 직접 반환하지 않는다. pagination이 필요 없는 예외는 상한과 이유를 API 계약에 남긴다.
- 단순 단일 aggregate 조회는 service/query service 내부에서 `Pagination`을 Spring Data `Pageable`로 변환해 repository를 호출할 수 있다.
- Spring Data `Pageable`/`Page`를 controller request/response 또는 public use case API로 그대로 노출하지 않는다.
- join, 동적 검색, projection, 운영자 리포트성 조회는 QueryDSL query port로 분리한다.
- 컨트롤러 request/response를 내부 private workflow까지 끌고 가지 않는다.
- input/result 모델은 required/provided port 파일에 함께 두지 않는다.
- input/result/request/approval 모델은 도메인 루트 패키지의 `{Action}Models.kt` 파일에 둔다.
- Result는 use case 경계 모델이다. API 응답과 1:1이면 컨트롤러에서 Result를 그대로 감싸고, 단순 복사용 response DTO와 `toResponse()`는 만들지 않는다.
- API 응답 모양이 Result와 다를 때만 response DTO와 명시적 mapper를 둔다.

```text
AuthorizePaymentInput
AuthorizePaymentResult
CreateCouponOrderInput
CreateCouponOrderResult

GetPaymentQuery
PaymentDetailQueryResult
```

## Flow Rule

변경 흐름:

```text
request
  -> required use case
  -> domain root service/facade
  -> input
  -> domain aggregate entity/domainservice
  -> provided port
  -> result
  -> response
```

조회 흐름:

```text
request/query parameters
  -> query use case
  -> domain root query service/facade
  -> query port
  -> projection/read model
  -> response
```

## Service And Facade Must Not

```kotlin
// 금지: HTTP 응답 wrapper 사용
ApiResponse.success(result)

// 금지: controller annotation 사용
@RequestBody
@ResponseStatus
@Operation

// 금지: infra 구현체 직접 의존
JpaPaymentAdapter(...)
QueryDslPaymentQueryAdapter(...)

// 금지: 외부 adapter 직접 의존
MockExternalPaymentAdapter(...)
```

## Service And Facade Comment Rule

```kotlin
/**
 * 쿠폰 주문 생성 흐름에서 외부 결제 승인, 결제 저장, 쿠폰 적립을 순서대로 조율하는 application facade다.
 */
@Service
class CouponOrderFacade
```

규칙:

- 설명은 한글로 작성한다.
- 이 service/facade가 담당하는 use case 흐름을 한 문장으로 설명한다.
- 사용자가 흐름 이해를 위해 작성한 내부 주석은 `rules/comment-preservation-rule.md`에 따라 삭제하지 않는다.

## Error Message Rule

service/facade에서 `require`, `check`, `requireNotNull`, domain/application 예외를 직접 만들 때 메시지는 `rules/error-message-rule.md`를 따른다.

```kotlin
require(input.quantity > 0) { "쿠폰 수량은 1개 이상이어야 합니다." }

requireNotNull(savedPayment.paymentId) { "저장된 결제에는 결제 ID가 있어야 합니다." }
```

## Test Rule

- service/facade test는 `application` 모듈에 둔다.
- BehaviorSpec을 사용한다.
- provided port는 MockK로 mock 처리한다.
- domain aggregate는 mock 하지 않는다.
- service/facade test는 orchestration, port 호출, domain 결과 변환을 검증한다.
- controller 응답 wrapper나 HTTP status는 service/facade test에서 검증하지 않는다.
- transaction, 동시성, outbox 보장은 MockK orchestration test만으로 완료 처리하지 않고 필요한 integration test를 둔다.
- 공통 테스트 기준은 `rules/test-rule.md`를 따른다.

## Reference

- 네이밍 규칙: `rules/naming-rule.md`
- 아키텍처 규칙: `rules/backend-architecture.md`
- 트랜잭션 규칙: `rules/transaction-rule.md`
- 동시성 규칙: `rules/concurrency-rule.md`
- 이벤트 발행 규칙: `rules/event-publication-rule.md`
- 테스트 규칙: `rules/test-rule.md`
- 실제 서비스 예시: `docs/how/references/service-code-examples.md`
- 현재 적용 코드: `modules/application/src/main/kotlin/com/example/cardservice/application/payment`
