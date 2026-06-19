package com.example.cardservice.application.commerce.response

import com.example.cardservice.application.commerce.PayOrderResult
import com.example.cardservice.application.commerce.RefundOrderResult
import com.example.cardservice.domain.commerce.model.OrderStatus

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

fun PayOrderResult.toResponse(): PayOrderResponse =
    PayOrderResponse(orderId, paymentId, orderStatus, paymentStatus, paidAmount, issuedCouponCount)

fun RefundOrderResult.toResponse(): RefundOrderResponse =
    RefundOrderResponse(orderId, paymentId, orderStatus, paymentStatus, voidedCouponCount)
