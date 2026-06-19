package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.domain.commerce.model.CouponHistoryType
import com.example.cardservice.domain.commerce.model.CouponStatus

data class CouponResponse(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)

data class CouponHistoryResponse(
    val id: Long,
    val couponId: Long?,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val type: CouponHistoryType,
)

fun CouponResult.toResponse(): CouponResponse = CouponResponse(id, memberId, orderId, paymentId, status)
fun CouponHistoryResult.toResponse(): CouponHistoryResponse =
    CouponHistoryResponse(id, couponId, memberId, orderId, paymentId, type)
