package com.example.cardservice.infra.commerce

import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.domain.commerce.model.coupon.CouponHistoryType
import com.example.cardservice.domain.commerce.model.coupon.CouponStatus
import com.querydsl.core.annotations.QueryProjection

data class CouponRow @QueryProjection constructor(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
) {
    fun toResult(): CouponResult =
        CouponResult(
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
    fun toResult(): CouponHistoryResult =
        CouponHistoryResult(
            id = id,
            couponId = couponId,
            memberId = memberId,
            orderId = orderId,
            paymentId = paymentId,
            type = type,
        )
}
