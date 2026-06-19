# Controller Code Examples

이 문서는 `rules/controller-code-rule.md`의 긴 복사용 예시다. 평소에는 규칙 파일만 읽고, 새 컨트롤러를 만들 때 이 파일을 참고한다.

## Controller Example

```kotlin
package com.example.cardservice.web.payment

import com.example.cardservice.application.payment.CreateCouponOrderInput
import com.example.cardservice.application.payment.CreateCouponOrderResult
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.application.payment.required.CouponOrderUseCase
import com.example.cardservice.application.payment.request.CreateCouponOrderRequest
import com.example.cardservice.application.payment.response.CreateCouponOrderResponse
import com.example.cardservice.domain.payment.model.CustomerId
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.OrderId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/coupon-orders")
@Tag(name = "Coupon Order", description = "쿠폰 주문 결제 API")
class CouponOrderController(
    private val couponOrderUseCase: CouponOrderUseCase,
) {
    @PostMapping
    @Operation(summary = "쿠폰 주문", description = "외부 결제 mock 승인 후 쿠폰을 적립한다.")
    @ApiResponses(
        value = [
            SwaggerApiResponse(responseCode = "201", description = "쿠폰 주문 성공"),
            SwaggerApiResponse(responseCode = "400", description = "잘못된 요청"),
        ],
    )
    fun create(
        @RequestBody request: CreateCouponOrderRequest,
    ): ResponseEntity<ApiResponse<CreateCouponOrderResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(couponOrderUseCase.create(request.toInput()).toResponse()))

    private fun CreateCouponOrderRequest.toInput(): CreateCouponOrderInput =
        CreateCouponOrderInput(
            customerId = CustomerId(customerId),
            orderId = OrderId(orderId),
            idempotencyKey = IdempotencyKey(idempotencyKey),
            quantity = quantity,
        )

    private fun CreateCouponOrderResult.toResponse(): CreateCouponOrderResponse =
        CreateCouponOrderResponse(
            orderId = orderId.value,
            paymentId = paymentId.value.toString(),
            paymentStatus = paymentStatus.name,
            paymentStatusLabel = paymentStatus.label,
            amount = amount,
            currency = currency,
            couponIds = couponIds,
        )
}
```

## Request Example

```kotlin
package com.example.cardservice.application.payment.request

import io.swagger.v3.oas.annotations.media.Schema

data class CreateCouponOrderRequest(
    @get:Schema(description = "고객 ID", example = "customer-1")
    val customerId: String,

    @get:Schema(description = "주문 ID", example = "order-1")
    val orderId: String,

    @get:Schema(description = "중복 요청 방지 키", example = "idem-1")
    val idempotencyKey: String,

    @get:Schema(description = "쿠폰 수량", example = "2", minimum = "1")
    val quantity: Int,
)
```

## Response Example

```kotlin
package com.example.cardservice.application.payment.response

import io.swagger.v3.oas.annotations.media.Schema

data class CreateCouponOrderResponse(
    @get:Schema(description = "주문 ID", example = "order-1")
    val orderId: String,

    @get:Schema(description = "결제 ID", example = "1")
    val paymentId: String,

    @get:Schema(description = "결제 상태", example = "AUTHORIZED")
    val paymentStatus: String,

    @get:Schema(description = "결제 상태 한글 표시명", example = "승인 완료")
    val paymentStatusLabel: String,

    @get:Schema(description = "결제 금액", example = "10000")
    val amount: Long,

    @get:Schema(description = "통화", example = "KRW")
    val currency: String,

    @get:Schema(description = "적립된 쿠폰 ID 목록")
    val couponIds: List<String>,
)
```

## Change Endpoint Example

```kotlin
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment", description = "결제 생성/취소 API")
class PaymentController(
    private val authorizePaymentUseCase: AuthorizePaymentUseCase,
    private val cancelPaymentUseCase: CancelPaymentUseCase,
) {
    @PostMapping("/authorize")
    @Operation(summary = "결제 승인")
    fun authorize(
        @RequestBody request: AuthorizePaymentRequest,
    ): ResponseEntity<ApiResponse<AuthorizePaymentResponse>> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(authorizePaymentUseCase.authorize(request)))

    @PostMapping("/{paymentId}/cancel")
    fun cancel(
        @PathVariable paymentId: String,
        @RequestBody request: CancelPaymentRequest,
    ): ResponseEntity<ApiResponse<CancelPaymentResponse>> =
        ResponseEntity.ok(ApiResponse.success(cancelPaymentUseCase.cancel(paymentId, request)))
}
```

