package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.application.commerce.response.toResponse
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Coupon", description = "쿠폰과 쿠폰 히스토리 조회 API")
class CouponController(
    private val couponQueryUseCase: CouponQueryUseCase,
) {
    @GetMapping("/members/{memberId}/coupons")
    @Operation(summary = "회원 쿠폰 도장 조회")
    fun listCoupons(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.listCoupons(memberId).map { it.toResponse() })

    @GetMapping("/members/{memberId}/coupon-histories")
    @Operation(summary = "회원 쿠폰 히스토리 조회")
    fun listMemberCouponHistories(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.listMemberCouponHistories(memberId).map { it.toResponse() })

    @GetMapping("/orders/{orderId}/coupon-histories")
    @Operation(summary = "주문 쿠폰 히스토리 조회")
    fun listOrderCouponHistories(@PathVariable orderId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.listOrderCouponHistories(orderId).map { it.toResponse() })
}
