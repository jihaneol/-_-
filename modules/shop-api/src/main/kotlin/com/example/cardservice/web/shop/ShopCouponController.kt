package com.example.cardservice.web.shop

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
@RequestMapping("/api/shop/members/{memberId}")
@Tag(name = "Shop Coupon", description = "쇼핑몰 쿠폰 API")
class ShopCouponController(
    private val couponQueryUseCase: CouponQueryUseCase,
) {
    @GetMapping("/coupons")
    @Operation(summary = "쇼핑몰 회원 쿠폰 조회")
    fun listCoupons(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.listCoupons(memberId).map { it.toResponse() })

    @GetMapping("/coupon-histories")
    @Operation(summary = "쇼핑몰 회원 쿠폰 히스토리 조회")
    fun listCouponHistories(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.listMemberCouponHistories(memberId).map { it.toResponse() })

    @GetMapping("/coupon-wallet")
    @Operation(summary = "쇼핑몰 회원 쿠폰 지갑 요약 조회")
    fun getCouponWallet(@PathVariable memberId: Long): ApiResponse<Any> =
        ApiResponse.success(couponQueryUseCase.getCouponWallet(memberId).toResponse())
}
