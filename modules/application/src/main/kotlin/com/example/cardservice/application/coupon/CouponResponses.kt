package com.example.cardservice.application.coupon

import com.example.cardservice.domain.coupon.CouponHistoryType
import com.example.cardservice.domain.coupon.CouponStatus

data class CouponResponse(
    val id: Long,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val status: CouponStatus,
)

data class CouponPageResponse(
    val items: List<CouponResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class CouponHistoryResponse(
    val id: Long,
    val couponId: Long?,
    val memberId: Long,
    val orderId: Long,
    val paymentId: Long,
    val type: CouponHistoryType,
)

data class CouponHistoryPageResponse(
    val items: List<CouponHistoryResponse>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)

data class CouponConsistencyReportResponse(
    val consistent: Boolean,
    val totalCouponCount: Long,
    val totalIssueHistoryCount: Long,
    val totalVoidHistoryCount: Long,
    val totalExchangeHistoryCount: Long,
    val memberRows: List<MemberCouponConsistencyResponse>,
    val orderRows: List<OrderCouponConsistencyResponse>,
)

data class CouponWalletResponse(
    val memberId: Long,
    val issuedCouponCount: Long,
    val exchangedCouponCount: Long,
    val voidedCouponCount: Long,
    val totalCouponCount: Long,
    val exchangeableSetCount: Long,
    val remainingToNextExchange: Long,
    val recentHistories: List<CouponHistoryResponse>,
)

data class MemberCouponConsistencyResponse(
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

data class OrderCouponConsistencyResponse(
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
