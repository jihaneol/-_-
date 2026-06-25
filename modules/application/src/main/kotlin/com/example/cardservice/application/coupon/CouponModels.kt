package com.example.cardservice.application.coupon

import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus

data class CouponResult(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)

data class CouponPageResult(
    val items: List<CouponResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class CouponHistoryResult(
    val id: Long,
    val couponId: Long?,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val type: CouponHistoryType,
)

data class CouponHistoryPageResult(
    val items: List<CouponHistoryResult>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
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

data class CouponWalletResult(
    val memberId: Long,
    val issuedCouponCount: Long,
    val exchangedCouponCount: Long,
    val voidedCouponCount: Long,
    val totalCouponCount: Long,
    val exchangeableSetCount: Long,
    val remainingToNextExchange: Long,
    val recentHistories: List<CouponHistoryResult>,
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
