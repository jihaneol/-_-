package com.example.cardservice.web.shop

import com.example.cardservice.application.commerce.CouponHistoryPageResult
import com.example.cardservice.application.commerce.CouponPageQuery
import com.example.cardservice.application.commerce.CouponPageResult
import com.example.cardservice.application.commerce.CouponWalletResult
import com.example.cardservice.application.commerce.MemberCouponHistoryPageQuery
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
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
    fun listCoupons(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ApiResponse<CouponPageResult> =
        ApiResponse.success(couponQueryUseCase.listCoupons(CouponPageQuery(memberId, page, size, sort)))

    @GetMapping("/coupon-histories")
    @Operation(summary = "쇼핑몰 회원 쿠폰 히스토리 조회")
    fun listCouponHistories(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ApiResponse<CouponHistoryPageResult> =
        ApiResponse.success(
            couponQueryUseCase.listMemberCouponHistories(MemberCouponHistoryPageQuery(memberId, page, size, sort)),
        )

    @GetMapping("/coupon-wallet")
    @Operation(summary = "쇼핑몰 회원 쿠폰 지갑 요약 조회")
    fun getCouponWallet(@PathVariable memberId: Long): ApiResponse<CouponWalletResult> =
        ApiResponse.success(couponQueryUseCase.getCouponWallet(memberId))
}
