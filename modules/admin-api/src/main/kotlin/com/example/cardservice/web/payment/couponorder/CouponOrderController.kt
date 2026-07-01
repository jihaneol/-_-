package com.example.cardservice.web.payment.couponorder

import com.example.cardservice.application.payment.CreateCouponOrderRequest
import com.example.cardservice.application.payment.CreateCouponOrderResponse
import com.example.cardservice.application.payment.required.CouponOrderUseCase
import com.example.cardservice.web.common.ApplicationResponseType
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/coupon-orders")
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
        couponOrderUseCase
            .create(request)
            .toApplicationResponse(ApplicationResponseType.CREATED)
}
