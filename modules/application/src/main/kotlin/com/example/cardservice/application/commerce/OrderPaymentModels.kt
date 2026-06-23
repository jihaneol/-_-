package com.example.cardservice.application.commerce

import com.example.cardservice.domain.commerce.model.order.OrderStatus

data class PayOrderInput(val idempotencyKey: String)
data class PayOrderResult(
    val orderId: Long,
    val paymentId: Long,
    val orderStatus: OrderStatus,
    val paymentStatus: String,
    val paidAmount: Long,
    val issuedCouponCount: Int,
)

data class RefundOrderResult(
    val orderId: Long,
    val paymentId: Long,
    val orderStatus: OrderStatus,
    val paymentStatus: String,
    val voidedCouponCount: Int,
)
