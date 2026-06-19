package com.example.cardservice.application.commerce

data class CommerceDashboardSummaryResult(
    val memberCount: Long,
    val productCount: Long,
    val orderCount: Long,
    val paidOrderCount: Long,
    val refundedOrderCount: Long,
    val issuedCouponCount: Long,
)