## Query Endpoint Example

```kotlin
@RestController
@RequestMapping("/api/payments")
@Tag(name = "Payment Query", description = "결제 조회 API")
class PaymentQueryController(
    private val getPaymentUseCase: GetPaymentQueryUseCase,
    private val searchPaymentsUseCase: SearchPaymentsQueryUseCase,
) {
    @GetMapping("/{paymentId}")
    @Operation(summary = "결제 상세 조회")
    fun get(
        @PathVariable paymentId: String,
    ): ResponseEntity<ApiResponse<PaymentDetailResponse>> =
        ResponseEntity.ok(ApiResponse.success(getPaymentUseCase.get(paymentId)))

    @GetMapping
    @Operation(summary = "결제 목록 조회")
    fun search(
        request: SearchPaymentsRequest,
    ): ResponseEntity<ApiResponse<SearchPaymentsResponse>> =
        ResponseEntity.ok(ApiResponse.success(searchPaymentsUseCase.search(request)))
}
```

## Error Handler Example

```kotlin
package com.example.cardservice.web.common

data class ApiErrorResponse(
    val code: String,
    val message: String,
    val fields: List<ApiFieldErrorResponse> = emptyList(),
)

data class ApiFieldErrorResponse(
    val field: String,
    val message: String,
)
```

```kotlin
package com.example.cardservice.web.common

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException

@RestControllerAdvice
class GlobalApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(exception: MethodArgumentNotValidException): ResponseEntity<ApiErrorResponse> {
        val fields = exception.bindingResult.fieldErrors.map { fieldError ->
            ApiFieldErrorResponse(
                field = fieldError.field,
                message = fieldError.safeMessage(),
            )
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "요청 값이 올바르지 않습니다.",
                    fields = fields,
                ),
            )
    }

    @ExceptionHandler(
        IllegalArgumentException::class,
        ConstraintViolationException::class,
        MethodArgumentTypeMismatchException::class,
    )
    fun handleBadRequestException(exception: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(
                ApiErrorResponse(
                    code = "BAD_REQUEST",
                    message = exception.message ?: "잘못된 요청입니다.",
                ),
            )

    @ExceptionHandler(Exception::class)
    fun handleUnexpectedException(exception: Exception): ResponseEntity<ApiErrorResponse> =
        ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ApiErrorResponse(
                    code = "INTERNAL_SERVER_ERROR",
                    message = "처리 중 오류가 발생했습니다.",
                ),
            )

    private fun FieldError.safeMessage(): String =
        defaultMessage ?: "올바르지 않은 값입니다."
}
```

## Controller Test Example

```kotlin
@WebMvcTest(CouponOrderController::class)
class CouponOrderControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var couponOrderUseCase: CouponOrderUseCase

    @Test
    fun `쿠폰 주문 요청을 use case로 전달한다`() {
        given(couponOrderUseCase.create(any<CreateCouponOrderInput>()))
            .willReturn(
                CreateCouponOrderResult(
                    orderId = OrderId("order-1"),
                    paymentId = PaymentId(1),
                    paymentStatus = PaymentStatus.AUTHORIZED,
                    amount = 10_000,
                    currency = "KRW",
                    couponIds = listOf("coupon_1", "coupon_2"),
                ),
            )

        mockMvc.post("/api/coupon-orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """
                {
                  "customerId": "customer-1",
                  "orderId": "order-1",
                  "idempotencyKey": "idem-1",
                  "quantity": 2
                }
            """.trimIndent()
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.code") { value("SUCCESS") }
                jsonPath("$.data.paymentStatus") { value("AUTHORIZED") }
                jsonPath("$.data.paymentStatusLabel") { value("승인 완료") }
                jsonPath("$.data.couponIds", hasSize<String>(2))
            }
    }
}
```
