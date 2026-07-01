# Service And Facade Code Rule

Service/facade는 application use case 구현체다. HTTP, DB, 외부 기술을 모르고 `required` use case를 구현하며 `provided` port만 호출한다.

긴 예시는 `docs/how/references/service-code-examples.md`.

## Shape

```text
application/{domain}/{Action}Service.kt
application/{domain}/{Feature}Facade.kt
application/{domain}/{Action}Requests.kt
application/{domain}/{Action}Responses.kt
application/{domain}/required/{Action}UseCase.kt
application/{domain}/provided/{Capability}Port.kt
```

```kotlin
/**
 * 쿠폰 주문 생성 흐름에서 외부 결제 승인, 결제 저장, 쿠폰 적립을 순서대로 조율하는 application facade다.
 */
@Service
class CouponOrderFacade(
    private val externalPaymentPort: ExternalPaymentPort,
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
    private val accrueCouponPort: AccrueCouponPort,
) : CouponOrderUseCase {
    @Transactional
    override fun create(request: CreateCouponOrderRequest): CreateCouponOrderResponse {
        require(request.quantity > 0) { "쿠폰 수량은 1개 이상이어야 합니다." }
        // domain 생성/호출 -> provided port -> result
    }
}
```

Current example: `modules/application/src/main/kotlin/com/example/cardservice/application/payment/CouponOrderFacade.kt`

## Defaults

- 단일 도메인 규칙 실행: `{Action}Service`
- 여러 use case/port 조율: `{Feature}Facade`
- 조회 전용 흐름: `{Feature}QueryService` 또는 `{Feature}QueryFacade`
- 변경 transaction: service/facade의 `@Transactional`
- 조회 transaction: 별도 query service/facade의 `@Transactional(readOnly = true)`
- KDoc: 타입 선언 바로 위에 한글 한 문장

## Boundary

```kotlin
// OK
class ProductService(
    private val productRepository: ProductRepository,
) : ProductUseCase

// OK
class CouponOrderFacade(
    private val externalPaymentPort: ExternalPaymentPort,
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
) : CouponOrderUseCase

// Avoid
class CommerceService(...) // 회원/상품/재고/주문/결제/쿠폰 책임 혼합
class ProductService(private val adapter: JpaProductAdapter) // infra 구현체 직접 의존
```

## Request And Response

```kotlin
data class CreateCouponOrderRequest(...)
data class CreateCouponOrderResponse(...)
data class SearchCouponsQuery(...)
data class CouponPageResponse(
    val items: List<CouponSummaryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
```

- 변경 입력/결과: `{Action}Request`, `{Action}Response`
- 조회 입력/결과: `{Action}Query`, `{Projection}Response`
- 목록 입력: 공통 `Pagination`; `memberId`, `orderId`는 별도 파라미터
- 목록 결과: `{Feature}PageResponse` with `items/page/size/totalElements/totalPages/hasNext`
- `Pageable`/`Page`는 use case API, controller response로 노출하지 않는다.
- Response가 API와 1:1이면 controller에서 바로 `toApplicationResponse()`로 감싼다.
- 외부에서 받으면 안 되는 값은 public 생성 경로에 열지 않는다.
- request는 하나만 사용하고, 내부 값은 controller에서 `copy(...).also { ... }`로 직접 채운다.

```kotlin
data class AdjustInventoryRequest(
    val quantity: Long,
) {
    var productId: Long? = null
        internal set
}

fun increase(productId: Long, request: AdjustInventoryRequest) =
    inventoryUseCase.increaseInventory(
        request.copy().also { it.productId = productId },
    )
```

## Flow

```text
command: request -> use case -> service/facade -> domain -> provided port -> result -> response
query:   params  -> query use case -> query service -> query port -> projection -> response
```

## Avoid

```kotlin
response.toApplicationResponse()        // web wrapper 사용
@RequestBody                            // controller annotation 사용
@Operation                              // Swagger 사용
JpaPaymentAdapter(...)                  // infra 구현체 직접 의존
MockExternalPaymentAdapter(...)         // external adapter 직접 의존
repository.findAll(Pageable.unpaged())  // 무제한 목록
```

## Tests

- 위치: `modules/application/src/test`
- 스타일: BehaviorSpec + MockK ports
- mock 금지: domain aggregate
- 검증: orchestration, port 호출, domain 결과 변환
- 별도 integration 필요: transaction, concurrency, outbox

## References

- `rules/backend-architecture.md`
- `rules/transaction-rule.md`
- `rules/concurrency-rule.md`
- `rules/event-publication-rule.md`
- `rules/error-message-rule.md`
- `rules/test-rule.md`
