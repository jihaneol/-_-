package com.example.cardservice.web.coupon

import com.example.cardservice.application.coupon.ApproveCouponExchangeRequest
import com.example.cardservice.application.coupon.ApproveCouponExchangeResponse
import com.example.cardservice.application.coupon.CouponConsistencyReportResponse
import com.example.cardservice.application.coupon.CouponExchangeResponse
import com.example.cardservice.application.coupon.CouponHistoryPageResponse
import com.example.cardservice.application.coupon.CouponPageResponse
import com.example.cardservice.application.common.Pagination
import com.example.cardservice.application.coupon.required.CouponExchangeUseCase
import com.example.cardservice.application.coupon.required.CouponQueryUseCase
import com.example.cardservice.web.common.ApiResponse
import com.example.cardservice.web.common.toApplicationResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Coupon", description = "쿠폰과 쿠폰 히스토리 조회 API")
class CouponController(
    private val couponQueryUseCase: CouponQueryUseCase,
    private val couponExchangeUseCase: CouponExchangeUseCase,
) {
    @GetMapping("/members/{memberId}/coupons")
    @Operation(summary = "회원 쿠폰 도장 조회")
    fun listCoupons(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<CouponPageResponse>> =
        couponQueryUseCase.listCoupons(memberId, Pagination(page, size, sort)).toApplicationResponse()

    @GetMapping("/members/{memberId}/coupon-histories")
    @Operation(summary = "회원 쿠폰 히스토리 조회")
    fun listMemberCouponHistories(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<CouponHistoryPageResponse>> =
        couponQueryUseCase
            .listMemberCouponHistories(memberId, Pagination(page, size, sort))
            .toApplicationResponse()

    @GetMapping("/orders/{orderId}/coupon-histories")
    @Operation(summary = "주문 쿠폰 히스토리 조회")
    fun listOrderCouponHistories(
        @PathVariable orderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ResponseEntity<ApiResponse<CouponHistoryPageResponse>> =
        couponQueryUseCase
            .listOrderCouponHistories(orderId, Pagination(page, size, sort))
            .toApplicationResponse()

    @GetMapping("/coupon-consistency")
    @Operation(summary = "쿠폰 정합성 리포트 조회")
    fun getCouponConsistencyReport(): ResponseEntity<ApiResponse<CouponConsistencyReportResponse>> =
        couponQueryUseCase.getCouponConsistencyReport().toApplicationResponse()

    @PostMapping("/coupons/{couponId}/exchange")
    @Operation(summary = "쿠폰 교환 처리")
    fun exchangeCoupon(@PathVariable couponId: Long): ResponseEntity<ApiResponse<CouponExchangeResponse>> =
        couponExchangeUseCase.exchangeCoupon(couponId).toApplicationResponse()

    @PostMapping("/members/{memberId}/coupon-exchanges")
    @Operation(summary = "회원 쿠폰 10장 교환 승인")
    fun approveCouponExchange(
        @PathVariable memberId: Long,
        @RequestBody request: ApproveCouponExchangeRequest,
    ): ResponseEntity<ApiResponse<ApproveCouponExchangeResponse>> =
        couponExchangeUseCase
            .approveCouponExchange(
                request.copy().also { it.memberId = memberId },
            )
            .toApplicationResponse()
}
