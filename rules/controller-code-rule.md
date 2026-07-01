# Controller Code Rule

컨트롤러는 얇은 HTTP inbound adapter다. Swagger, use case 호출, status/body 조립만 둔다.

긴 예시는 `docs/how/references/controller-code-examples.md`.

## Shape

```text
application/{domain}/*Requests.kt
application/{domain}/*Responses.kt
{admin-api|shop-api}/web/{domain}/*Controller.kt
{admin-api|shop-api}/web/common/ApiResponse.kt
```

```kotlin
@RestController
@RequestMapping("/api/admin/products/{productId}/inventory")
@Tag(name = "Inventory", description = "재고 API")
class InventoryController(
    private val inventoryUseCase: InventoryUseCase,
) {
    @PatchMapping("/increase")
    @Operation(summary = "재고 증가")
    fun increase(
        @PathVariable productId: Long,
        @RequestBody request: AdjustInventoryRequest,
    ): ResponseEntity<ApiResponse<InventoryResponse>> =
        inventoryUseCase
            .increaseInventory(
                request.copy().also { it.productId = productId },
            )
            .toApplicationResponse()
}
```

Current example: `modules/admin-api/src/main/kotlin/com/example/cardservice/web/payment/couponorder/CouponOrderController.kt`

## Split

```text
MemberController       -> 회원 생성/수정/삭제/조회
ProductController      -> 상품 생성/수정/삭제/조회
InventoryController    -> 재고 생성/증감/조회
OrderController        -> 주문 생성/취소/삭제/조회
OrderPaymentController -> 주문 결제/환불
CouponController       -> 쿠폰/쿠폰 히스토리 조회
```

- `web/commerce/{domain}`, `web/shop/{domain}`처럼 runtime 이름을 한 번 더 넣지 않는다.
- 한 controller는 하나의 운영 책임과 필요한 use case만 가진다.
- command/query endpoint는 분리된 use case를 호출한다.

## Response

```kotlin
data class ApiResponse<T>(
    val result: ApplicationResult,
    val payload: T,
)

fun <T> T.toApplicationResponse(
    type: ApplicationResponseType = ApplicationResponseType.OK,
): ResponseEntity<ApiResponse<T>> = ...
```

```kotlin
// OK: request 타입은 하나만 쓰고, 외부에서 받으면 안 되는 값은 직접 copy로 채운다.
request.copy().also { it.productId = productId }

// OK: 1:1이면 use case response를 그대로 감싸기
response.toApplicationResponse()

// Avoid
ApiResponse<Any>
ApiResponse<List<ProductResponse>>      // top-level 대량 목록
fun MemberResponse.toApiResponse() = MemberApiResponse(id, name, email) // 단순 복사
```

- status는 `ResponseEntity`로 명시하고 `@ResponseStatus`는 쓰지 않는다.
- 대량 목록은 `items/page/size/totalElements/totalPages/hasNext`를 포함한다.
- 고정 소량 목록만 `{Feature}ListResponse`로 감싼다.

## Request And Response DTO

```kotlin
data class CreateCouponOrderRequest(
    @get:Schema(description = "주문 ID", example = "10")
    val orderId: Long,
)
```

- request/response: `application/{domain}`
- 외부에서 받으면 안 되는 값은 request public 생성 경로에 열지 않는다.
- path/header/auth/server-derived 값은 controller에서 `copy(...).also { ... }`로 직접 채운다.
- DTO 파일은 기능별로 분리한다. `CommerceRequests.kt` 같은 대형 파일 금지.
- DTO에 entity/JPA entity를 노출하지 않는다.
- bean validation annotation은 쓰지 않는다. 입력 검증은 application/domain에서 처리한다.

## Avoid

```kotlin
@ResponseStatus(HttpStatus.CREATED)
@Valid
@field:NotBlank
JpaPaymentAdapter(...)
JPAQueryFactory(entityManager)
try { ... } catch (e: Exception) { ApiErrorResponse(...) }
```

에러 응답은 `GlobalApiExceptionHandler -> ApiErrorResponse`로 변환한다.

## Swagger

```kotlin
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
```

- controller: `@Tag`
- method: `@Operation`, 주요 `@ApiResponses`
- field: 중요한 필드만 `@Schema`
- 내부 구현/테스트 메모/긴 비즈니스 설명은 description에 쓰지 않는다.

## Tests

- 위치: `admin-api` 또는 `shop-api`
- 성공 응답: `$.result.code`, `$.result.message`, `$.payload.*`
- 예외 응답: use case 예외가 HTTP error response로 변환되는지 확인
- convention: request DTO에 bean validation annotation 재유입 방지

## References

- `rules/naming-rule.md`
- `rules/error-message-rule.md`
- `rules/test-rule.md`
