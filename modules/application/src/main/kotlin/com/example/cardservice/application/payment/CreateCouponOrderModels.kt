package com.example.cardservice.application.payment

import com.example.cardservice.domain.payment.model.CustomerId
import com.example.cardservice.domain.payment.model.IdempotencyKey
import com.example.cardservice.domain.payment.model.OrderId
import com.example.cardservice.domain.payment.model.PaymentId
import com.example.cardservice.domain.payment.model.PaymentStatus

data class CreateCouponOrderInput(
    val customerId: CustomerId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val quantity: Int,
)

data class CreateCouponOrderResult(
    val orderId: OrderId,
    val paymentId: PaymentId,
    val paymentStatus: PaymentStatus,
    val amount: Long,
    val currency: String,
    val couponIds: List<String>,
)
