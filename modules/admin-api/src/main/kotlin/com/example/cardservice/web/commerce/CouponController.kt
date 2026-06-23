package com.example.cardservice.web.commerce

import com.example.cardservice.application.commerce.ApproveCouponExchangeInput
import com.example.cardservice.application.commerce.ApproveCouponExchangeResult
import com.example.cardservice.application.commerce.CouponConsistencyReportResult
import com.example.cardservice.application.commerce.CouponExchangeResult
import com.example.cardservice.application.commerce.CouponHistoryPageResult
import com.example.cardservice.application.commerce.CouponPageQuery
import com.example.cardservice.application.commerce.CouponPageResult
import com.example.cardservice.application.commerce.MemberCouponHistoryPageQuery
import com.example.cardservice.application.commerce.OrderCouponHistoryPageQuery
import com.example.cardservice.application.commerce.request.ApproveCouponExchangeRequest
import com.example.cardservice.application.commerce.required.CouponExchangeUseCase
import com.example.cardservice.application.commerce.required.CouponQueryUseCase
import com.example.cardservice.web.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
    ): ApiResponse<CouponPageResult> =
        ApiResponse.success(couponQueryUseCase.listCoupons(CouponPageQuery(memberId, page, size, sort)))

    @GetMapping("/members/{memberId}/coupon-histories")
    @Operation(summary = "회원 쿠폰 히스토리 조회")
    fun listMemberCouponHistories(
        @PathVariable memberId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ApiResponse<CouponHistoryPageResult> =
        ApiResponse.success(
            couponQueryUseCase.listMemberCouponHistories(MemberCouponHistoryPageQuery(memberId, page, size, sort)),
        )

    @GetMapping("/orders/{orderId}/coupon-histories")
    @Operation(summary = "주문 쿠폰 히스토리 조회")
    fun listOrderCouponHistories(
        @PathVariable orderId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(defaultValue = "id,desc") sort: String,
    ): ApiResponse<CouponHistoryPageResult> =
        ApiResponse.success(
            couponQueryUseCase.listOrderCouponHistories(OrderCouponHistoryPageQuery(orderId, page, size, sort)),
        )

    @GetMapping("/coupon-consistency")
    @Operation(summary = "쿠폰 정합성 리포트 조회")
    fun getCouponConsistencyReport(): ApiResponse<CouponConsistencyReportResult> =
        ApiResponse.success(couponQueryUseCase.getCouponConsistencyReport())

    @PostMapping("/coupons/{couponId}/exchange")
    @Operation(summary = "쿠폰 교환 처리")
    fun exchangeCoupon(@PathVariable couponId: Long): ApiResponse<CouponExchangeResult> =
        ApiResponse.success(couponExchangeUseCase.exchangeCoupon(couponId))

    @PostMapping("/members/{memberId}/coupon-exchanges")
    @Operation(summary = "회원 쿠폰 10장 교환 승인")
    fun approveCouponExchange(
        @PathVariable memberId: Long,
        @RequestBody request: ApproveCouponExchangeRequest,
    ): ApiResponse<ApproveCouponExchangeResult> =
        ApiResponse.success(
            couponExchangeUseCase.approveCouponExchange(
                memberId = memberId,
                input = ApproveCouponExchangeInput(productId = request.productId),
            ),
        )
}
