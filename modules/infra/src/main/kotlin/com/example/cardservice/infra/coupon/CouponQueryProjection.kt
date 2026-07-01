package com.example.cardservice.infra.coupon

import com.example.cardservice.application.coupon.CouponHistoryResponse
import com.example.cardservice.application.coupon.CouponResponse
import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus
import com.querydsl.core.annotations.QueryProjection

data class CouponRow @QueryProjection constructor(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
) {
    fun toResponse(): CouponResponse =
        CouponResponse(
            id = id,
            memberId = memberId,
            orderId = orderId,
            paymentId = paymentId,
            status = status,
        )
}

data class CouponHistoryRow @QueryProjection constructor(
    val id: Long,
    val couponId: Long?,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val type: CouponHistoryType,
) {
    fun toResponse(): CouponHistoryResponse =
        CouponHistoryResponse(
            id = id,
            couponId = couponId,
            memberId = memberId,
            orderId = orderId,
            paymentId = paymentId,
            type = type,
        )
}
