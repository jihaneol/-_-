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

data class CouponConsistencyReportResult(
    val consistent: Boolean,
    val totalCouponCount: Long,
    val totalIssueHistoryCount: Long,
    val totalVoidHistoryCount: Long,
    val totalExchangeHistoryCount: Long,
    val memberRows: List<MemberCouponConsistencyResult>,
    val orderRows: List<OrderCouponConsistencyResult>,
)

data class MemberCouponConsistencyResult(
    val memberId: Long,
    val issuedCouponCount: Long,
    val voidedCouponCount: Long,
    val exchangedCouponCount: Long,
    val issueHistoryCount: Long,
    val voidHistoryCount: Long,
    val exchangeHistoryCount: Long,
    val exchangeableSetCount: Long,
    val remainingToNextExchange: Long,
    val consistent: Boolean,
)

data class OrderCouponConsistencyResult(
    val orderId: Long,
    val memberId: Long,
    val issuedCouponCount: Long,
    val voidedCouponCount: Long,
    val exchangedCouponCount: Long,
    val issueHistoryCount: Long,
    val voidHistoryCount: Long,
    val exchangeHistoryCount: Long,
    val consistent: Boolean,
)
