# Controller Code Rule

컨트롤러는 HTTP inbound adapter다. 컨트롤러는 얇게 유지하고 request/response 모델은 `application` 모듈의 별도 패키지에 둔다.

긴 복사용 예시는 `docs/how/references/controller-code-examples.md`를 필요할 때만 읽는다.

## Package Rule

```text
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/request
modules/application/src/main/kotlin/com/example/cardservice/application/{domain}/response
modules/bootstrap/src/main/kotlin/com/example/cardservice/web/{domain}
modules/bootstrap/src/main/kotlin/com/example/cardservice/web/common
```

## Feature Split Rule

도메인 하나가 여러 운영 기능을 가지면 컨트롤러를 하나의 큰 클래스로 합치지 않고 기능 책임별로 분리한다.

예시:

```text
MemberController       -> 회원 생성/수정/삭제/조회
ProductController      -> 상품 생성/수정/삭제/조회
InventoryController    -> 재고 생성/증감/조회
OrderController        -> 주문 생성/취소/삭제/조회
OrderPaymentController -> 주문 결제/전체 환불
CouponController       -> 쿠폰과 쿠폰 히스토리 조회
```

규칙:

- 한 컨트롤러는 하나의 운영 책임만 가진다.
- 하나의 컨트롤러에 회원, 상품, 주문, 결제, 쿠폰 API를 모두 넣지 않는다.
- 기능별 컨트롤러는 필요한 use case만 주입받는다.
- 공통 응답 조립이 필요하면 private helper나 공통 web utility를 사용하되, 비즈니스 판단을 넣지 않는다.

## Controller Rule

- 컨트롤러는 Swagger annotation, use case 호출, HTTP status/body 조립만 담당한다.
- 비즈니스 판단, 금액 계산, 도메인 상태 전이, repository 호출, QueryDSL 호출, external adapter 직접 호출은 금지한다.
- command endpoint와 query endpoint는 use case도 분리해서 호출한다.
- `@ResponseStatus`는 사용하지 않는다. HTTP status는 `ResponseEntity`로 명시한다.
- 성공 응답은 `ApiResponse<T>`로 감싼다.
- 에러 응답은 `GlobalApiExceptionHandler`가 `ApiErrorResponse`로 변환한다.
- 에러 메시지는 `rules/error-message-rule.md`를 따른다.

## Common Success Response

```kotlin
package com.example.cardservice.web.common

data class ApiResponse<T>(
    val code: String,
    val message: String,
    val data: T,
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> =
            ApiResponse(
                code = "SUCCESS",
                message = "요청이 성공했습니다.",
                data = data,
            )
    }
}
```

사용 기준:

```kotlin
ResponseEntity
    .status(HttpStatus.CREATED)
    .body(ApiResponse.success(result))

ResponseEntity.ok(ApiResponse.success(result))
```

## Request And Response Rule

- request는 `application/{domain}/request` 패키지에 둔다.
- response는 `application/{domain}/response` 패키지에 둔다.
- request/response 파일도 기능 책임별로 분리한다. 예: `MemberRequests.kt`, `OrderPaymentResponses.kt`.
- 여러 기능의 DTO를 `CommerceRequests.kt`, `CommerceResponses.kt` 같은 대형 파일에 모으지 않는다.
- request/response에는 도메인 entity나 JPA entity를 노출하지 않는다.
- request/response DTO에는 bean validation annotation을 사용하지 않는다.
- `@Valid`, `@get:NotBlank`, `@field:NotBlank`, `@get:Min`, `@field:Min`은 사용하지 않는다.
- 입력 값 검증은 application/domain layer의 value object와 service 규칙으로 처리한다.
- Swagger `@Schema`는 문서화를 위해 `@get:` target을 사용할 수 있다.

```kotlin
@get:Schema(description = "주문 ID", example = "order-1")
val orderId: String
```

## Swagger Rule

- 컨트롤러에는 `@Tag`를 작성한다.
- 메서드에는 `@Operation`을 작성한다.
- 주요 성공/실패 응답에는 `@ApiResponses`를 작성한다.
- request/response 필드에는 중요한 필드만 `@Schema`를 작성한다.
- 내부 구현, 테스트 메모, 너무 긴 비즈니스 설명은 Swagger description에 쓰지 않는다.
- 프로젝트 공통 `ApiResponse`와 Swagger `ApiResponse` 이름이 겹치면 Swagger annotation은 alias 처리한다.

```kotlin
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
```

## Error Handler Rule

- 컨트롤러 메서드에서 try-catch로 에러 응답을 만들지 않는다.
- 잘못된 요청은 `400 BAD_REQUEST`로 변환한다.
- bean validation을 쓰는 예외적 endpoint가 생기면 validation 실패는 `400 VALIDATION_ERROR`로 변환한다.
- 예상하지 못한 예외는 `500 INTERNAL_SERVER_ERROR`로 변환한다.
- 도메인별 예외가 생기면 명시적인 error code로 확장한다.

## Test Rule

- web slice test는 `bootstrap` 모듈에 둔다.
- 성공 응답은 `$.code`, `$.message`, `$.data.*`를 검증한다.
- use case 예외가 HTTP error response로 변환되는지 검증한다.
- request DTO에 bean validation annotation이 다시 들어오지 않도록 source convention test로 검증한다.
- 공통 테스트 기준은 `rules/test-rule.md`를 따른다.

## Reference

- 네이밍 규칙: `rules/naming-rule.md`
- 에러 메시지 규칙: `rules/error-message-rule.md`
- 테스트 규칙: `rules/test-rule.md`
- 실제 컨트롤러 예시: `docs/how/references/controller-code-examples.md`
- 현재 적용 코드: `modules/bootstrap/src/main/kotlin/com/example/cardservice/web/payment/CouponOrderController.kt`
- 현재 테스트 코드: `modules/bootstrap/src/test/kotlin/com/example/cardservice/web/payment/CouponOrderControllerTest.kt`
