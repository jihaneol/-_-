package com.example.cardservice.application.commerce

import com.example.cardservice.domain.commerce.model.CouponHistoryType
import com.example.cardservice.domain.commerce.model.CouponStatus

data class CouponResult(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)

data class CouponHistoryResult(
    val id: Long,
    val couponId: Long?,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val type: CouponHistoryType,
)
