package com.example.cardservice.payment.application.port.`in`

import com.example.cardservice.payment.domain.model.CustomerId
import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.OrderId
import com.example.cardservice.payment.domain.model.PaymentId
import com.example.cardservice.payment.domain.model.PaymentStatus

interface PlaceStarbucksCouponOrderUseCase {
    fun place(command: PlaceStarbucksCouponOrderCommand): PlaceStarbucksCouponOrderResult
}

data class PlaceStarbucksCouponOrderCommand(
    val customerId: CustomerId,
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val quantity: Int,
)

data class PlaceStarbucksCouponOrderResult(
    val orderId: OrderId,
    val paymentId: PaymentId,
    val paymentStatus: PaymentStatus,
    val amount: Long,
    val currency: String,
    val couponIds: List<String>,
)
