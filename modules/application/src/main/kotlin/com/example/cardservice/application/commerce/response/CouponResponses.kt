package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.ApproveCouponExchangeResult
import com.example.cardservice.application.commerce.CouponConsistencyReportResult
import com.example.cardservice.application.commerce.CouponHistoryResult
import com.example.cardservice.application.commerce.CouponExchangeResult
import com.example.cardservice.application.commerce.CouponResult
import com.example.cardservice.application.commerce.CouponWalletResult
import com.example.cardservice.application.commerce.MemberCouponConsistencyResult
import com.example.cardservice.application.commerce.OrderCouponConsistencyResult
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

fun CouponResult.toResponse(): CouponResponse = CouponResponse(id, memberId, orderId, paymentId, status)
fun CouponHistoryResult.toResponse(): CouponHistoryResponse =
    CouponHistoryResponse(id, couponId, memberId, orderId, paymentId, type)
fun CouponExchangeResult.toResponse(): CouponExchangeResponse =
    CouponExchangeResponse(coupon.toResponse(), history.toResponse())
fun ApproveCouponExchangeResult.toResponse(): ApproveCouponExchangeResponse =
    ApproveCouponExchangeResponse(
        memberId = memberId,
        productId = productId,
        productName = productName,
        exchangedCouponCount = exchangedCouponCount,
        remainingIssuedCouponCount = remainingIssuedCouponCount,
        exchangedCouponIds = exchangedCouponIds,
    )
fun CouponConsistencyReportResult.toResponse(): CouponConsistencyReportResponse =
    CouponConsistencyReportResponse(
        consistent = consistent,
        totalCouponCount = totalCouponCount,
        totalIssueHistoryCount = totalIssueHistoryCount,
        totalVoidHistoryCount = totalVoidHistoryCount,
        totalExchangeHistoryCount = totalExchangeHistoryCount,
        memberRows = memberRows.map { it.toResponse() },
        orderRows = orderRows.map { it.toResponse() },
    )

fun CouponWalletResult.toResponse(): CouponWalletResponse =
    CouponWalletResponse(
        memberId = memberId,
        issuedCouponCount = issuedCouponCount,
        exchangedCouponCount = exchangedCouponCount,
        voidedCouponCount = voidedCouponCount,
        totalCouponCount = totalCouponCount,
        exchangeableSetCount = exchangeableSetCount,
        remainingToNextExchange = remainingToNextExchange,
        recentHistories = recentHistories.map { it.toResponse() },
    )

private fun MemberCouponConsistencyResult.toResponse(): MemberCouponConsistencyResponse =
    MemberCouponConsistencyResponse(
        memberId = memberId,
        issuedCouponCount = issuedCouponCount,
        voidedCouponCount = voidedCouponCount,
        exchangedCouponCount = exchangedCouponCount,
        issueHistoryCount = issueHistoryCount,
        voidHistoryCount = voidHistoryCount,
        exchangeHistoryCount = exchangeHistoryCount,
        exchangeableSetCount = exchangeableSetCount,
        remainingToNextExchange = remainingToNextExchange,
        consistent = consistent,
    )

private fun OrderCouponConsistencyResult.toResponse(): OrderCouponConsistencyResponse =
    OrderCouponConsistencyResponse(
        orderId = orderId,
        memberId = memberId,
        issuedCouponCount = issuedCouponCount,
        voidedCouponCount = voidedCouponCount,
        exchangedCouponCount = exchangedCouponCount,
        issueHistoryCount = issueHistoryCount,
        voidHistoryCount = voidHistoryCount,
        exchangeHistoryCount = exchangeHistoryCount,
        consistent = consistent,
    )
