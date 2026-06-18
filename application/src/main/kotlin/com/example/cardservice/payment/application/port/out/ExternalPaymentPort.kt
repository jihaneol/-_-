package com.example.cardservice.payment.application.port.out

import com.example.cardservice.payment.domain.model.IdempotencyKey
import com.example.cardservice.payment.domain.model.Money
import com.example.cardservice.payment.domain.model.OrderId

interface ExternalPaymentPort {
    fun approve(request: ExternalPaymentRequest): ExternalPaymentApproval
}

data class ExternalPaymentRequest(
    val orderId: OrderId,
    val idempotencyKey: IdempotencyKey,
    val money: Money,
)

data class ExternalPaymentApproval(
    val approvalKey: String,
)
