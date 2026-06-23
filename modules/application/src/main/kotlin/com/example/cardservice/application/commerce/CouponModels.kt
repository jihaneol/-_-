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

data class CouponPageQuery(
    val memberId: Long,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    val normalizedPage: Int = page.coerceAtLeast(0)
    val normalizedSize: Int = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
    val sortDirection: SortDirection = SortDirection.from(sort)
}

data class MemberCouponHistoryPageQuery(
    val memberId: Long,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    val normalizedPage: Int = page.coerceAtLeast(0)
    val normalizedSize: Int = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
    val sortDirection: SortDirection = SortDirection.from(sort)
}

data class OrderCouponHistoryPageQuery(
    val orderId: Long,
    val page: Int,
    val size: Int,
    val sort: String,
) {
    val normalizedPage: Int = page.coerceAtLeast(0)
    val normalizedSize: Int = size.coerceIn(MIN_PAGE_SIZE, MAX_PAGE_SIZE)
    val sortDirection: SortDirection = SortDirection.from(sort)
}

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

enum class SortDirection {
    ASC,
    DESC,
    ;

    companion object {
        fun from(sort: String): SortDirection =
            if (sort.substringAfter(",", "desc").equals("asc", ignoreCase = true)) ASC else DESC
    }
}

const val DEFAULT_PAGE_SIZE = 20
const val MAX_PAGE_SIZE = 100
private const val MIN_PAGE_SIZE = 1
