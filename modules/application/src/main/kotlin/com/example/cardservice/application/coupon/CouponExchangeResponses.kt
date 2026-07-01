package com.example.cardservice.application.coupon

data class CouponExchangeResponse(
    val coupon: CouponResponse,
    val history: CouponHistoryResponse,
)

data class ApproveCouponExchangeResponse(
    val memberId: Long,
    val productId: Long,
    val productName: String,
    val exchangedCouponCount: Int,
    val remainingIssuedCouponCount: Long,
    val exchangedCouponIds: List<Long>,
)
