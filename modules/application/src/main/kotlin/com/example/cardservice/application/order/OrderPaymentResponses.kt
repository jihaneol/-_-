package com.example.cardservice.application.order

import com.example.cardservice.domain.order.OrderStatus

data class PayOrderResponse(
    val orderId: Long,
    val paymentId: Long,
    val orderStatus: OrderStatus,
    val paymentStatus: String,
    val paidAmount: Long,
    val issuedCouponCount: Int,
)

data class RefundOrderResponse(
    val orderId: Long,
    val paymentId: Long,
    val orderStatus: OrderStatus,
    val paymentStatus: String,
    val voidedCouponCount: Int,
)
