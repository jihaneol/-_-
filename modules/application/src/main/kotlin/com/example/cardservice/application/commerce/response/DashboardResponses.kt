package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.CommerceDashboardSummaryResult

data class CommerceDashboardSummaryResponse(
    val memberCount: Long,
    val productCount: Long,
    val orderCount: Long,
    val paidOrderCount: Long,
    val refundedOrderCount: Long,
    val issuedCouponCount: Long,
)

fun CommerceDashboardSummaryResult.toResponse(): CommerceDashboardSummaryResponse =
    CommerceDashboardSummaryResponse(
        memberCount = memberCount,
        productCount = productCount,
        orderCount = orderCount,
        paidOrderCount = paidOrderCount,
        refundedOrderCount = refundedOrderCount,
        issuedCouponCount = issuedCouponCount,
    )
