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
