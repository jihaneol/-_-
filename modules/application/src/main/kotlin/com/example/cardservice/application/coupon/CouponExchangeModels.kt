package com.example.cardservice.application.coupon

data class CouponExchangeResult(
    val coupon: CouponResult,
    val history: CouponHistoryResult,
)

data class ApproveCouponExchangeInput(val productId: Long)

data class ApproveCouponExchangeResult(
    val memberId: Long,
    val productId: Long,
    val productName: String,
    val exchangedCouponCount: Int,
    val remainingIssuedCouponCount: Long,
    val exchangedCouponIds: List<Long>,
)
